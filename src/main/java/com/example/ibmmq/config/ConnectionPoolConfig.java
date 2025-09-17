package com.example.ibmmq.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConnectionPoolConfig {

    @ConfigProperty(name = "ibm.mq.pool.initial.size", defaultValue = "5")
    private int initialPoolSize;

    @ConfigProperty(name = "ibm.mq.pool.max.size", defaultValue = "50")
    private int maxPoolSize;

    @ConfigProperty(name = "ibm.mq.pool.min.size", defaultValue = "5")
    private int minPoolSize;

    @ConfigProperty(name = "ibm.mq.pool.max.idle.time", defaultValue = "300000")
    private long maxIdleTime;

    @ConfigProperty(name = "ibm.mq.pool.max.wait.time", defaultValue = "30000")
    private long maxWaitTime;

    @ConfigProperty(name = "ibm.mq.pool.validation.enabled", defaultValue = "true")
    private boolean validationEnabled;

    @ConfigProperty(name = "ibm.mq.pool.validation.timeout", defaultValue = "5000")
    private long validationTimeout;

    @ConfigProperty(name = "ibm.mq.pool.retry.attempts", defaultValue = "3")
    private int retryAttempts;

    @ConfigProperty(name = "ibm.mq.pool.retry.interval", defaultValue = "1000")
    private long retryInterval;

    @ConfigProperty(name = "ibm.mq.connection.heartbeat.interval", defaultValue = "300")
    private int heartbeatInterval;

    @ConfigProperty(name = "ibm.mq.connection.receive.timeout", defaultValue = "15000")
    private long receiveTimeout;

    @ConfigProperty(name = "ibm.mq.connection.send.timeout", defaultValue = "15000")
    private long sendTimeout;

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public long getValidationTimeout() {
        return validationTimeout;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public long getReceiveTimeout() {
        return receiveTimeout;
    }

    public long getSendTimeout() {
        return sendTimeout;
    }
}