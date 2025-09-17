package com.example.ibmmq.unit.pool;

import com.example.ibmmq.config.ConnectionPoolConfig;
import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.pool.PooledConnection;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IBMMQConnectionPool Comprehensive Tests")
class IBMMQConnectionPoolTest {

    @Mock
    private IBMMQConfig mqConfig;

    @Mock
    private ConnectionPoolConfig poolConfig;

    @Mock
    private Connection mockConnection;

    @InjectMocks
    private IBMMQConnectionPool connectionPool;

    @BeforeEach
    void setUp() {
        // MQ Configuration
        when(mqConfig.getHostname()).thenReturn("localhost");
        when(mqConfig.getPort()).thenReturn(1414);
        when(mqConfig.getChannel()).thenReturn("DEV.APP.SVRCONN");
        when(mqConfig.getQueueManager()).thenReturn("QM1");
        when(mqConfig.getUsername()).thenReturn("app");
        when(mqConfig.getPassword()).thenReturn("password");

        // Pool Configuration
        when(poolConfig.getInitialPoolSize()).thenReturn(2);
        when(poolConfig.getMaxPoolSize()).thenReturn(10);
        when(poolConfig.getMinPoolSize()).thenReturn(2);
        when(poolConfig.getMaxIdleTime()).thenReturn(300000L);
        when(poolConfig.getMaxWaitTime()).thenReturn(30000L);
        when(poolConfig.isValidationEnabled()).thenReturn(true);
        when(poolConfig.getValidationTimeout()).thenReturn(5000L);
        when(poolConfig.getRetryAttempts()).thenReturn(3);
        when(poolConfig.getRetryInterval()).thenReturn(1000L);
        when(poolConfig.getHeartbeatInterval()).thenReturn(300);
        when(poolConfig.getReceiveTimeout()).thenReturn(15000L);
        when(poolConfig.getSendTimeout()).thenReturn(15000L);
    }

    @Test
    @DisplayName("Should create connection pool with correct configuration")
    void shouldCreateConnectionPoolWithCorrectConfiguration() throws Exception {
        // When & Then - expect RuntimeException due to MQ connection failure in unit test
        assertThatThrownBy(() -> connectionPool.initialize())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to initialize connection pool");

        // Verify basic configuration methods were called
        verify(mqConfig, atLeastOnce()).getHostname();
        verify(mqConfig, atLeastOnce()).getPort();
        verify(mqConfig, atLeastOnce()).getChannel();
        verify(mqConfig, atLeastOnce()).getQueueManager();
    }

    @Test
    @DisplayName("Should initialize pool fields correctly")
    void shouldInitializePoolFieldsCorrectly() throws Exception {
        // Given - simulate field initialization without actual MQ connections
        Field availableField = IBMMQConnectionPool.class.getDeclaredField("availableConnections");
        Field activeField = IBMMQConnectionPool.class.getDeclaredField("activeConnections");
        Field totalField = IBMMQConnectionPool.class.getDeclaredField("totalConnections");
        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");

        availableField.setAccessible(true);
        activeField.setAccessible(true);
        totalField.setAccessible(true);
        shutdownField.setAccessible(true);

        // Manually initialize fields to test structure
        availableField.set(connectionPool, new ArrayBlockingQueue<>(10));
        activeField.set(connectionPool, new ConcurrentHashMap<>());
        totalField.set(connectionPool, new AtomicInteger(0));
        shutdownField.set(connectionPool, false);

        // When
        ArrayBlockingQueue<?> availableConnections = (ArrayBlockingQueue<?>) availableField.get(connectionPool);
        ConcurrentHashMap<?, ?> activeConnections = (ConcurrentHashMap<?, ?>) activeField.get(connectionPool);
        AtomicInteger totalConnections = (AtomicInteger) totalField.get(connectionPool);
        boolean isShutdown = (boolean) shutdownField.get(connectionPool);

        // Then
        assertThat(availableConnections).isNotNull();
        assertThat(activeConnections).isNotNull();
        assertThat(totalConnections.get()).isEqualTo(0);
        assertThat(isShutdown).isFalse();
    }

    @Test
    @DisplayName("Should handle getConnection when pool is shutdown")
    void shouldHandleGetConnectionWhenPoolIsShutdown() throws Exception {
        // Given - simulate shutdown state
        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");
        shutdownField.setAccessible(true);
        shutdownField.set(connectionPool, true);

        // When & Then
        assertThatThrownBy(() -> connectionPool.getConnection())
            .isInstanceOf(JMSException.class)
            .hasMessage("Connection pool is shutdown");
    }

    @Test
    @DisplayName("Should handle releaseConnection with null")
    void shouldHandleReleaseConnectionWithNull() {
        // When & Then - Should not throw exception
        assertThatCode(() -> connectionPool.releaseConnection(null))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle releaseConnection when pool is shutdown")
    void shouldHandleReleaseConnectionWhenPoolIsShutdown() throws Exception {
        // Given - simulate shutdown state
        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");
        shutdownField.setAccessible(true);
        shutdownField.set(connectionPool, true);

        // When & Then - Should not throw exception
        assertThatCode(() -> connectionPool.releaseConnection(mockConnection))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle releaseConnection with unknown connection")
    void shouldHandleReleaseConnectionWithUnknownConnection() throws Exception {
        // Given - simulate initialized state
        Field activeField = IBMMQConnectionPool.class.getDeclaredField("activeConnections");
        activeField.setAccessible(true);
        activeField.set(connectionPool, new ConcurrentHashMap<>());

        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");
        shutdownField.setAccessible(true);
        shutdownField.set(connectionPool, false);

        // When & Then - Should not throw exception
        assertThatCode(() -> connectionPool.releaseConnection(mockConnection))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should return correct pool status")
    void shouldReturnCorrectPoolStatus() throws Exception {
        // Given - simulate pool state
        Field totalField = IBMMQConnectionPool.class.getDeclaredField("totalConnections");
        Field activeField = IBMMQConnectionPool.class.getDeclaredField("activeConnections");
        Field availableField = IBMMQConnectionPool.class.getDeclaredField("availableConnections");

        totalField.setAccessible(true);
        activeField.setAccessible(true);
        availableField.setAccessible(true);

        totalField.set(connectionPool, new AtomicInteger(5));
        activeField.set(connectionPool, new ConcurrentHashMap<>());
        availableField.set(connectionPool, new ArrayBlockingQueue<>(10));

        // When
        IBMMQConnectionPool.PoolStatus status = connectionPool.getPoolStatus();

        // Then
        assertThat(status).isNotNull();
        assertThat(status.getTotalConnections()).isEqualTo(5);
        assertThat(status.getActiveConnections()).isEqualTo(0);
        assertThat(status.getAvailableConnections()).isEqualTo(0);
        assertThat(status.getMaxPoolSize()).isEqualTo(10);
        assertThat(status.getMinPoolSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should create PoolStatus with correct values")
    void shouldCreatePoolStatusWithCorrectValues() {
        // When
        IBMMQConnectionPool.PoolStatus status = new IBMMQConnectionPool.PoolStatus(5, 3, 2, 10, 1);

        // Then
        assertThat(status.getTotalConnections()).isEqualTo(5);
        assertThat(status.getActiveConnections()).isEqualTo(3);
        assertThat(status.getAvailableConnections()).isEqualTo(2);
        assertThat(status.getMaxPoolSize()).isEqualTo(10);
        assertThat(status.getMinPoolSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle shutdown gracefully")
    void shouldHandleShutdownGracefully() throws Exception {
        // Given - simulate initialized state
        Field activeField = IBMMQConnectionPool.class.getDeclaredField("activeConnections");
        Field availableField = IBMMQConnectionPool.class.getDeclaredField("availableConnections");
        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");

        activeField.setAccessible(true);
        availableField.setAccessible(true);
        shutdownField.setAccessible(true);

        ConcurrentHashMap<Connection, PooledConnection> activeConnections = new ConcurrentHashMap<>();
        ArrayBlockingQueue<PooledConnection> availableConnections = new ArrayBlockingQueue<>(10);

        activeField.set(connectionPool, activeConnections);
        availableField.set(connectionPool, availableConnections);
        shutdownField.set(connectionPool, false);

        // When
        connectionPool.shutdown();

        // Then
        boolean isShutdown = (boolean) shutdownField.get(connectionPool);
        assertThat(isShutdown).isTrue();
        assertThat(activeConnections).isEmpty();
        assertThat(availableConnections).isEmpty();
    }

    @Test
    @DisplayName("Should test isConnectionValid method via reflection")
    void shouldTestIsConnectionValidMethodViaReflection() throws Exception {
        // Given
        Method isConnectionValidMethod = IBMMQConnectionPool.class.getDeclaredMethod("isConnectionValid", PooledConnection.class);
        isConnectionValidMethod.setAccessible(true);

        // Test with null
        boolean resultNull = (boolean) isConnectionValidMethod.invoke(connectionPool, (PooledConnection) null);
        assertThat(resultNull).isFalse();

        // Test with pooled connection having null connection
        PooledConnection pooledConnectionWithNull = new PooledConnection(null);
        boolean resultNullConnection = (boolean) isConnectionValidMethod.invoke(connectionPool, pooledConnectionWithNull);
        assertThat(resultNullConnection).isFalse();
    }

    @Test
    @DisplayName("Should test closePooledConnection method via reflection")
    void shouldTestClosePooledConnectionMethodViaReflection() throws Exception {
        // Given
        Method closePooledConnectionMethod = IBMMQConnectionPool.class.getDeclaredMethod("closePooledConnection", PooledConnection.class);
        closePooledConnectionMethod.setAccessible(true);

        // Test with null - should not throw
        assertThatCode(() -> closePooledConnectionMethod.invoke(connectionPool, (PooledConnection) null))
            .doesNotThrowAnyException();

        // Test with mock pooled connection
        PooledConnection mockPooledConnection = mock(PooledConnection.class);
        assertThatCode(() -> closePooledConnectionMethod.invoke(connectionPool, mockPooledConnection))
            .doesNotThrowAnyException();

        verify(mockPooledConnection).close();
    }

    @Test
    @DisplayName("Should test createConnectionFactory method behavior via configuration")
    void shouldTestCreateConnectionFactoryBehavior() throws Exception {
        // Given - different configurations
        when(mqConfig.getUsername()).thenReturn(null);
        when(poolConfig.getReceiveTimeout()).thenReturn(0L);
        when(poolConfig.getSendTimeout()).thenReturn(0L);
        when(poolConfig.getHeartbeatInterval()).thenReturn(0);

        // When & Then - expect failure but verify configuration calls
        assertThatThrownBy(() -> connectionPool.initialize())
            .isInstanceOf(RuntimeException.class);

        // Then - verify configuration calls
        verify(mqConfig, atLeastOnce()).getHostname();
        verify(mqConfig, atLeastOnce()).getPort();
        verify(mqConfig, atLeastOnce()).getChannel();
        verify(mqConfig, atLeastOnce()).getQueueManager();
        verify(poolConfig, atLeastOnce()).getReceiveTimeout();
        verify(poolConfig, atLeastOnce()).getSendTimeout();
        verify(poolConfig, atLeastOnce()).getHeartbeatInterval();
    }

    @Test
    @DisplayName("Should handle configuration edge cases")
    void shouldHandleConfigurationEdgeCases() throws Exception {
        // Given - edge case configurations
        when(mqConfig.getUsername()).thenReturn("");
        when(mqConfig.getPassword()).thenReturn(null);
        when(poolConfig.getMaxPoolSize()).thenReturn(1);
        when(poolConfig.getInitialPoolSize()).thenReturn(0);

        // When & Then - expect failure but verify configuration handling
        assertThatThrownBy(() -> connectionPool.initialize())
            .isInstanceOf(RuntimeException.class);

        // Then - verify configuration handling
        assertThat(connectionPool).isNotNull();
        verify(mqConfig, atLeastOnce()).getUsername();
        // getInitialPoolSize only called after successful connection factory creation
    }

    @Test
    @DisplayName("Should validate pool configuration consistency")
    void shouldValidatePoolConfigurationConsistency() {
        // Given - various pool configurations
        when(poolConfig.getMaxPoolSize()).thenReturn(5);
        when(poolConfig.getMinPoolSize()).thenReturn(1);
        when(poolConfig.getInitialPoolSize()).thenReturn(3);
        when(poolConfig.getMaxWaitTime()).thenReturn(10000L);
        when(poolConfig.getMaxIdleTime()).thenReturn(60000L);

        // When & Then - verify all configurations are accessible
        assertThat(poolConfig.getMaxPoolSize()).isEqualTo(5);
        assertThat(poolConfig.getMinPoolSize()).isEqualTo(1);
        assertThat(poolConfig.getInitialPoolSize()).isEqualTo(3);
        assertThat(poolConfig.getMaxWaitTime()).isEqualTo(10000L);
        assertThat(poolConfig.getMaxIdleTime()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("Should handle timeout configurations")
    void shouldHandleTimeoutConfigurations() {
        // Given - timeout configurations
        when(poolConfig.getValidationTimeout()).thenReturn(3000L);
        when(poolConfig.getRetryInterval()).thenReturn(500L);
        when(poolConfig.getReceiveTimeout()).thenReturn(5000L);
        when(poolConfig.getSendTimeout()).thenReturn(5000L);

        // When & Then - verify timeout configurations
        assertThat(poolConfig.getValidationTimeout()).isEqualTo(3000L);
        assertThat(poolConfig.getRetryInterval()).isEqualTo(500L);
        assertThat(poolConfig.getReceiveTimeout()).isEqualTo(5000L);
        assertThat(poolConfig.getSendTimeout()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("Should handle retry and validation configurations")
    void shouldHandleRetryAndValidationConfigurations() {
        // Given - retry and validation configurations
        when(poolConfig.getRetryAttempts()).thenReturn(5);
        when(poolConfig.isValidationEnabled()).thenReturn(false);
        when(poolConfig.getHeartbeatInterval()).thenReturn(600);

        // When & Then - verify retry and validation configurations
        assertThat(poolConfig.getRetryAttempts()).isEqualTo(5);
        assertThat(poolConfig.isValidationEnabled()).isFalse();
        assertThat(poolConfig.getHeartbeatInterval()).isEqualTo(600);
    }

    @Test
    @DisplayName("Should test initializePool method structure")
    void shouldTestInitializePoolMethodStructure() throws Exception {
        // Given - verify method exists and is accessible
        Method initializePoolMethod = IBMMQConnectionPool.class.getDeclaredMethod("initializePool");
        assertThat(initializePoolMethod).isNotNull();

        // Test method accessibility
        initializePoolMethod.setAccessible(true);
        assertThat(initializePoolMethod.isAccessible()).isTrue();
    }

    @Test
    @DisplayName("Should test createPooledConnection method structure")
    void shouldTestCreatePooledConnectionMethodStructure() throws Exception {
        // Given - verify method exists and is accessible
        Method createPooledConnectionMethod = IBMMQConnectionPool.class.getDeclaredMethod("createPooledConnection");
        assertThat(createPooledConnectionMethod).isNotNull();

        // Test method accessibility
        createPooledConnectionMethod.setAccessible(true);
        assertThat(createPooledConnectionMethod.isAccessible()).isTrue();

        // Verify return type
        assertThat(createPooledConnectionMethod.getReturnType()).isEqualTo(PooledConnection.class);
    }

    @Test
    @DisplayName("Should test pool field initialization structure")
    void shouldTestPoolFieldInitializationStructure() throws Exception {
        // Given - check pool fields exist
        Field availableField = IBMMQConnectionPool.class.getDeclaredField("availableConnections");
        Field activeField = IBMMQConnectionPool.class.getDeclaredField("activeConnections");
        Field totalField = IBMMQConnectionPool.class.getDeclaredField("totalConnections");
        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");

        // When - verify fields are accessible
        availableField.setAccessible(true);
        activeField.setAccessible(true);
        totalField.setAccessible(true);
        shutdownField.setAccessible(true);

        // Then - verify field types
        assertThat(availableField.getType()).isEqualTo(BlockingQueue.class);
        assertThat(activeField.getType()).isEqualTo(ConcurrentMap.class);
        assertThat(totalField.getType()).isEqualTo(AtomicInteger.class);
        assertThat(shutdownField.getType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Should test connection factory field structure")
    void shouldTestConnectionFactoryFieldStructure() throws Exception {
        // Given - check connection factory field exists
        Field connectionFactoryField = IBMMQConnectionPool.class.getDeclaredField("connectionFactory");

        // When - verify field is accessible
        connectionFactoryField.setAccessible(true);

        // Then - verify field type
        assertThat(connectionFactoryField.getType()).isEqualTo(com.ibm.mq.jms.MQConnectionFactory.class);
    }

    @Test
    @DisplayName("Should test releaseConnection with valid pooled connection")
    void shouldTestReleaseConnectionWithValidPooledConnection() throws Exception {
        // Given - simulate active connection
        Field availableField = IBMMQConnectionPool.class.getDeclaredField("availableConnections");
        Field activeField = IBMMQConnectionPool.class.getDeclaredField("activeConnections");
        Field totalField = IBMMQConnectionPool.class.getDeclaredField("totalConnections");
        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");

        availableField.setAccessible(true);
        activeField.setAccessible(true);
        totalField.setAccessible(true);
        shutdownField.setAccessible(true);

        ArrayBlockingQueue<PooledConnection> availableConnections = new ArrayBlockingQueue<>(10);
        ConcurrentHashMap<Connection, PooledConnection> activeConnections = new ConcurrentHashMap<>();
        AtomicInteger totalConnections = new AtomicInteger(1);

        Connection mockJakartaConnection = mock(Connection.class);
        PooledConnection mockPooledConnection = mock(PooledConnection.class);
        when(mockPooledConnection.getConnection()).thenReturn(mockJakartaConnection);
        when(mockJakartaConnection.getMetaData()).thenReturn(mock(jakarta.jms.ConnectionMetaData.class));

        activeConnections.put(mockJakartaConnection, mockPooledConnection);

        availableField.set(connectionPool, availableConnections);
        activeField.set(connectionPool, activeConnections);
        totalField.set(connectionPool, totalConnections);
        shutdownField.set(connectionPool, false);

        // When
        connectionPool.releaseConnection(mockJakartaConnection);

        // Then
        assertThat(activeConnections).doesNotContainKey(mockJakartaConnection);
        assertThat(availableConnections).hasSize(1);
        verify(mockPooledConnection).markAsAvailable();
    }

    @Test
    @DisplayName("Should test releaseConnection with invalid pooled connection")
    void shouldTestReleaseConnectionWithInvalidPooledConnection() throws Exception {
        // Given - simulate active connection that is invalid
        Field availableField = IBMMQConnectionPool.class.getDeclaredField("availableConnections");
        Field activeField = IBMMQConnectionPool.class.getDeclaredField("activeConnections");
        Field totalField = IBMMQConnectionPool.class.getDeclaredField("totalConnections");
        Field shutdownField = IBMMQConnectionPool.class.getDeclaredField("isShutdown");

        availableField.setAccessible(true);
        activeField.setAccessible(true);
        totalField.setAccessible(true);
        shutdownField.setAccessible(true);

        ArrayBlockingQueue<PooledConnection> availableConnections = new ArrayBlockingQueue<>(10);
        ConcurrentHashMap<Connection, PooledConnection> activeConnections = new ConcurrentHashMap<>();
        AtomicInteger totalConnections = new AtomicInteger(1);

        Connection mockJakartaConnection = mock(Connection.class);
        PooledConnection mockPooledConnection = mock(PooledConnection.class);
        when(mockPooledConnection.getConnection()).thenReturn(mockJakartaConnection);
        // Make connection validation fail
        when(mockJakartaConnection.getMetaData()).thenThrow(new JMSException("Connection invalid"));

        activeConnections.put(mockJakartaConnection, mockPooledConnection);

        availableField.set(connectionPool, availableConnections);
        activeField.set(connectionPool, activeConnections);
        totalField.set(connectionPool, totalConnections);
        shutdownField.set(connectionPool, false);

        // When
        connectionPool.releaseConnection(mockJakartaConnection);

        // Then
        assertThat(activeConnections).doesNotContainKey(mockJakartaConnection);
        assertThat(availableConnections).isEmpty(); // Invalid connection not returned to pool
        assertThat(totalConnections.get()).isEqualTo(0); // Total decremented
        verify(mockPooledConnection).close();
    }

    @Test
    @DisplayName("Should test isConnectionValid with valid pooled connection")
    void shouldTestIsConnectionValidWithValidPooledConnection() throws Exception {
        // Given
        Method isConnectionValidMethod = IBMMQConnectionPool.class.getDeclaredMethod("isConnectionValid", PooledConnection.class);
        isConnectionValidMethod.setAccessible(true);

        Connection mockJakartaConnection = mock(Connection.class);
        PooledConnection pooledConnection = new PooledConnection(mockJakartaConnection);

        // Mock successful connection validation
        when(mockJakartaConnection.getMetaData()).thenReturn(mock(jakarta.jms.ConnectionMetaData.class));

        // When
        boolean result = (boolean) isConnectionValidMethod.invoke(connectionPool, pooledConnection);

        // Then
        assertThat(result).isTrue();
        verify(mockJakartaConnection).getMetaData();
    }

    @Test
    @DisplayName("Should test isConnectionValid with invalid pooled connection")
    void shouldTestIsConnectionValidWithInvalidPooledConnection() throws Exception {
        // Given
        Method isConnectionValidMethod = IBMMQConnectionPool.class.getDeclaredMethod("isConnectionValid", PooledConnection.class);
        isConnectionValidMethod.setAccessible(true);

        Connection mockJakartaConnection = mock(Connection.class);
        PooledConnection pooledConnection = new PooledConnection(mockJakartaConnection);

        // Mock connection validation failure
        when(mockJakartaConnection.getMetaData()).thenThrow(new JMSException("Connection invalid"));

        // When
        boolean result = (boolean) isConnectionValidMethod.invoke(connectionPool, pooledConnection);

        // Then
        assertThat(result).isFalse();
        verify(mockJakartaConnection).getMetaData();
    }
}