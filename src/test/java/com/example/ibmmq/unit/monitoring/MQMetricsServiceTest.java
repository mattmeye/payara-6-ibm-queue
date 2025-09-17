package com.example.ibmmq.unit.monitoring;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.monitoring.MQMetricsService;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.repository.MQMessageRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
@DisplayName("MQMetricsService Tests")
class MQMetricsServiceTest {

    private MeterRegistry meterRegistry;

    @Mock
    private MQMessageRepository messageRepository;

    @Mock
    private IBMMQConnectionPool connectionPool;

    private MQMetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MQMetricsService();
        // Use reflection to set the injected fields
        try {
            java.lang.reflect.Field registryField = MQMetricsService.class.getDeclaredField("meterRegistry");
            registryField.setAccessible(true);
            registryField.set(metricsService, meterRegistry);

            java.lang.reflect.Field repoField = MQMetricsService.class.getDeclaredField("messageRepository");
            repoField.setAccessible(true);
            repoField.set(metricsService, messageRepository);

            java.lang.reflect.Field poolField = MQMetricsService.class.getDeclaredField("connectionPool");
            poolField.setAccessible(true);
            poolField.set(metricsService, connectionPool);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test", e);
        }
    }

    @Test
    @DisplayName("Should initialize metrics successfully")
    void shouldInitializeMetricsSuccessfully() {
        // When
        metricsService.initialize();

        // Then - verify metrics were registered
        assertThat(meterRegistry.getMeters()).isNotEmpty();
    }

    @Test
    @DisplayName("Should record message sent metric")
    void shouldRecordMessageSentMetric() {
        // Given
        String queueName = "TEST.QUEUE";
        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> metricsService.recordMessageSent(queueName))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should record message received metric")
    void shouldRecordMessageReceivedMetric() {
        // Given
        String queueName = "RECEIVE.QUEUE";
        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> metricsService.recordMessageReceived(queueName))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should record message failed metric")
    void shouldRecordMessageFailedMetric() {
        // Given
        String queueName = "FAILED.QUEUE";
        String errorType = "validation_error";
        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> metricsService.recordMessageFailed(queueName, errorType))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should record message processed metric")
    void shouldRecordMessageProcessedMetric() {
        // Given
        String queueName = "PROCESSED.QUEUE";
        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> metricsService.recordMessageProcessed(queueName))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should start and stop message processing timer")
    void shouldStartAndStopMessageProcessingTimer() {
        // Given
        String queueName = "TIMER.QUEUE";
        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> {
            Timer.Sample sample = metricsService.startMessageProcessingTimer();
            metricsService.stopMessageProcessingTimer(sample, queueName, "SUCCESS");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should start and stop batch job timer")
    void shouldStartAndStopBatchJobTimer() {
        // Given
        String jobName = "test-job";
        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> {
            Timer.Sample sample = metricsService.startBatchJobTimer();
            metricsService.stopBatchJobTimer(sample, jobName, "COMPLETED");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should record batch job metrics")
    void shouldRecordBatchJobMetrics() {
        // Given
        String jobName = "test-batch-job";
        long itemsRead = 100L;
        long itemsWritten = 95L;
        long itemsSkipped = 5L;
        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> metricsService.recordBatchJobMetrics(jobName, itemsRead, itemsWritten, itemsSkipped))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should get application metrics")
    void shouldGetApplicationMetrics() {
        // Given
        when(messageRepository.countByStatus(MQMessage.MessageStatus.FAILED)).thenReturn(2L);

        IBMMQConnectionPool.PoolStatus poolStatus = new IBMMQConnectionPool.PoolStatus(10, 2, 5, 15, 5);
        when(connectionPool.getPoolStatus()).thenReturn(poolStatus);

        metricsService.initialize();

        // When & Then - verify it doesn't throw an exception
        assertThatCode(() -> {
            MQMetricsService.ApplicationMetrics metrics = metricsService.getApplicationMetrics();
            assertThat(metrics).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle metric recording without errors")
    void shouldHandleMetricRecordingWithoutErrors() {
        // Given
        metricsService.initialize();

        // When & Then - verify all metric operations work without errors
        assertThatCode(() -> {
            metricsService.recordMessageSent("QUEUE.A");
            metricsService.recordMessageSent("QUEUE.B");
            metricsService.recordMessageReceived("QUEUE.A");
            metricsService.recordMessageFailed("QUEUE.C", "error");
            metricsService.recordMessageProcessed("QUEUE.D");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle message processing lifecycle")
    void shouldHandleMessageProcessingLifecycle() {
        // Given
        metricsService.initialize();
        String messageId = "test-msg-123";
        String queueName = "LIFECYCLE.QUEUE";

        // When & Then - verify lifecycle methods work without errors
        assertThatCode(() -> {
            metricsService.startMessageProcessing(messageId);
            metricsService.endMessageProcessing(messageId, queueName, "SUCCESS");
        }).doesNotThrowAnyException();
    }
}