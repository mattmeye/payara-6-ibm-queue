package com.example.ibmmq.service;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.JobInstance;
import jakarta.batch.runtime.StepExecution;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class BatchJobService {

    private static final Logger LOGGER = Logger.getLogger(BatchJobService.class.getName());
    private static final String JOB_NAME = "mq-to-postgres-job";

    private final JobOperator jobOperator;

    public BatchJobService() {
        this.jobOperator = BatchRuntime.getJobOperator();
    }

    public long startMQProcessingJob() {
        return startMQProcessingJob(new Properties());
    }

    public long startMQProcessingJob(Properties jobParameters) {
        try {
            LOGGER.info("Starting MQ processing batch job with parameters: " + jobParameters);

            long executionId = jobOperator.start(JOB_NAME, jobParameters);

            LOGGER.info("Started MQ processing batch job with execution ID: " + executionId);
            return executionId;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start MQ processing batch job", e);
            throw new RuntimeException("Failed to start batch job", e);
        }
    }

    public long restartJob(long executionId) {
        try {
            LOGGER.info("Restarting batch job execution: " + executionId);

            long newExecutionId = jobOperator.restart(executionId, new Properties());

            LOGGER.info("Restarted batch job with new execution ID: " + newExecutionId);
            return newExecutionId;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to restart batch job execution: " + executionId, e);
            throw new RuntimeException("Failed to restart batch job", e);
        }
    }

    public void stopJob(long executionId) {
        try {
            LOGGER.info("Stopping batch job execution: " + executionId);

            jobOperator.stop(executionId);

            LOGGER.info("Stop request sent for batch job execution: " + executionId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to stop batch job execution: " + executionId, e);
            throw new RuntimeException("Failed to stop batch job", e);
        }
    }

    public JobExecution getJobExecution(long executionId) {
        try {
            return jobOperator.getJobExecution(executionId);
        } catch (NoSuchJobExecutionException e) {
            LOGGER.warning("Job execution not found: " + executionId);
            return null;
        }
    }

    public List<JobInstance> getJobInstances() {
        try {
            return jobOperator.getJobInstances(JOB_NAME, 0, 100);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get job instances", e);
            return List.of();
        }
    }

    public List<JobExecution> getJobExecutions(JobInstance jobInstance) {
        try {
            return jobOperator.getJobExecutions(jobInstance);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get job executions for instance: " + jobInstance.getInstanceId(), e);
            return List.of();
        }
    }

    public List<StepExecution> getStepExecutions(long executionId) {
        try {
            return jobOperator.getStepExecutions(executionId);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get step executions for job: " + executionId, e);
            return List.of();
        }
    }

    public Set<String> getJobNames() {
        try {
            return jobOperator.getJobNames();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get job names", e);
            return Set.of();
        }
    }

    public Properties getJobParameters(long executionId) {
        try {
            JobExecution execution = getJobExecution(executionId);
            return execution != null ? execution.getJobParameters() : new Properties();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get job parameters for execution: " + executionId, e);
            return new Properties();
        }
    }

    public boolean isJobRunning(long executionId) {
        try {
            JobExecution execution = getJobExecution(executionId);
            if (execution == null) {
                return false;
            }

            switch (execution.getBatchStatus()) {
                case STARTING:
                case STARTED:
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to check job status for execution: " + executionId, e);
            return false;
        }
    }
}