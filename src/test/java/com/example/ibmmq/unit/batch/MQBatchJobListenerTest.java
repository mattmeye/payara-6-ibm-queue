package com.example.ibmmq.unit.batch;

import com.example.ibmmq.batch.MQBatchJobListener;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MQBatchJobListener Comprehensive Tests")
class MQBatchJobListenerTest {

    @Mock
    private JobContext jobContext;

    @Mock
    private Properties jobProperties;

    @InjectMocks
    private MQBatchJobListener jobListener;

    @BeforeEach
    void setUp() {
        when(jobContext.getJobName()).thenReturn("TestMQJob");
        when(jobContext.getExecutionId()).thenReturn(12345L);
        when(jobContext.getProperties()).thenReturn(jobProperties);
    }

    @Test
    @DisplayName("Should execute beforeJob successfully")
    void shouldExecuteBeforeJobSuccessfully() throws Exception {
        // When
        jobListener.beforeJob();

        // Then
        verify(jobContext).getJobName();
        verify(jobContext).getExecutionId();
        verify(jobContext).getProperties();
        verify(jobProperties).setProperty(eq("job.start.time"), anyString());
    }

    @Test
    @DisplayName("Should set start time property in beforeJob")
    void shouldSetStartTimePropertyInBeforeJob() throws Exception {
        // Given
        long beforeTime = System.currentTimeMillis();

        // When
        jobListener.beforeJob();

        // Then
        verify(jobProperties).setProperty(eq("job.start.time"), argThat(timeStr -> {
            long time = Long.parseLong(timeStr);
            return time >= beforeTime && time <= System.currentTimeMillis();
        }));
    }

    @Test
    @DisplayName("Should execute afterJob with COMPLETED status")
    void shouldExecuteAfterJobWithCompletedStatus() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(System.currentTimeMillis() - 1000));

        // When
        jobListener.afterJob();

        // Then
        verify(jobContext).getExitStatus();
        verify(jobContext).getBatchStatus();
        verify(jobProperties).getProperty("job.start.time");
        verify(jobProperties).setProperty(eq("job.duration"), anyString());
    }

    @Test
    @DisplayName("Should execute afterJob with FAILED status")
    void shouldExecuteAfterJobWithFailedStatus() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn("FAILED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.FAILED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(System.currentTimeMillis() - 2000));

        // When
        jobListener.afterJob();

        // Then
        verify(jobContext).getExitStatus();
        verify(jobContext).getBatchStatus();
        verify(jobProperties).setProperty(eq("job.duration"), anyString());
    }

    @Test
    @DisplayName("Should execute afterJob with STOPPED status")
    void shouldExecuteAfterJobWithStoppedStatus() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn("STOPPED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.STOPPED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(System.currentTimeMillis() - 500));

        // When
        jobListener.afterJob();

        // Then
        verify(jobContext).getExitStatus();
        verify(jobContext).getBatchStatus();
        verify(jobProperties).setProperty(eq("job.duration"), anyString());
    }

    @Test
    @DisplayName("Should handle null start time in afterJob")
    void shouldHandleNullStartTimeInAfterJob() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(null);

        long beforeTime = System.currentTimeMillis();

        // When
        jobListener.afterJob();

        // Then
        verify(jobProperties).setProperty(eq("job.duration"), argThat(durationStr -> {
            long duration = Long.parseLong(durationStr);
            // Duration should be very small since start time defaults to current time
            return duration >= 0 && duration <= (System.currentTimeMillis() - beforeTime + 100);
        }));
    }

    @Test
    @DisplayName("Should handle empty start time in afterJob")
    void shouldHandleEmptyStartTimeInAfterJob() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn("");

        // When & Then - should handle NumberFormatException gracefully
        assertThatThrownBy(() -> jobListener.afterJob())
            .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("Should handle invalid start time in afterJob")
    void shouldHandleInvalidStartTimeInAfterJob() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn("not-a-number");

        // When & Then - should handle NumberFormatException
        assertThatThrownBy(() -> jobListener.afterJob())
            .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("Should calculate correct duration")
    void shouldCalculateCorrectDuration() throws Exception {
        // Given
        long startTime = System.currentTimeMillis() - 5000; // 5 seconds ago
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(startTime));

        // When
        jobListener.afterJob();

        // Then
        verify(jobProperties).setProperty(eq("job.duration"), argThat(durationStr -> {
            long duration = Long.parseLong(durationStr);
            // Duration should be approximately 5000ms, allowing some tolerance
            return duration >= 4900 && duration <= 5100;
        }));
    }

    @Test
    @DisplayName("Should handle null exit status")
    void shouldHandleNullExitStatus() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn(null);
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(System.currentTimeMillis() - 1000));

        // When
        jobListener.afterJob();

        // Then
        verify(jobContext).getExitStatus();
        verify(jobContext).getBatchStatus();
        verify(jobProperties).setProperty(eq("job.duration"), anyString());
    }

    @Test
    @DisplayName("Should handle null batch status")
    void shouldHandleNullBatchStatus() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(null);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(System.currentTimeMillis() - 1000));

        // When & Then - should handle NullPointerException gracefully
        assertThatThrownBy(() -> jobListener.afterJob())
            .isInstanceOf(NullPointerException.class);

        // Verify that the method was called despite the exception
        verify(jobContext).getBatchStatus();
    }

    @Test
    @DisplayName("Should handle different job names and execution IDs")
    void shouldHandleDifferentJobNamesAndExecutionIds() throws Exception {
        // Given
        when(jobContext.getJobName()).thenReturn("CustomJobName");
        when(jobContext.getExecutionId()).thenReturn(99999L);

        // When
        jobListener.beforeJob();

        // Then
        verify(jobContext).getJobName();
        verify(jobContext).getExecutionId();
        verify(jobProperties).setProperty(eq("job.start.time"), anyString());
    }

    @Test
    @DisplayName("Should handle null job name")
    void shouldHandleNullJobName() throws Exception {
        // Given
        when(jobContext.getJobName()).thenReturn(null);

        // When
        jobListener.beforeJob();

        // Then
        verify(jobContext).getJobName();
        verify(jobProperties).setProperty(eq("job.start.time"), anyString());
    }

    @Test
    @DisplayName("Should handle exception in beforeJob")
    void shouldHandleExceptionInBeforeJob() throws Exception {
        // Given
        when(jobContext.getJobName()).thenThrow(new RuntimeException("Context error"));

        // When & Then
        assertThatThrownBy(() -> jobListener.beforeJob())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Context error");
    }

    @Test
    @DisplayName("Should handle exception in afterJob")
    void shouldHandleExceptionInAfterJob() throws Exception {
        // Given
        when(jobContext.getExitStatus()).thenThrow(new RuntimeException("Context error"));

        // When & Then
        assertThatThrownBy(() -> jobListener.afterJob())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Context error");
    }

    @Test
    @DisplayName("Should handle various batch statuses")
    void shouldHandleVariousBatchStatuses() throws Exception {
        // Given different batch statuses
        BatchStatus[] statuses = {
            BatchStatus.STARTING,
            BatchStatus.STARTED,
            BatchStatus.STOPPING,
            BatchStatus.STOPPED,
            BatchStatus.ABANDONED
        };

        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(System.currentTimeMillis() - 1000));

        for (BatchStatus status : statuses) {
            // Given
            when(jobContext.getExitStatus()).thenReturn(status.toString());
            when(jobContext.getBatchStatus()).thenReturn(status);

            // When
            jobListener.afterJob();

            // Then
            verify(jobContext, atLeastOnce()).getBatchStatus();
        }
    }

    @Test
    @DisplayName("Should set duration property correctly")
    void shouldSetDurationPropertyCorrectly() throws Exception {
        // Given
        long startTime = System.currentTimeMillis() - 3000; // 3 seconds ago
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(startTime));

        // When
        jobListener.afterJob();

        // Then
        verify(jobProperties).setProperty(eq("job.duration"), argThat(durationStr -> {
            try {
                long duration = Long.parseLong(durationStr);
                return duration > 0; // Should be positive
            } catch (NumberFormatException e) {
                return false;
            }
        }));
    }

    @Test
    @DisplayName("Should handle very long job duration")
    void shouldHandleVeryLongJobDuration() throws Exception {
        // Given
        long startTime = System.currentTimeMillis() - 3600000; // 1 hour ago
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(startTime));

        // When
        jobListener.afterJob();

        // Then
        verify(jobProperties).setProperty(eq("job.duration"), argThat(durationStr -> {
            long duration = Long.parseLong(durationStr);
            // Should be approximately 1 hour (3600000ms)
            return duration >= 3590000 && duration <= 3610000;
        }));
    }

    @Test
    @DisplayName("Should handle zero duration")
    void shouldHandleZeroDuration() throws Exception {
        // Given
        long currentTime = System.currentTimeMillis();
        when(jobContext.getExitStatus()).thenReturn("COMPLETED");
        when(jobContext.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobProperties.getProperty("job.start.time")).thenReturn(String.valueOf(currentTime));

        // When
        jobListener.afterJob();

        // Then
        verify(jobProperties).setProperty(eq("job.duration"), argThat(durationStr -> {
            long duration = Long.parseLong(durationStr);
            // Duration should be very small (0 to a few milliseconds)
            return duration >= 0 && duration <= 100;
        }));
    }
}