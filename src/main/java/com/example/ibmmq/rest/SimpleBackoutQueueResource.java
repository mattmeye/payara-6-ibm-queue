package com.example.ibmmq.rest;

import com.example.ibmmq.backout.SimpleBackoutQueueService;
import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.repository.MQMessageRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Logger;

@Path("/api/backout")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SimpleBackoutQueueResource {

    private static final Logger LOGGER = Logger.getLogger(SimpleBackoutQueueResource.class.getName());

    @Inject
    private SimpleBackoutQueueService backoutQueueService;

    @Inject
    private MQMessageRepository messageRepository;

    @GET
    @Path("/stats/{queueName}")
    public Response getBackoutStats(@PathParam("queueName") String queueName) {
        try {
            SimpleBackoutQueueService.BackoutQueueStats stats = backoutQueueService.getBackoutQueueStats(queueName);
            return Response.ok(stats).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get backout stats for queue: " + queueName + " - " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to get backout statistics\"}")
                    .build();
        }
    }

    @GET
    @Path("/messages/backout")
    public Response getBackoutMessages(@QueryParam("queue") String queueName,
                                       @QueryParam("limit") @DefaultValue("50") int limit) {
        try {
            List<MQMessage> messages;
            if (queueName != null) {
                messages = messageRepository.findBackoutMessagesByQueue(queueName);
            } else {
                messages = messageRepository.findByStatusWithLimit(MQMessage.MessageStatus.BACKOUT, limit);
            }

            return Response.ok(messages).build();

        } catch (Exception e) {
            LOGGER.severe("Failed to get backout messages - " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to get backout messages\"}")
                    .build();
        }
    }

    @POST
    @Path("/move-back-all/{originalQueue}")
    public Response moveAllBackToOriginalQueue(@PathParam("originalQueue") String originalQueueName) {
        try {
            String backoutQueueName = originalQueueName + ".BACKOUT";
            int movedCount = backoutQueueService.moveAllBackToOriginalQueue(backoutQueueName, originalQueueName);

            return Response.ok()
                    .entity("{\"message\": \"" + movedCount + " messages moved back to original queue successfully\"}")
                    .build();

        } catch (Exception e) {
            LOGGER.severe("Failed to move all messages back for queue: " + originalQueueName + " - " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to move messages back\"}")
                    .build();
        }
    }

    @POST
    @Path("/move-back-batch/{originalQueue}")
    public Response moveBatchBackToOriginalQueue(@PathParam("originalQueue") String originalQueueName,
                                                @QueryParam("batchSize") @DefaultValue("10") int batchSize) {
        try {
            if (batchSize <= 0 || batchSize > 100) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Batch size must be between 1 and 100\"}")
                        .build();
            }

            String backoutQueueName = originalQueueName + ".BACKOUT";
            int movedCount = backoutQueueService.moveBatchBackToOriginalQueue(backoutQueueName, originalQueueName, batchSize);

            return Response.ok()
                    .entity("{\"message\": \"" + movedCount + " messages moved back to original queue successfully\"}")
                    .build();

        } catch (Exception e) {
            LOGGER.severe("Failed to move batch messages back for queue: " + originalQueueName + " - " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to move batch messages back\"}")
                    .build();
        }
    }

    @GET
    @Path("/health")
    public Response healthCheck() {
        try {
            return Response.ok()
                    .entity("{\"status\": \"healthy\", \"service\": \"simple-backout-queue-service\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"status\": \"unhealthy\", \"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}