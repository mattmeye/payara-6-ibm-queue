package com.example.ibmmq.rest;

import com.example.ibmmq.service.BatchJobService;
import jakarta.batch.runtime.JobExecution;
import jakarta.batch.runtime.JobInstance;
import jakarta.batch.runtime.StepExecution;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

@Path("/batch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchJobResource {

    private static final Logger LOGGER = Logger.getLogger(BatchJobResource.class.getName());

    @Inject
    private BatchJobService batchJobService;

    @POST
    @Path("/jobs/start")
    public Response startJob() {
        try {
            long executionId = batchJobService.startMQProcessingJob();
            return Response.ok()
                .entity("{\"status\":\"success\",\"executionId\":" + executionId + ",\"message\":\"Batch job started\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to start batch job: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    @Path("/jobs/start-with-params")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response startJobWithParams(String jsonParams) {
        try {
            Properties params = new Properties();

            if (jsonParams != null && !jsonParams.trim().isEmpty()) {
                String[] pairs = jsonParams.replace("{", "").replace("}", "").replace("\"", "").split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        params.setProperty(keyValue[0].trim(), keyValue[1].trim());
                    }
                }
            }

            long executionId = batchJobService.startMQProcessingJob(params);
            return Response.ok()
                .entity("{\"status\":\"success\",\"executionId\":" + executionId + ",\"message\":\"Batch job started with parameters\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to start batch job with parameters: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    @Path("/jobs/{executionId}/restart")
    public Response restartJob(@PathParam("executionId") long executionId) {
        try {
            long newExecutionId = batchJobService.restartJob(executionId);
            return Response.ok()
                .entity("{\"status\":\"success\",\"executionId\":" + newExecutionId + ",\"message\":\"Batch job restarted\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to restart batch job " + executionId + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    @Path("/jobs/{executionId}/stop")
    public Response stopJob(@PathParam("executionId") long executionId) {
        try {
            batchJobService.stopJob(executionId);
            return Response.ok()
                .entity("{\"status\":\"success\",\"message\":\"Stop request sent for batch job " + executionId + "\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to stop batch job " + executionId + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/jobs/{executionId}")
    public Response getJobExecution(@PathParam("executionId") long executionId) {
        try {
            JobExecution execution = batchJobService.getJobExecution(executionId);
            if (execution == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"status\":\"error\",\"message\":\"Job execution not found\"}")
                    .build();
            }

            String json = String.format(
                "{\"executionId\":%d,\"jobName\":\"%s\",\"batchStatus\":\"%s\",\"exitStatus\":\"%s\"," +
                "\"createTime\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\"}",
                execution.getExecutionId(),
                execution.getJobName(),
                execution.getBatchStatus(),
                execution.getExitStatus() != null ? execution.getExitStatus() : "",
                execution.getCreateTime(),
                execution.getStartTime(),
                execution.getEndTime()
            );

            return Response.ok().entity(json).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get job execution " + executionId + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/jobs/{executionId}/steps")
    public Response getStepExecutions(@PathParam("executionId") long executionId) {
        try {
            List<StepExecution> steps = batchJobService.getStepExecutions(executionId);

            StringBuilder json = new StringBuilder("{\"steps\":[");
            for (int i = 0; i < steps.size(); i++) {
                StepExecution step = steps.get(i);
                if (i > 0) json.append(",");
                json.append(String.format(
                    "{\"stepExecutionId\":%d,\"stepName\":\"%s\",\"batchStatus\":\"%s\",\"exitStatus\":\"%s\"," +
                    "\"startTime\":\"%s\",\"endTime\":\"%s\"}",
                    step.getStepExecutionId(),
                    step.getStepName(),
                    step.getBatchStatus(),
                    step.getExitStatus() != null ? step.getExitStatus() : "",
                    step.getStartTime(),
                    step.getEndTime()
                ));
            }
            json.append("]}");

            return Response.ok().entity(json.toString()).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get step executions for job " + executionId + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/jobs")
    public Response getJobInstances() {
        try {
            List<JobInstance> instances = batchJobService.getJobInstances();

            StringBuilder json = new StringBuilder("{\"jobInstances\":[");
            for (int i = 0; i < instances.size(); i++) {
                JobInstance instance = instances.get(i);
                if (i > 0) json.append(",");
                json.append(String.format(
                    "{\"instanceId\":%d,\"jobName\":\"%s\"}",
                    instance.getInstanceId(),
                    instance.getJobName()
                ));
            }
            json.append("]}");

            return Response.ok().entity(json.toString()).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get job instances: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/jobs/names")
    public Response getJobNames() {
        try {
            Set<String> jobNames = batchJobService.getJobNames();

            StringBuilder json = new StringBuilder("{\"jobNames\":[");
            int i = 0;
            for (String jobName : jobNames) {
                if (i > 0) json.append(",");
                json.append("\"").append(jobName).append("\"");
                i++;
            }
            json.append("]}");

            return Response.ok().entity(json.toString()).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get job names: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/jobs/{executionId}/running")
    public Response isJobRunning(@PathParam("executionId") long executionId) {
        try {
            boolean isRunning = batchJobService.isJobRunning(executionId);
            return Response.ok()
                .entity("{\"executionId\":" + executionId + ",\"isRunning\":" + isRunning + "}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to check job status " + executionId + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/health")
    public Response healthCheck() {
        return Response.ok()
            .entity("{\"status\":\"healthy\",\"service\":\"Batch Job Management\"}")
            .build();
    }
}