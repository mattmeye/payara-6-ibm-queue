package com.example.ibmmq.pool;

import com.example.ibmmq.config.ConnectionPoolConfig;
import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.adapter.JakartaJMSAdapter;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class IBMMQConnectionPool {

    private static final Logger LOGGER = Logger.getLogger(IBMMQConnectionPool.class.getName());

    @Inject
    private IBMMQConfig mqConfig;

    @Inject
    private ConnectionPoolConfig poolConfig;

    private MQConnectionFactory connectionFactory;
    private BlockingQueue<PooledConnection> availableConnections;
    private ConcurrentMap<Connection, PooledConnection> activeConnections;
    private AtomicInteger totalConnections;
    private volatile boolean isShutdown = false;

    @PostConstruct
    public void initialize() {
        try {
            LOGGER.info("Initializing IBM MQ Connection Pool");

            createConnectionFactory();
            initializePool();

            LOGGER.info("IBM MQ Connection Pool initialized successfully - " +
                       "Initial Size: " + poolConfig.getInitialPoolSize() +
                       ", Max Size: " + poolConfig.getMaxPoolSize());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize IBM MQ Connection Pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    private void createConnectionFactory() throws javax.jms.JMSException {
        connectionFactory = new MQConnectionFactory();
        connectionFactory.setHostName(mqConfig.getHostname());
        connectionFactory.setPort(mqConfig.getPort());
        connectionFactory.setChannel(mqConfig.getChannel());
        connectionFactory.setQueueManager(mqConfig.getQueueManager());
        connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

        if (mqConfig.getUsername() != null && !mqConfig.getUsername().isEmpty()) {
            connectionFactory.setStringProperty(WMQConstants.USERID, mqConfig.getUsername());
            connectionFactory.setStringProperty(WMQConstants.PASSWORD, mqConfig.getPassword());
        }

        connectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        connectionFactory.setStringProperty(WMQConstants.WMQ_CONNECTION_NAME_LIST,
            mqConfig.getHostname() + "(" + mqConfig.getPort() + ")");

        // Set connection properties using MQConnectionFactory methods
        if (poolConfig.getReceiveTimeout() > 0) {
            connectionFactory.setIntProperty("XMSC_WMQ_RECEIVE_TIMEOUT", (int) poolConfig.getReceiveTimeout());
        }
        if (poolConfig.getSendTimeout() > 0) {
            connectionFactory.setIntProperty("XMSC_WMQ_SEND_TIMEOUT", (int) poolConfig.getSendTimeout());
        }
        if (poolConfig.getHeartbeatInterval() > 0) {
            connectionFactory.setIntProperty("XMSC_WMQ_HEARTBEAT_INTERVAL", poolConfig.getHeartbeatInterval());
        }

        // Enable connection sharing and temporary models
        connectionFactory.setStringProperty("XMSC_WMQ_TEMP_Q_PREFIX", "AMQ.JMS.TEMP");
        connectionFactory.setBooleanProperty("XMSC_WMQ_SHARE_CONV_ALLOWED", true);
    }

    private void initializePool() {
        availableConnections = new ArrayBlockingQueue<>(poolConfig.getMaxPoolSize());
        activeConnections = new ConcurrentHashMap<>();
        totalConnections = new AtomicInteger(0);

        for (int i = 0; i < poolConfig.getInitialPoolSize(); i++) {
            try {
                PooledConnection pooledConnection = createPooledConnection();
                availableConnections.offer(pooledConnection);
                totalConnections.incrementAndGet();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to create initial connection " + (i + 1), e);
            }
        }
    }

    public Connection getConnection() throws JMSException {
        if (isShutdown) {
            throw new JMSException("Connection pool is shutdown");
        }

        PooledConnection pooledConnection = null;

        try {
            pooledConnection = availableConnections.poll();

            if (pooledConnection == null) {
                if (totalConnections.get() < poolConfig.getMaxPoolSize()) {
                    pooledConnection = createPooledConnection();
                    totalConnections.incrementAndGet();
                    LOGGER.fine("Created new connection, total: " + totalConnections.get());
                } else {
                    long startTime = System.currentTimeMillis();
                    pooledConnection = availableConnections.take();
                    long waitTime = System.currentTimeMillis() - startTime;

                    if (waitTime > poolConfig.getMaxWaitTime()) {
                        LOGGER.warning("Connection wait time exceeded: " + waitTime + "ms");
                    }
                }
            }

            if (poolConfig.isValidationEnabled() && !isConnectionValid(pooledConnection)) {
                LOGGER.warning("Invalid connection detected, creating new one");
                closePooledConnection(pooledConnection);
                totalConnections.decrementAndGet();
                pooledConnection = createPooledConnection();
                totalConnections.incrementAndGet();
            }

            pooledConnection.markAsActive();
            activeConnections.put(pooledConnection.getConnection(), pooledConnection);

            LOGGER.fine("Connection acquired - Active: " + activeConnections.size() +
                       ", Available: " + availableConnections.size() +
                       ", Total: " + totalConnections.get());

            return pooledConnection.getConnection();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JMSException("Interrupted while waiting for connection");
        } catch (Exception e) {
            if (pooledConnection != null) {
                closePooledConnection(pooledConnection);
                totalConnections.decrementAndGet();
            }
            throw new JMSException("Failed to get connection from pool: " + e.getMessage());
        }
    }

    public void releaseConnection(Connection connection) {
        if (connection == null || isShutdown) {
            return;
        }

        PooledConnection pooledConnection = activeConnections.remove(connection);
        if (pooledConnection == null) {
            LOGGER.warning("Attempted to release unknown connection");
            return;
        }

        try {
            if (isConnectionValid(pooledConnection)) {
                pooledConnection.markAsAvailable();

                if (availableConnections.size() < poolConfig.getMinPoolSize() ||
                    (System.currentTimeMillis() - pooledConnection.getLastUsed()) < poolConfig.getMaxIdleTime()) {
                    availableConnections.offer(pooledConnection);
                    LOGGER.fine("Connection returned to pool");
                } else {
                    closePooledConnection(pooledConnection);
                    totalConnections.decrementAndGet();
                    LOGGER.fine("Connection closed due to idle time, total: " + totalConnections.get());
                }
            } else {
                closePooledConnection(pooledConnection);
                totalConnections.decrementAndGet();
                LOGGER.fine("Invalid connection closed, total: " + totalConnections.get());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error releasing connection", e);
            closePooledConnection(pooledConnection);
            totalConnections.decrementAndGet();
        }
    }

    private PooledConnection createPooledConnection() throws JMSException {
        try {
            // Create javax.jms connection from IBM MQ factory
            javax.jms.Connection javaxConnection = connectionFactory.createConnection();
            // Wrap it to provide Jakarta JMS interface
            Connection jakartaConnection = new JakartaJMSAdapter.ConnectionWrapper(javaxConnection);
            return new PooledConnection(jakartaConnection);
        } catch (javax.jms.JMSException e) {
            throw new JMSException(e.getMessage());
        }
    }

    private boolean isConnectionValid(PooledConnection pooledConnection) {
        if (pooledConnection == null || pooledConnection.getConnection() == null) {
            return false;
        }

        try {
            pooledConnection.getConnection().getMetaData();
            return true;
        } catch (Exception e) {
            LOGGER.fine("Connection validation failed: " + e.getMessage());
            return false;
        }
    }

    private void closePooledConnection(PooledConnection pooledConnection) {
        if (pooledConnection != null) {
            try {
                pooledConnection.close();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Error closing pooled connection", e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        LOGGER.info("Shutting down IBM MQ Connection Pool");
        isShutdown = true;

        for (PooledConnection pooledConnection : activeConnections.values()) {
            closePooledConnection(pooledConnection);
        }
        activeConnections.clear();

        PooledConnection pooledConnection;
        while ((pooledConnection = availableConnections.poll()) != null) {
            closePooledConnection(pooledConnection);
        }

        LOGGER.info("IBM MQ Connection Pool shutdown completed");
    }

    public PoolStatus getPoolStatus() {
        return new PoolStatus(
            totalConnections.get(),
            activeConnections.size(),
            availableConnections.size(),
            poolConfig.getMaxPoolSize(),
            poolConfig.getMinPoolSize()
        );
    }

    public static class PoolStatus {
        private final int totalConnections;
        private final int activeConnections;
        private final int availableConnections;
        private final int maxPoolSize;
        private final int minPoolSize;

        public PoolStatus(int totalConnections, int activeConnections, int availableConnections,
                         int maxPoolSize, int minPoolSize) {
            this.totalConnections = totalConnections;
            this.activeConnections = activeConnections;
            this.availableConnections = availableConnections;
            this.maxPoolSize = maxPoolSize;
            this.minPoolSize = minPoolSize;
        }

        public int getTotalConnections() { return totalConnections; }
        public int getActiveConnections() { return activeConnections; }
        public int getAvailableConnections() { return availableConnections; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public int getMinPoolSize() { return minPoolSize; }
    }
}