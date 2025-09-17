package com.example.ibmmq.unit.rest;

import com.example.ibmmq.monitoring.MQMetricsService;
import com.example.ibmmq.rest.MetricsResource;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MetricsResource Tests")
class MetricsResourceTest {

    @Mock
    private MQMetricsService metricsService;

    @Mock
    private PrometheusMeterRegistry meterRegistry;

    @InjectMocks
    private MetricsResource metricsResource;

    @Mock
    private MQMetricsService.ApplicationMetrics applicationMetrics;

    @BeforeEach
    void setUp() {
        // Setup application metrics mock
        when(applicationMetrics.getMessagesSent()).thenReturn(100.0);
        when(applicationMetrics.getMessagesReceived()).thenReturn(95.0);
        when(applicationMetrics.getMessagesFailed()).thenReturn(5.0);
        when(applicationMetrics.getMessagesProcessed()).thenReturn(90.0);
        when(applicationMetrics.getActiveConnections()).thenReturn(3);
        when(applicationMetrics.getAvailableConnections()).thenReturn(7);
        when(applicationMetrics.getTotalConnections()).thenReturn(10);
        when(applicationMetrics.getActiveBatchJobs()).thenReturn(2L);
        when(applicationMetrics.getTotalBatchJobs()).thenReturn(15L);
        when(applicationMetrics.getPendingMessages()).thenReturn(5.0);
        when(applicationMetrics.getFailedMessages()).thenReturn(2.0);
    }

    @Test
    @DisplayName("Should get Prometheus metrics successfully")
    void shouldGetPrometheusMetricsSuccessfully() {
        // Given
        String prometheusMetrics = """
            # HELP mq_messages_sent_total Total number of messages sent to MQ
            # TYPE mq_messages_sent_total counter
            mq_messages_sent_total 100.0
            # HELP mq_messages_received_total Total number of messages received from MQ
            # TYPE mq_messages_received_total counter
            mq_messages_received_total 95.0
            """;
        when(meterRegistry.scrape()).thenReturn(prometheusMetrics);

        // When
        Response response = metricsResource.getPrometheusMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("mq_messages_sent_total 100.0");
        assertThat(response.getEntity().toString()).contains("mq_messages_received_total 95.0");
        verify(meterRegistry).scrape();
    }

    @Test
    @DisplayName("Should handle Prometheus metrics scraping failure")
    void shouldHandlePrometheusMetricsScrapingFailure() {
        // Given
        String errorMessage = "Metrics registry unavailable";
        when(meterRegistry.scrape()).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = metricsResource.getPrometheusMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("Failed to retrieve metrics");
        verify(meterRegistry).scrape();
    }

    @Test
    @DisplayName("Should get application metrics successfully")
    void shouldGetApplicationMetricsSuccessfully() {
        // Given
        when(metricsService.getApplicationMetrics()).thenReturn(applicationMetrics);

        // When
        Response response = metricsResource.getApplicationMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();

        // Verify all metrics are included in JSON response
        assertThat(entity).contains("\"messagesSent\":100");
        assertThat(entity).contains("\"messagesReceived\":95");
        assertThat(entity).contains("\"messagesFailed\":5");
        assertThat(entity).contains("\"messagesProcessed\":90");
        assertThat(entity).contains("\"activeConnections\":3");
        assertThat(entity).contains("\"availableConnections\":7");
        assertThat(entity).contains("\"totalConnections\":10");
        assertThat(entity).contains("\"activeBatchJobs\":2");
        assertThat(entity).contains("\"totalBatchJobs\":15");
        assertThat(entity).contains("\"pendingMessages\":5");
        assertThat(entity).contains("\"failedMessages\":2");

        verify(metricsService).getApplicationMetrics();
    }

    @Test
    @DisplayName("Should handle application metrics service failure")
    void shouldHandleApplicationMetricsServiceFailure() {
        // Given
        String errorMessage = "Metrics collection failed";
        when(metricsService.getApplicationMetrics()).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = metricsResource.getApplicationMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("error");
        assertThat(entity).contains(errorMessage);
        verify(metricsService).getApplicationMetrics();
    }

    @Test
    @DisplayName("Should return healthy status for health check")
    void shouldReturnHealthyStatusForHealthCheck() {
        // When
        Response response = metricsResource.healthCheck();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("healthy");
        assertThat(entity).contains("Metrics Service");
    }

    @Test
    @DisplayName("Should handle zero values in application metrics")
    void shouldHandleZeroValuesInApplicationMetrics() {
        // Given
        when(applicationMetrics.getMessagesSent()).thenReturn(0.0);
        when(applicationMetrics.getMessagesReceived()).thenReturn(0.0);
        when(applicationMetrics.getMessagesFailed()).thenReturn(0.0);
        when(applicationMetrics.getMessagesProcessed()).thenReturn(0.0);
        when(applicationMetrics.getActiveConnections()).thenReturn(0);
        when(applicationMetrics.getAvailableConnections()).thenReturn(0);
        when(applicationMetrics.getTotalConnections()).thenReturn(0);
        when(applicationMetrics.getActiveBatchJobs()).thenReturn(0L);
        when(applicationMetrics.getTotalBatchJobs()).thenReturn(0L);
        when(applicationMetrics.getPendingMessages()).thenReturn(0.0);
        when(applicationMetrics.getFailedMessages()).thenReturn(0.0);

        when(metricsService.getApplicationMetrics()).thenReturn(applicationMetrics);

        // When
        Response response = metricsResource.getApplicationMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();

        // Verify all metrics show zero values
        assertThat(entity).contains("\"messagesSent\":0");
        assertThat(entity).contains("\"messagesReceived\":0");
        assertThat(entity).contains("\"messagesFailed\":0");
        assertThat(entity).contains("\"messagesProcessed\":0");
        assertThat(entity).contains("\"activeConnections\":0");
        assertThat(entity).contains("\"availableConnections\":0");
        assertThat(entity).contains("\"totalConnections\":0");
        assertThat(entity).contains("\"activeBatchJobs\":0");
        assertThat(entity).contains("\"totalBatchJobs\":0");
        assertThat(entity).contains("\"pendingMessages\":0");
        assertThat(entity).contains("\"failedMessages\":0");
    }

    @Test
    @DisplayName("Should handle large values in application metrics")
    void shouldHandleLargeValuesInApplicationMetrics() {
        // Given
        when(applicationMetrics.getMessagesSent()).thenReturn(999999999.0);
        when(applicationMetrics.getMessagesReceived()).thenReturn(888888888.0);
        when(applicationMetrics.getMessagesFailed()).thenReturn(77777777.0);
        when(applicationMetrics.getMessagesProcessed()).thenReturn(666666666.0);
        when(applicationMetrics.getActiveConnections()).thenReturn(Integer.MAX_VALUE);
        when(applicationMetrics.getAvailableConnections()).thenReturn(Integer.MAX_VALUE - 1);
        when(applicationMetrics.getTotalConnections()).thenReturn(Integer.MAX_VALUE);
        when(applicationMetrics.getActiveBatchJobs()).thenReturn(999999L);
        when(applicationMetrics.getTotalBatchJobs()).thenReturn(1000000L);
        when(applicationMetrics.getPendingMessages()).thenReturn(555555555.0);
        when(applicationMetrics.getFailedMessages()).thenReturn(444444444.0);

        when(metricsService.getApplicationMetrics()).thenReturn(applicationMetrics);

        // When
        Response response = metricsResource.getApplicationMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();

        // Verify large values are properly formatted
        assertThat(entity).contains("\"messagesSent\":999999999");
        assertThat(entity).contains("\"messagesReceived\":888888888");
        assertThat(entity).contains("\"activeConnections\":" + Integer.MAX_VALUE);
        assertThat(entity).contains("\"totalBatchJobs\":1000000");
    }

    @Test
    @DisplayName("Should handle empty Prometheus metrics")
    void shouldHandleEmptyPrometheusMetrics() {
        // Given
        when(meterRegistry.scrape()).thenReturn("");

        // When
        Response response = metricsResource.getPrometheusMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).isEmpty();
        verify(meterRegistry).scrape();
    }

    @Test
    @DisplayName("Should handle null metrics service response")
    void shouldHandleNullMetricsServiceResponse() {
        // Given
        when(metricsService.getApplicationMetrics()).thenReturn(null);

        // When
        Response response = metricsResource.getApplicationMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("error");
        verify(metricsService).getApplicationMetrics();
    }

    @Test
    @DisplayName("Should format decimal values correctly")
    void shouldFormatDecimalValuesCorrectly() {
        // Given - Setup metrics with decimal values
        when(applicationMetrics.getMessagesSent()).thenReturn(100.5);
        when(applicationMetrics.getMessagesReceived()).thenReturn(95.7);
        when(applicationMetrics.getMessagesFailed()).thenReturn(5.2);
        when(applicationMetrics.getMessagesProcessed()).thenReturn(90.3);
        when(applicationMetrics.getPendingMessages()).thenReturn(5.8);
        when(applicationMetrics.getFailedMessages()).thenReturn(2.1);

        // Integer values remain the same
        when(applicationMetrics.getActiveConnections()).thenReturn(3);
        when(applicationMetrics.getAvailableConnections()).thenReturn(7);
        when(applicationMetrics.getTotalConnections()).thenReturn(10);
        when(applicationMetrics.getActiveBatchJobs()).thenReturn(2L);
        when(applicationMetrics.getTotalBatchJobs()).thenReturn(15L);

        when(metricsService.getApplicationMetrics()).thenReturn(applicationMetrics);

        // When
        Response response = metricsResource.getApplicationMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();

        // Verify decimal values are formatted as integers (due to %.0f format)
        assertThat(entity).contains("\"messagesSent\":101");  // 100.5 rounds to 101
        assertThat(entity).contains("\"messagesReceived\":96"); // 95.7 rounds to 96
        assertThat(entity).contains("\"messagesFailed\":5");   // 5.2 rounds to 5
        assertThat(entity).contains("\"messagesProcessed\":90"); // 90.3 rounds to 90
        assertThat(entity).contains("\"pendingMessages\":6");  // 5.8 rounds to 6
        assertThat(entity).contains("\"failedMessages\":2");   // 2.1 rounds to 2
    }

    @Test
    @DisplayName("Should handle multiple concurrent requests")
    void shouldHandleMultipleConcurrentRequests() throws InterruptedException {
        // Given
        when(metricsService.getApplicationMetrics()).thenReturn(applicationMetrics);
        when(meterRegistry.scrape()).thenReturn("# Prometheus metrics");

        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        final boolean[] success = {true};

        // When - Multiple threads requesting metrics simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    if (threadId % 2 == 0) {
                        Response response = metricsResource.getApplicationMetrics();
                        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                            success[0] = false;
                        }
                    } else {
                        Response response = metricsResource.getPrometheusMetrics();
                        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                            success[0] = false;
                        }
                    }
                } catch (Exception e) {
                    success[0] = false;
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertThat(success[0]).isTrue();
        verify(metricsService, times(5)).getApplicationMetrics(); // Half the threads
        verify(meterRegistry, times(5)).scrape(); // Other half
    }

    @Test
    @DisplayName("Should validate JSON structure in application metrics response")
    void shouldValidateJsonStructureInApplicationMetricsResponse() {
        // Given
        when(metricsService.getApplicationMetrics()).thenReturn(applicationMetrics);

        // When
        Response response = metricsResource.getApplicationMetrics();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();

        // Verify valid JSON structure - should start and end with braces
        assertThat(entity).startsWith("{");
        assertThat(entity).endsWith("}");

        // Verify all required fields are present with proper formatting
        assertThat(entity).matches(".*\"messagesSent\":\\d+.*");
        assertThat(entity).matches(".*\"messagesReceived\":\\d+.*");
        assertThat(entity).matches(".*\"messagesFailed\":\\d+.*");
        assertThat(entity).matches(".*\"messagesProcessed\":\\d+.*");
        assertThat(entity).matches(".*\"activeConnections\":\\d+.*");
        assertThat(entity).matches(".*\"availableConnections\":\\d+.*");
        assertThat(entity).matches(".*\"totalConnections\":\\d+.*");
        assertThat(entity).matches(".*\"activeBatchJobs\":\\d+.*");
        assertThat(entity).matches(".*\"totalBatchJobs\":\\d+.*");
        assertThat(entity).matches(".*\"pendingMessages\":\\d+.*");
        assertThat(entity).matches(".*\"failedMessages\":\\d+.*");
    }
}