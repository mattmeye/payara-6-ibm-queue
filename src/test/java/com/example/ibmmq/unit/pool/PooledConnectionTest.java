package com.example.ibmmq.unit.pool;

import com.example.ibmmq.pool.PooledConnection;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PooledConnection Comprehensive Tests")
class PooledConnectionTest {

    @Mock
    private Connection mockConnection;

    private PooledConnection pooledConnection;
    private long startTime;

    @BeforeEach
    void setUp() {
        startTime = System.currentTimeMillis();
        pooledConnection = new PooledConnection(mockConnection);
    }

    @Test
    @DisplayName("Should create PooledConnection with correct initial state")
    void shouldCreatePooledConnectionWithCorrectInitialState() {
        // When
        Connection connection = pooledConnection.getConnection();
        long createdTime = pooledConnection.getCreatedTime();
        long lastUsed = pooledConnection.getLastUsed();
        boolean inUse = pooledConnection.isInUse();

        // Then
        assertThat(connection).isEqualTo(mockConnection);
        assertThat(createdTime).isGreaterThanOrEqualTo(startTime);
        assertThat(lastUsed).isEqualTo(createdTime);
        assertThat(inUse).isFalse();
    }

    @Test
    @DisplayName("Should create PooledConnection with null connection")
    void shouldCreatePooledConnectionWithNullConnection() {
        // When
        PooledConnection nullPooledConnection = new PooledConnection(null);

        // Then
        assertThat(nullPooledConnection.getConnection()).isNull();
        assertThat(nullPooledConnection.getCreatedTime()).isGreaterThanOrEqualTo(startTime);
        assertThat(nullPooledConnection.isInUse()).isFalse();
    }

    @Test
    @DisplayName("Should mark connection as active")
    void shouldMarkConnectionAsActive() throws InterruptedException {
        // Given
        long beforeMark = System.currentTimeMillis();
        Thread.sleep(1); // Ensure time difference

        // When
        pooledConnection.markAsActive();

        // Then
        assertThat(pooledConnection.isInUse()).isTrue();
        assertThat(pooledConnection.getLastUsed()).isGreaterThanOrEqualTo(beforeMark);
    }

    @Test
    @DisplayName("Should mark connection as available")
    void shouldMarkConnectionAsAvailable() throws InterruptedException {
        // Given
        pooledConnection.markAsActive();
        Thread.sleep(1); // Ensure time difference
        long beforeMark = System.currentTimeMillis();

        // When
        pooledConnection.markAsAvailable();

        // Then
        assertThat(pooledConnection.isInUse()).isFalse();
        assertThat(pooledConnection.getLastUsed()).isGreaterThanOrEqualTo(beforeMark);
    }

    @Test
    @DisplayName("Should toggle between active and available states")
    void shouldToggleBetweenActiveAndAvailableStates() {
        // Initially not in use
        assertThat(pooledConnection.isInUse()).isFalse();

        // Mark as active
        pooledConnection.markAsActive();
        assertThat(pooledConnection.isInUse()).isTrue();

        // Mark as available
        pooledConnection.markAsAvailable();
        assertThat(pooledConnection.isInUse()).isFalse();

        // Mark as active again
        pooledConnection.markAsActive();
        assertThat(pooledConnection.isInUse()).isTrue();
    }

    @Test
    @DisplayName("Should update lastUsed time when marking as active")
    void shouldUpdateLastUsedTimeWhenMarkingAsActive() throws InterruptedException {
        // Given
        long initialLastUsed = pooledConnection.getLastUsed();
        Thread.sleep(2); // Ensure time difference

        // When
        pooledConnection.markAsActive();

        // Then
        assertThat(pooledConnection.getLastUsed()).isGreaterThan(initialLastUsed);
    }

    @Test
    @DisplayName("Should update lastUsed time when marking as available")
    void shouldUpdateLastUsedTimeWhenMarkingAsAvailable() throws InterruptedException {
        // Given
        long initialLastUsed = pooledConnection.getLastUsed();
        Thread.sleep(2); // Ensure time difference

        // When
        pooledConnection.markAsAvailable();

        // Then
        assertThat(pooledConnection.getLastUsed()).isGreaterThan(initialLastUsed);
    }

    @Test
    @DisplayName("Should calculate age correctly")
    void shouldCalculateAgeCorrectly() throws InterruptedException {
        // Given
        long createdTime = pooledConnection.getCreatedTime();
        Thread.sleep(5); // Wait a bit

        // When
        long age = pooledConnection.getAge();

        // Then
        long expectedMinAge = System.currentTimeMillis() - createdTime;
        assertThat(age).isGreaterThanOrEqualTo(5); // At least the sleep time
        assertThat(age).isLessThanOrEqualTo(expectedMinAge + 10); // Allow some tolerance
    }

    @Test
    @DisplayName("Should calculate idle time correctly")
    void shouldCalculateIdleTimeCorrectly() throws InterruptedException {
        // Given
        long initialLastUsed = pooledConnection.getLastUsed();
        Thread.sleep(5); // Wait a bit

        // When
        long idleTime = pooledConnection.getIdleTime();

        // Then
        long expectedMinIdleTime = System.currentTimeMillis() - initialLastUsed;
        assertThat(idleTime).isGreaterThanOrEqualTo(5); // At least the sleep time
        assertThat(idleTime).isLessThanOrEqualTo(expectedMinIdleTime + 10); // Allow some tolerance
    }

    @Test
    @DisplayName("Should reset idle time when marking as active")
    void shouldResetIdleTimeWhenMarkingAsActive() throws InterruptedException {
        // Given - wait to accumulate idle time
        Thread.sleep(5);
        long idleTimeBeforeMark = pooledConnection.getIdleTime();
        assertThat(idleTimeBeforeMark).isGreaterThanOrEqualTo(5);

        // When - mark as active
        pooledConnection.markAsActive();

        // Then - idle time should be minimal
        long idleTimeAfterMark = pooledConnection.getIdleTime();
        assertThat(idleTimeAfterMark).isLessThan(idleTimeBeforeMark);
        assertThat(idleTimeAfterMark).isLessThan(2); // Should be very small
    }

    @Test
    @DisplayName("Should reset idle time when marking as available")
    void shouldResetIdleTimeWhenMarkingAsAvailable() throws InterruptedException {
        // Given - mark as active then wait
        pooledConnection.markAsActive();
        Thread.sleep(5);
        long idleTimeBeforeMark = pooledConnection.getIdleTime();
        assertThat(idleTimeBeforeMark).isGreaterThanOrEqualTo(5);

        // When - mark as available
        pooledConnection.markAsAvailable();

        // Then - idle time should be minimal
        long idleTimeAfterMark = pooledConnection.getIdleTime();
        assertThat(idleTimeAfterMark).isLessThan(idleTimeBeforeMark);
        assertThat(idleTimeAfterMark).isLessThan(2); // Should be very small
    }

    @Test
    @DisplayName("Should close connection successfully")
    void shouldCloseConnectionSuccessfully() throws JMSException {
        // When
        pooledConnection.close();

        // Then
        verify(mockConnection).close();
    }

    @Test
    @DisplayName("Should handle close with null connection")
    void shouldHandleCloseWithNullConnection() {
        // Given
        PooledConnection nullPooledConnection = new PooledConnection(null);

        // When & Then - Should not throw exception
        assertThatCode(() -> nullPooledConnection.close())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle JMSException during close")
    void shouldHandleJMSExceptionDuringClose() throws JMSException {
        // Given
        doThrow(new JMSException("Connection close failed")).when(mockConnection).close();

        // When & Then - Should propagate the exception
        assertThatThrownBy(() -> pooledConnection.close())
            .isInstanceOf(JMSException.class)
            .hasMessage("Connection close failed");

        verify(mockConnection).close();
    }

    @Test
    @DisplayName("Should maintain state consistency during concurrent access")
    void shouldMaintainStateConsistencyDuringConcurrentAccess() {
        // When - simulate rapid state changes
        for (int i = 0; i < 100; i++) {
            pooledConnection.markAsActive();
            assertThat(pooledConnection.isInUse()).isTrue();

            pooledConnection.markAsAvailable();
            assertThat(pooledConnection.isInUse()).isFalse();
        }

        // Then - final state should be consistent
        assertThat(pooledConnection.isInUse()).isFalse();
        assertThat(pooledConnection.getConnection()).isEqualTo(mockConnection);
    }

    @Test
    @DisplayName("Should have monotonically increasing timestamps")
    void shouldHaveMonotonicallyIncreasingTimestamps() throws InterruptedException {
        // Given
        long createdTime = pooledConnection.getCreatedTime();
        long initialLastUsed = pooledConnection.getLastUsed();

        // When - make changes with delays
        Thread.sleep(1);
        pooledConnection.markAsActive();
        long lastUsedAfterActive = pooledConnection.getLastUsed();

        Thread.sleep(1);
        pooledConnection.markAsAvailable();
        long lastUsedAfterAvailable = pooledConnection.getLastUsed();

        // Then - timestamps should be increasing
        assertThat(initialLastUsed).isEqualTo(createdTime); // Initial state
        assertThat(lastUsedAfterActive).isGreaterThan(initialLastUsed);
        assertThat(lastUsedAfterAvailable).isGreaterThanOrEqualTo(lastUsedAfterActive);
    }

    @Test
    @DisplayName("Should handle multiple consecutive state changes")
    void shouldHandleMultipleConsecutiveStateChanges() {
        // When - multiple consecutive active marks
        pooledConnection.markAsActive();
        long firstActiveTime = pooledConnection.getLastUsed();
        boolean firstActiveState = pooledConnection.isInUse();

        pooledConnection.markAsActive();
        long secondActiveTime = pooledConnection.getLastUsed();
        boolean secondActiveState = pooledConnection.isInUse();

        // When - multiple consecutive available marks
        pooledConnection.markAsAvailable();
        long firstAvailableTime = pooledConnection.getLastUsed();
        boolean firstAvailableState = pooledConnection.isInUse();

        pooledConnection.markAsAvailable();
        long secondAvailableTime = pooledConnection.getLastUsed();
        boolean secondAvailableState = pooledConnection.isInUse();

        // Then - state should be consistent
        assertThat(firstActiveState).isTrue();
        assertThat(secondActiveState).isTrue();
        assertThat(firstAvailableState).isFalse();
        assertThat(secondAvailableState).isFalse();

        // Timestamps should be updated even for consecutive calls
        assertThat(secondActiveTime).isGreaterThanOrEqualTo(firstActiveTime);
        assertThat(firstAvailableTime).isGreaterThanOrEqualTo(secondActiveTime);
        assertThat(secondAvailableTime).isGreaterThanOrEqualTo(firstAvailableTime);
    }

    @Test
    @DisplayName("Should maintain immutable created time")
    void shouldMaintainImmutableCreatedTime() throws InterruptedException {
        // Given
        long originalCreatedTime = pooledConnection.getCreatedTime();

        // When - perform various operations
        Thread.sleep(1);
        pooledConnection.markAsActive();
        Thread.sleep(1);
        pooledConnection.markAsAvailable();

        // Then - created time should remain unchanged
        assertThat(pooledConnection.getCreatedTime()).isEqualTo(originalCreatedTime);
    }

    @Test
    @DisplayName("Should provide accurate time calculations")
    void shouldProvideAccurateTimeCalculations() throws InterruptedException {
        // Given
        long startTime = System.currentTimeMillis();
        Thread.sleep(10); // Age accumulation

        // When
        pooledConnection.markAsActive();
        Thread.sleep(5); // Idle time accumulation

        long age = pooledConnection.getAge();
        long idleTime = pooledConnection.getIdleTime();
        long currentTime = System.currentTimeMillis();

        // Then
        assertThat(age).isGreaterThanOrEqualTo(15); // At least both sleep periods
        assertThat(idleTime).isGreaterThanOrEqualTo(5); // At least second sleep
        assertThat(age).isLessThanOrEqualTo(currentTime - startTime + 10); // Allow tolerance
        assertThat(idleTime).isLessThan(age); // Idle time should be less than age
    }
}