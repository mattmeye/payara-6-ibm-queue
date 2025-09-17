package com.example.ibmmq.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok()
            .entity("{\"status\":\"healthy\",\"service\":\"Test Service\"}")
            .build();
    }

    @GET
    @Path("/endpoints")
    public Response endpoints() {
        return Response.ok()
            .entity("{\"endpoints\":[\"/api/batch/jobs\",\"/api/batch/health\",\"/api/mq/health\",\"/api/test/health\"],\"context\":\"payara6-ibmmq\"}")
            .build();
    }
}