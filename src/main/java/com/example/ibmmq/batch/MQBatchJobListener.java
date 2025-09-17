package com.example.ibmmq.batch;

import jakarta.batch.api.listener.JobListener;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.logging.Logger;

@Dependent
@Named
public class MQBatchJobListener implements JobListener {

    private static final Logger LOGGER = Logger.getLogger(MQBatchJobListener.class.getName());

    @Inject
    private JobContext jobContext;

    @Override
    public void beforeJob() throws Exception {
        LOGGER.info("Starting MQ to PostgreSQL batch job: " + jobContext.getJobName() +
                   " (Execution ID: " + jobContext.getExecutionId() + ")");

        long startTime = System.currentTimeMillis();
        jobContext.getProperties().setProperty("job.start.time", String.valueOf(startTime));
    }

    @Override
    public void afterJob() throws Exception {
        String startTimeStr = jobContext.getProperties().getProperty("job.start.time");
        long startTime = startTimeStr != null ? Long.parseLong(startTimeStr) : System.currentTimeMillis();
        long duration = System.currentTimeMillis() - startTime;

        String exitStatus = jobContext.getExitStatus();
        String batchStatus = jobContext.getBatchStatus().toString();

        LOGGER.info("Completed MQ to PostgreSQL batch job: " + jobContext.getJobName() +
                   " (Execution ID: " + jobContext.getExecutionId() + ")" +
                   " - Duration: " + duration + "ms" +
                   " - Batch Status: " + batchStatus +
                   " - Exit Status: " + exitStatus);

        jobContext.getProperties().setProperty("job.duration", String.valueOf(duration));

        if ("FAILED".equals(exitStatus)) {
            LOGGER.severe("Batch job failed - check application logs for details");
        } else if ("COMPLETED".equals(exitStatus)) {
            LOGGER.info("Batch job completed successfully");
        } else {
            LOGGER.warning("Batch job finished with status: " + exitStatus);
        }
    }
}