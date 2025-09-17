package com.example.ibmmq.rest;

import com.example.ibmmq.repository.SimpleRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/simple")
@Produces(MediaType.APPLICATION_JSON)
public class SimpleTestResource {

    @Inject
    private SimpleRepository repository;

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok()
            .entity("{\"status\":\"OK\",\"service\":\"Simple Test Resource\"}")
            .build();
    }

    @GET
    @Path("/messages")
    public Response getMessages() {
        try {
            int count = repository.findAll().size();
            return Response.ok()
                .entity("{\"message\":\"Database accessible\",\"messageCount\":" + count + "}")
                .build();
        } catch (Exception e) {
            return Response.ok()
                .entity("{\"message\":\"Database not accessible but service running\",\"error\":\"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/info")
    public Response info() {
        return Response.ok()
            .entity("{\"application\":\"Payara 6 IBM MQ Integration\",\"status\":\"running\"}")
            .build();
    }
}