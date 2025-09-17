package com.example.ibmmq.rest;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.repository.MQMessageRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MQMessageResource {

    private static final Logger LOGGER = Logger.getLogger(MQMessageResource.class.getName());

    @Inject
    private MQMessageRepository messageRepository;

    @GET
    public Response getAllMessages() {
        try {
            List<MQMessage> messages = messageRepository.findAll();
            return Response.ok(convertToJson(messages)).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get all messages: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getMessageById(@PathParam("id") Long id) {
        try {
            Optional<MQMessage> message = messageRepository.findById(id);
            if (message.isPresent()) {
                return Response.ok(convertToJson(message.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"status\":\"error\",\"message\":\"Message not found\"}")
                    .build();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to get message by ID " + id + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/by-status/{status}")
    public Response getMessagesByStatus(@PathParam("status") String status) {
        try {
            MQMessage.MessageStatus messageStatus = MQMessage.MessageStatus.valueOf(status.toUpperCase());
            List<MQMessage> messages = messageRepository.findByStatus(messageStatus);
            return Response.ok(convertToJson(messages)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"status\":\"error\",\"message\":\"Invalid status: " + status + "\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get messages by status " + status + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/by-queue/{queueName}")
    public Response getMessagesByQueue(@PathParam("queueName") String queueName) {
        try {
            List<MQMessage> messages = messageRepository.findByQueue(queueName);
            return Response.ok(convertToJson(messages)).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get messages by queue " + queueName + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/count/by-status/{status}")
    public Response countMessagesByStatus(@PathParam("status") String status) {
        try {
            MQMessage.MessageStatus messageStatus = MQMessage.MessageStatus.valueOf(status.toUpperCase());
            long count = messageRepository.countByStatus(messageStatus);
            return Response.ok("{\"status\":\"" + status + "\",\"count\":" + count + "}").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"status\":\"error\",\"message\":\"Invalid status: " + status + "\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to count messages by status " + status + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMessage(@PathParam("id") Long id) {
        try {
            Optional<MQMessage> message = messageRepository.findById(id);
            if (message.isPresent()) {
                messageRepository.deleteById(id);
                return Response.ok("{\"status\":\"success\",\"message\":\"Message deleted\"}").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"status\":\"error\",\"message\":\"Message not found\"}")
                    .build();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to delete message " + id + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    @Path("/cleanup")
    public Response cleanupOldMessages(@QueryParam("days") @DefaultValue("30") int days) {
        try {
            int deletedCount = messageRepository.deleteOldProcessedMessages(days);
            return Response.ok("{\"status\":\"success\",\"deletedCount\":" + deletedCount + "}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to cleanup old messages: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/health")
    public Response healthCheck() {
        return Response.ok()
            .entity("{\"status\":\"healthy\",\"service\":\"MQ Message Repository\"}")
            .build();
    }

    private String convertToJson(List<MQMessage> messages) {
        StringBuilder json = new StringBuilder("{\"messages\":[");
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0) json.append(",");
            json.append(convertToJson(messages.get(i)));
        }
        json.append("]}");
        return json.toString();
    }

    private String convertToJson(MQMessage message) {
        return String.format(
            "{\"id\":%d,\"messageId\":\"%s\",\"correlationId\":\"%s\",\"queueName\":\"%s\"," +
            "\"messageContent\":\"%s\",\"messageType\":\"%s\",\"priority\":%s,\"expiry\":%s," +
            "\"receivedAt\":\"%s\",\"processedAt\":\"%s\",\"status\":\"%s\",\"errorMessage\":\"%s\"," +
            "\"retryCount\":%d,\"version\":%d}",
            message.getId(),
            escapeJsonString(message.getMessageId()),
            escapeJsonString(message.getCorrelationId()),
            escapeJsonString(message.getQueueName()),
            escapeJsonString(message.getMessageContent()),
            escapeJsonString(message.getMessageType()),
            message.getPriority(),
            message.getExpiry(),
            message.getReceivedAt(),
            message.getProcessedAt(),
            message.getStatus(),
            escapeJsonString(message.getErrorMessage()),
            message.getRetryCount(),
            message.getVersion()
        );
    }

    private String escapeJsonString(String value) {
        if (value == null) return "null";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}