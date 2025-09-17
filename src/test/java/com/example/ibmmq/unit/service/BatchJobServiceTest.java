package com.example.ibmmq.unit.service;

import com.example.ibmmq.service.BatchJobService;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.JobInstance;
import jakarta.batch.runtime.StepExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BatchJobService Tests")
class BatchJobServiceTest {

    @Mock
    private JobOperator jobOperator;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private JobInstance jobInstance;

    @Mock
    private StepExecution stepExecution;

    private BatchJobService batchJobService;

    @BeforeEach
    void setUp() {
        // We need to use reflection or create a constructor that accepts JobOperator
        // For this test, we'll create a package-private constructor for testing
        batchJobService = new BatchJobService();
        // Inject the mock using reflection (in real scenario, use proper DI testing)
        try {
            java.lang.reflect.Field field = BatchJobService.class.getDeclaredField("jobOperator");
            field.setAccessible(true);
            field.set(batchJobService, jobOperator);
        } catch (Exception e) {
            // Handle reflection exception
        }
    }

    @Test
    @DisplayName("Should start MQ processing job with default properties")
    void shouldStartMQProcessingJobWithDefaultProperties() {
        // Given
        long expectedExecutionId = 123L;
        when(jobOperator.start(eq("mq-to-postgres-job"), any(Properties.class))).thenReturn(expectedExecutionId);

        // When
        long result = batchJobService.startMQProcessingJob();

        // Then
        assertThat(result).isEqualTo(expectedExecutionId);
        verify(jobOperator).start(eq("mq-to-postgres-job"), any(Properties.class));
    }

    @Test
    @DisplayName("Should start MQ processing job with custom properties")
    void shouldStartMQProcessingJobWithCustomProperties() {
        // Given
        Properties customProps = new Properties();
        customProps.setProperty("chunk.size", "20");
        customProps.setProperty("skip.limit", "10");
        long expectedExecutionId = 456L;
        when(jobOperator.start("mq-to-postgres-job", customProps)).thenReturn(expectedExecutionId);

        // When
        long result = batchJobService.startMQProcessingJob(customProps);

        // Then
        assertThat(result).isEqualTo(expectedExecutionId);
        verify(jobOperator).start("mq-to-postgres-job", customProps);
    }

    @Test
    @DisplayName("Should throw exception when job start fails")
    void shouldThrowExceptionWhenJobStartFails() {
        // Given
        when(jobOperator.start(anyString(), any(Properties.class)))
            .thenThrow(new RuntimeException("Job start failed"));

        // When & Then
        assertThatThrownBy(() -> batchJobService.startMQProcessingJob())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to start batch job")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should restart job successfully")
    void shouldRestartJobSuccessfully() {
        // Given
        long originalExecutionId = 123L;
        long newExecutionId = 456L;
        when(jobOperator.restart(eq(originalExecutionId), any(Properties.class))).thenReturn(newExecutionId);

        // When
        long result = batchJobService.restartJob(originalExecutionId);

        // Then
        assertThat(result).isEqualTo(newExecutionId);
        verify(jobOperator).restart(eq(originalExecutionId), any(Properties.class));
    }

    @Test
    @DisplayName("Should throw exception when job restart fails")
    void shouldThrowExceptionWhenJobRestartFails() {
        // Given
        long executionId = 123L;
        when(jobOperator.restart(eq(executionId), any(Properties.class)))
            .thenThrow(new RuntimeException("Restart failed"));

        // When & Then
        assertThatThrownBy(() -> batchJobService.restartJob(executionId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to restart batch job")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should stop job successfully")
    void shouldStopJobSuccessfully() {
        // Given
        long executionId = 123L;
        doNothing().when(jobOperator).stop(executionId);

        // When & Then
        assertThatCode(() -> batchJobService.stopJob(executionId))
            .doesNotThrowAnyException();

        verify(jobOperator).stop(executionId);
    }

    @Test
    @DisplayName("Should throw exception when job stop fails")
    void shouldThrowExceptionWhenJobStopFails() {
        // Given
        long executionId = 123L;
        doThrow(new RuntimeException("Stop failed")).when(jobOperator).stop(executionId);

        // When & Then
        assertThatThrownBy(() -> batchJobService.stopJob(executionId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to stop batch job")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should get job execution successfully")
    void shouldGetJobExecutionSuccessfully() {
        // Given
        long executionId = 123L;
        when(jobOperator.getJobExecution(executionId)).thenReturn(jobExecution);

        // When
        JobExecution result = batchJobService.getJobExecution(executionId);

        // Then
        assertThat(result).isEqualTo(jobExecution);
        verify(jobOperator).getJobExecution(executionId);
    }

    @Test
    @DisplayName("Should return null when job execution not found")
    void shouldReturnNullWhenJobExecutionNotFound() {
        // Given
        long executionId = 999L;
        when(jobOperator.getJobExecution(executionId)).thenThrow(new NoSuchJobExecutionException("Not found"));

        // When
        JobExecution result = batchJobService.getJobExecution(executionId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should get job instances successfully")
    void shouldGetJobInstancesSuccessfully() {
        // Given
        List<JobInstance> expectedInstances = Arrays.asList(jobInstance, jobInstance);
        when(jobOperator.getJobInstances("mq-to-postgres-job", 0, 100)).thenReturn(expectedInstances);

        // When
        List<JobInstance> result = batchJobService.getJobInstances();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedInstances);
        verify(jobOperator).getJobInstances("mq-to-postgres-job", 0, 100);
    }

    @Test
    @DisplayName("Should return empty list when getting job instances fails")
    void shouldReturnEmptyListWhenGettingJobInstancesFails() {
        // Given
        when(jobOperator.getJobInstances(anyString(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Failed to get instances"));

        // When
        List<JobInstance> result = batchJobService.getJobInstances();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get job executions for instance successfully")
    void shouldGetJobExecutionsForInstanceSuccessfully() {
        // Given
        List<JobExecution> expectedExecutions = Arrays.asList(jobExecution, jobExecution);
        when(jobOperator.getJobExecutions(jobInstance)).thenReturn(expectedExecutions);

        // When
        List<JobExecution> result = batchJobService.getJobExecutions(jobInstance);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedExecutions);
        verify(jobOperator).getJobExecutions(jobInstance);
    }

    @Test
    @DisplayName("Should return empty list when getting job executions fails")
    void shouldReturnEmptyListWhenGettingJobExecutionsFails() {
        // Given
        when(jobOperator.getJobExecutions(jobInstance))
            .thenThrow(new RuntimeException("Failed to get executions"));

        // When
        List<JobExecution> result = batchJobService.getJobExecutions(jobInstance);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get step executions successfully")
    void shouldGetStepExecutionsSuccessfully() {
        // Given
        long executionId = 123L;
        List<StepExecution> expectedSteps = Arrays.asList(stepExecution, stepExecution);
        when(jobOperator.getStepExecutions(executionId)).thenReturn(expectedSteps);

        // When
        List<StepExecution> result = batchJobService.getStepExecutions(executionId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedSteps);
        verify(jobOperator).getStepExecutions(executionId);
    }

    @Test
    @DisplayName("Should return empty list when getting step executions fails")
    void shouldReturnEmptyListWhenGettingStepExecutionsFails() {
        // Given
        long executionId = 123L;
        when(jobOperator.getStepExecutions(executionId))
            .thenThrow(new RuntimeException("Failed to get step executions"));

        // When
        List<StepExecution> result = batchJobService.getStepExecutions(executionId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get job names successfully")
    void shouldGetJobNamesSuccessfully() {
        // Given
        Set<String> expectedNames = Set.of("mq-to-postgres-job", "other-job");
        when(jobOperator.getJobNames()).thenReturn(expectedNames);

        // When
        Set<String> result = batchJobService.getJobNames();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedNames);
        verify(jobOperator).getJobNames();
    }

    @Test
    @DisplayName("Should return empty set when getting job names fails")
    void shouldReturnEmptySetWhenGettingJobNamesFails() {
        // Given
        when(jobOperator.getJobNames()).thenThrow(new RuntimeException("Failed to get job names"));

        // When
        Set<String> result = batchJobService.getJobNames();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get job parameters successfully")
    void shouldGetJobParametersSuccessfully() {
        // Given
        long executionId = 123L;
        Properties expectedProps = new Properties();
        expectedProps.setProperty("chunk.size", "10");
        when(jobOperator.getJobExecution(executionId)).thenReturn(jobExecution);
        when(jobExecution.getJobParameters()).thenReturn(expectedProps);

        // When
        Properties result = batchJobService.getJobParameters(executionId);

        // Then
        assertThat(result).isEqualTo(expectedProps);
        assertThat(result.getProperty("chunk.size")).isEqualTo("10");
    }

    @Test
    @DisplayName("Should return empty properties when job execution not found")
    void shouldReturnEmptyPropertiesWhenJobExecutionNotFound() {
        // Given
        long executionId = 999L;
        when(jobOperator.getJobExecution(executionId)).thenThrow(new NoSuchJobExecutionException("Not found"));

        // When
        Properties result = batchJobService.getJobParameters(executionId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check if job is running - true for STARTED status")
    void shouldCheckIfJobIsRunningTrueForStartedStatus() {
        // Given
        long executionId = 123L;
        when(jobOperator.getJobExecution(executionId)).thenReturn(jobExecution);
        when(jobExecution.getBatchStatus()).thenReturn(BatchStatus.STARTED);

        // When
        boolean result = batchJobService.isJobRunning(executionId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should check if job is running - true for STARTING status")
    void shouldCheckIfJobIsRunningTrueForStartingStatus() {
        // Given
        long executionId = 123L;
        when(jobOperator.getJobExecution(executionId)).thenReturn(jobExecution);
        when(jobExecution.getBatchStatus()).thenReturn(BatchStatus.STARTING);

        // When
        boolean result = batchJobService.isJobRunning(executionId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should check if job is running - false for COMPLETED status")
    void shouldCheckIfJobIsRunningFalseForCompletedStatus() {
        // Given
        long executionId = 123L;
        when(jobOperator.getJobExecution(executionId)).thenReturn(jobExecution);
        when(jobExecution.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);

        // When
        boolean result = batchJobService.isJobRunning(executionId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should check if job is running - false when execution not found")
    void shouldCheckIfJobIsRunningFalseWhenExecutionNotFound() {
        // Given
        long executionId = 999L;
        when(jobOperator.getJobExecution(executionId)).thenThrow(new NoSuchJobExecutionException("Not found"));

        // When
        boolean result = batchJobService.isJobRunning(executionId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when status check fails")
    void shouldReturnFalseWhenStatusCheckFails() {
        // Given
        long executionId = 123L;
        when(jobOperator.getJobExecution(executionId)).thenThrow(new RuntimeException("Status check failed"));

        // When
        boolean result = batchJobService.isJobRunning(executionId);

        // Then
        assertThat(result).isFalse();
    }
}