package com.example.ibmmq.unit.rest;

import com.example.ibmmq.rest.BatchJobResource;
import com.example.ibmmq.service.BatchJobService;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.JobInstance;
import jakarta.batch.runtime.StepExecution;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BatchJobResource Tests")
class BatchJobResourceTest {

    @Mock
    private BatchJobService batchJobService;

    @InjectMocks
    private BatchJobResource batchJobResource;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private JobInstance jobInstance;

    @Mock
    private StepExecution stepExecution;

    @BeforeEach
    void setUp() {
        // Setup common mock behaviors
        when(jobExecution.getExecutionId()).thenReturn(12345L);
        when(jobExecution.getJobName()).thenReturn("mqProcessingJob");
        when(jobExecution.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getExitStatus()).thenReturn("COMPLETED");
        when(jobExecution.getCreateTime()).thenReturn(new Date());
        when(jobExecution.getStartTime()).thenReturn(new Date());
        when(jobExecution.getEndTime()).thenReturn(new Date());

        when(jobInstance.getInstanceId()).thenReturn(67890L);
        when(jobInstance.getJobName()).thenReturn("mqProcessingJob");

        when(stepExecution.getStepExecutionId()).thenReturn(54321L);
        when(stepExecution.getStepName()).thenReturn("processMessages");
        when(stepExecution.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(stepExecution.getExitStatus()).thenReturn("COMPLETED");
        when(stepExecution.getStartTime()).thenReturn(new Date());
        when(stepExecution.getEndTime()).thenReturn(new Date());
    }

    @Test
    @DisplayName("Should start batch job successfully")
    void shouldStartBatchJobSuccessfully() {
        // Given
        long expectedExecutionId = 12345L;
        when(batchJobService.startMQProcessingJob()).thenReturn(expectedExecutionId);

        // When
        Response response = batchJobResource.startJob();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("success");
        assertThat(response.getEntity().toString()).contains(String.valueOf(expectedExecutionId));
        verify(batchJobService).startMQProcessingJob();
    }

    @Test
    @DisplayName("Should handle batch job start failure")
    void shouldHandleBatchJobStartFailure() {
        // Given
        String errorMessage = "Failed to start job";
        when(batchJobService.startMQProcessingJob()).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = batchJobResource.startJob();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should start batch job with parameters")
    void shouldStartBatchJobWithParameters() {
        // Given
        long expectedExecutionId = 12345L;
        String jsonParams = "{\"timeout\":\"30000\",\"batchSize\":\"100\"}";
        when(batchJobService.startMQProcessingJob(any(Properties.class))).thenReturn(expectedExecutionId);

        // When
        Response response = batchJobResource.startJobWithParams(jsonParams);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("success");
        assertThat(response.getEntity().toString()).contains(String.valueOf(expectedExecutionId));
        verify(batchJobService).startMQProcessingJob(any(Properties.class));
    }

    @Test
    @DisplayName("Should handle empty parameters")
    void shouldHandleEmptyParameters() {
        // Given
        long expectedExecutionId = 12345L;
        when(batchJobService.startMQProcessingJob(any(Properties.class))).thenReturn(expectedExecutionId);

        // When
        Response response = batchJobResource.startJobWithParams("");

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(batchJobService).startMQProcessingJob(any(Properties.class));
    }

    @Test
    @DisplayName("Should handle null parameters")
    void shouldHandleNullParameters() {
        // Given
        long expectedExecutionId = 12345L;
        when(batchJobService.startMQProcessingJob(any(Properties.class))).thenReturn(expectedExecutionId);

        // When
        Response response = batchJobResource.startJobWithParams(null);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(batchJobService).startMQProcessingJob(any(Properties.class));
    }

    @Test
    @DisplayName("Should restart batch job successfully")
    void shouldRestartBatchJobSuccessfully() {
        // Given
        long executionId = 12345L;
        long newExecutionId = 12346L;
        when(batchJobService.restartJob(executionId)).thenReturn(newExecutionId);

        // When
        Response response = batchJobResource.restartJob(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("success");
        assertThat(response.getEntity().toString()).contains(String.valueOf(newExecutionId));
        verify(batchJobService).restartJob(executionId);
    }

    @Test
    @DisplayName("Should handle restart job failure")
    void shouldHandleRestartJobFailure() {
        // Given
        long executionId = 12345L;
        String errorMessage = "Failed to restart job";
        when(batchJobService.restartJob(executionId)).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = batchJobResource.restartJob(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should stop batch job successfully")
    void shouldStopBatchJobSuccessfully() {
        // Given
        long executionId = 12345L;

        // When
        Response response = batchJobResource.stopJob(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("success");
        assertThat(response.getEntity().toString()).contains(String.valueOf(executionId));
        verify(batchJobService).stopJob(executionId);
    }

    @Test
    @DisplayName("Should handle stop job failure")
    void shouldHandleStopJobFailure() {
        // Given
        long executionId = 12345L;
        String errorMessage = "Failed to stop job";
        doThrow(new RuntimeException(errorMessage)).when(batchJobService).stopJob(executionId);

        // When
        Response response = batchJobResource.stopJob(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should get job execution successfully")
    void shouldGetJobExecutionSuccessfully() {
        // Given
        long executionId = 12345L;
        when(batchJobService.getJobExecution(executionId)).thenReturn(jobExecution);

        // When
        Response response = batchJobResource.getJobExecution(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("executionId");
        assertThat(entity).contains("jobName");
        assertThat(entity).contains("batchStatus");
        assertThat(entity).contains(String.valueOf(executionId));
        verify(batchJobService).getJobExecution(executionId);
    }

    @Test
    @DisplayName("Should return not found when job execution does not exist")
    void shouldReturnNotFoundWhenJobExecutionDoesNotExist() {
        // Given
        long executionId = 99999L;
        when(batchJobService.getJobExecution(executionId)).thenReturn(null);

        // When
        Response response = batchJobResource.getJobExecution(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(response.getEntity().toString()).contains("not found");
    }

    @Test
    @DisplayName("Should handle get job execution failure")
    void shouldHandleGetJobExecutionFailure() {
        // Given
        long executionId = 12345L;
        String errorMessage = "Database error";
        when(batchJobService.getJobExecution(executionId)).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = batchJobResource.getJobExecution(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should get step executions successfully")
    void shouldGetStepExecutionsSuccessfully() {
        // Given
        long executionId = 12345L;
        List<StepExecution> steps = Arrays.asList(stepExecution);
        when(batchJobService.getStepExecutions(executionId)).thenReturn(steps);

        // When
        Response response = batchJobResource.getStepExecutions(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("steps");
        assertThat(entity).contains("stepExecutionId");
        assertThat(entity).contains("stepName");
        verify(batchJobService).getStepExecutions(executionId);
    }

    @Test
    @DisplayName("Should handle empty step executions list")
    void shouldHandleEmptyStepExecutionsList() {
        // Given
        long executionId = 12345L;
        when(batchJobService.getStepExecutions(executionId)).thenReturn(Collections.emptyList());

        // When
        Response response = batchJobResource.getStepExecutions(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("steps");
        assertThat(response.getEntity().toString()).contains("[]");
    }

    @Test
    @DisplayName("Should get job instances successfully")
    void shouldGetJobInstancesSuccessfully() {
        // Given
        List<JobInstance> instances = Arrays.asList(jobInstance);
        when(batchJobService.getJobInstances()).thenReturn(instances);

        // When
        Response response = batchJobResource.getJobInstances();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("jobInstances");
        assertThat(entity).contains("instanceId");
        assertThat(entity).contains("jobName");
        verify(batchJobService).getJobInstances();
    }

    @Test
    @DisplayName("Should get job names successfully")
    void shouldGetJobNamesSuccessfully() {
        // Given
        Set<String> jobNames = new HashSet<>(Arrays.asList("mqProcessingJob", "dataCleanupJob"));
        when(batchJobService.getJobNames()).thenReturn(jobNames);

        // When
        Response response = batchJobResource.getJobNames();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("jobNames");
        assertThat(entity).contains("mqProcessingJob");
        verify(batchJobService).getJobNames();
    }

    @Test
    @DisplayName("Should handle empty job names set")
    void shouldHandleEmptyJobNamesSet() {
        // Given
        when(batchJobService.getJobNames()).thenReturn(Collections.emptySet());

        // When
        Response response = batchJobResource.getJobNames();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("jobNames");
        assertThat(response.getEntity().toString()).contains("[]");
    }

    @Test
    @DisplayName("Should check if job is running")
    void shouldCheckIfJobIsRunning() {
        // Given
        long executionId = 12345L;
        when(batchJobService.isJobRunning(executionId)).thenReturn(true);

        // When
        Response response = batchJobResource.isJobRunning(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("executionId");
        assertThat(entity).contains("isRunning");
        assertThat(entity).contains("true");
        verify(batchJobService).isJobRunning(executionId);
    }

    @Test
    @DisplayName("Should check if job is not running")
    void shouldCheckIfJobIsNotRunning() {
        // Given
        long executionId = 12345L;
        when(batchJobService.isJobRunning(executionId)).thenReturn(false);

        // When
        Response response = batchJobResource.isJobRunning(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("false");
    }

    @Test
    @DisplayName("Should handle job running check failure")
    void shouldHandleJobRunningCheckFailure() {
        // Given
        long executionId = 12345L;
        String errorMessage = "Service unavailable";
        when(batchJobService.isJobRunning(executionId)).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = batchJobResource.isJobRunning(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should return healthy status for health check")
    void shouldReturnHealthyStatusForHealthCheck() {
        // When
        Response response = batchJobResource.healthCheck();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("healthy");
        assertThat(entity).contains("Batch Job Management");
    }

    @Test
    @DisplayName("Should handle malformed JSON parameters gracefully")
    void shouldHandleMalformedJsonParametersGracefully() {
        // Given
        long expectedExecutionId = 12345L;
        String malformedJson = "{\"timeout\":\"30000\",\"batch"; // Missing closing brace
        when(batchJobService.startMQProcessingJob(any(Properties.class))).thenReturn(expectedExecutionId);

        // When
        Response response = batchJobResource.startJobWithParams(malformedJson);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(batchJobService).startMQProcessingJob(any(Properties.class));
    }

    @Test
    @DisplayName("Should handle job execution with null exit status")
    void shouldHandleJobExecutionWithNullExitStatus() {
        // Given
        long executionId = 12345L;
        when(jobExecution.getExitStatus()).thenReturn(null);
        when(batchJobService.getJobExecution(executionId)).thenReturn(jobExecution);

        // When
        Response response = batchJobResource.getJobExecution(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("executionId");
        assertThat(entity).contains("exitStatus");
    }

    @Test
    @DisplayName("Should handle step execution with null exit status")
    void shouldHandleStepExecutionWithNullExitStatus() {
        // Given
        long executionId = 12345L;
        when(stepExecution.getExitStatus()).thenReturn(null);
        List<StepExecution> steps = Arrays.asList(stepExecution);
        when(batchJobService.getStepExecutions(executionId)).thenReturn(steps);

        // When
        Response response = batchJobResource.getStepExecutions(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("steps");
        assertThat(entity).contains("exitStatus");
    }

    @Test
    @DisplayName("Should handle multiple step executions")
    void shouldHandleMultipleStepExecutions() {
        // Given
        long executionId = 12345L;
        StepExecution step2 = mock(StepExecution.class);
        when(step2.getStepExecutionId()).thenReturn(54322L);
        when(step2.getStepName()).thenReturn("validateMessages");
        when(step2.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);
        when(step2.getExitStatus()).thenReturn("COMPLETED");
        when(step2.getStartTime()).thenReturn(new Date());
        when(step2.getEndTime()).thenReturn(new Date());

        List<StepExecution> steps = Arrays.asList(stepExecution, step2);
        when(batchJobService.getStepExecutions(executionId)).thenReturn(steps);

        // When
        Response response = batchJobResource.getStepExecutions(executionId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("processMessages");
        assertThat(entity).contains("validateMessages");
        assertThat(entity).contains("54321");
        assertThat(entity).contains("54322");
    }
}