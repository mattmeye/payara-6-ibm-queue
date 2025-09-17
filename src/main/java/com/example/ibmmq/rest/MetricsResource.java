package com.example.ibmmq.rest;

import com.example.ibmmq.monitoring.MQMetricsService;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.logging.Logger;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    private static final Logger LOGGER = Logger.getLogger(MetricsResource.class.getName());

    @Inject
    private MQMetricsService metricsService;

    @Inject
    private PrometheusMeterRegistry meterRegistry;

    @GET
    @Path("/prometheus")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPrometheusMetrics() {
        try {
            String metrics = meterRegistry.scrape();
            return Response.ok(metrics).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to scrape Prometheus metrics: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Failed to retrieve metrics")
                .build();
        }
    }

    @GET
    @Path("/application")
    public Response getApplicationMetrics() {
        try {
            MQMetricsService.ApplicationMetrics metrics = metricsService.getApplicationMetrics();

            String json = String.format(
                "{\"messagesSent\":%.0f,\"messagesReceived\":%.0f,\"messagesFailed\":%.0f," +
                "\"messagesProcessed\":%.0f,\"activeConnections\":%d,\"availableConnections\":%d," +
                "\"totalConnections\":%d,\"activeBatchJobs\":%d,\"totalBatchJobs\":%d," +
                "\"pendingMessages\":%.0f,\"failedMessages\":%.0f}",
                metrics.getMessagesSent(),
                metrics.getMessagesReceived(),
                metrics.getMessagesFailed(),
                metrics.getMessagesProcessed(),
                metrics.getActiveConnections(),
                metrics.getAvailableConnections(),
                metrics.getTotalConnections(),
                metrics.getActiveBatchJobs(),
                metrics.getTotalBatchJobs(),
                metrics.getPendingMessages(),
                metrics.getFailedMessages()
            );

            return Response.ok(json).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get application metrics: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/health")
    public Response healthCheck() {
        return Response.ok()
            .entity("{\"status\":\"healthy\",\"service\":\"Metrics Service\"}")
            .build();
    }
}