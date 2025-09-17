package com.example.ibmmq.rest;

import com.example.ibmmq.service.IBMMQService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.logging.Logger;

@Path("/mq")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "mq", description = "IBM MQ message operations")
public class IBMMQResource {

    private static final Logger LOGGER = Logger.getLogger(IBMMQResource.class.getName());

    @Inject
    private IBMMQService mqService;

    @POST
    @Path("/send")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Send message to default queue",
        description = "Sends a text message to the default IBM MQ queue"
    )
    public Response sendMessage(
        @Parameter(description = "Message content to send", example = "Hello World", required = true)
        String message) {
        try {
            mqService.sendMessage(message);
            return Response.ok()
                .entity("{\"status\":\"success\",\"message\":\"Message sent successfully\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    @Path("/send/{queue}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendMessageToQueue(@PathParam("queue") String queueName, String message) {
        try {
            mqService.sendMessage(queueName, message);
            return Response.ok()
                .entity("{\"status\":\"success\",\"message\":\"Message sent to queue " + queueName + "\"}")
                .build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message to queue " + queueName + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/receive")
    public Response receiveMessage() {
        try {
            String message = mqService.receiveMessage();
            if (message != null) {
                return Response.ok()
                    .entity("{\"status\":\"success\",\"message\":\"" + message + "\"}")
                    .build();
            } else {
                return Response.ok()
                    .entity("{\"status\":\"success\",\"message\":\"No message available\"}")
                    .build();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to receive message: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/receive/{queue}")
    public Response receiveMessageFromQueue(@PathParam("queue") String queueName) {
        try {
            String message = mqService.receiveMessage(queueName);
            if (message != null) {
                return Response.ok()
                    .entity("{\"status\":\"success\",\"message\":\"" + message + "\"}")
                    .build();
            } else {
                return Response.ok()
                    .entity("{\"status\":\"success\",\"message\":\"No message available in queue " + queueName + "\"}")
                    .build();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to receive message from queue " + queueName + ": " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    @Path("/sendreceive")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendAndReceive(String message) {
        try {
            String response = mqService.sendAndReceive(message);
            if (response != null) {
                return Response.ok()
                    .entity("{\"status\":\"success\",\"response\":\"" + response + "\"}")
                    .build();
            } else {
                return Response.ok()
                    .entity("{\"status\":\"success\",\"response\":\"No response received\"}")
                    .build();
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to send and receive message: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/health")
    public Response healthCheck() {
        return Response.ok()
            .entity("{\"status\":\"healthy\",\"service\":\"IBM MQ Integration\"}")
            .build();
    }
}