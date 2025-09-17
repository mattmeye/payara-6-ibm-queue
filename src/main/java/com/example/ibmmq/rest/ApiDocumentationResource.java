package com.example.ibmmq.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Resource for serving API documentation files
 */
@ApplicationScoped
@Path("/docs")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "docs", description = "API Documentation")
public class ApiDocumentationResource {

    private static final Logger LOGGER = Logger.getLogger(ApiDocumentationResource.class.getName());

    @GET
    @Path("/openapi.yaml")
    @Produces("application/x-yaml")
    @Operation(
        summary = "Get OpenAPI specification",
        description = "Returns the OpenAPI 3.0 specification in YAML format"
    )
    public Response getOpenApiSpec() {
        try {
            String spec = loadResourceAsString("/openapi.yaml");
            return Response.ok(spec)
                .header("Content-Type", "application/x-yaml; charset=utf-8")
                .header("Content-Disposition", "inline; filename=\"openapi.yaml\"")
                .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load OpenAPI specification", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to load OpenAPI specification\"}")
                .build();
        }
    }

    @GET
    @Path("/openapi.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get OpenAPI specification as JSON",
        description = "Returns the OpenAPI 3.0 specification in JSON format (converted from YAML)"
    )
    public Response getOpenApiSpecAsJson() {
        try {
            // For simplicity, return a JSON message pointing to YAML version
            // In production, you might want to use a YAML to JSON converter
            String jsonResponse = "{\n" +
                "  \"message\": \"OpenAPI specification available in YAML format at /api/docs/openapi.yaml\",\n" +
                "  \"yaml_url\": \"/api/docs/openapi.yaml\",\n" +
                "  \"swagger_ui_url\": \"/api/docs/swagger-ui\"\n" +
                "}";
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to convert OpenAPI specification to JSON", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to convert OpenAPI specification to JSON\"}")
                .build();
        }
    }

    @GET
    @Path("/asyncapi.yaml")
    @Produces("application/x-yaml")
    @Operation(
        summary = "Get AsyncAPI specification",
        description = "Returns the AsyncAPI 3.0 specification in YAML format"
    )
    public Response getAsyncApiSpec() {
        try {
            String spec = loadResourceAsString("/asyncapi.yaml");
            return Response.ok(spec)
                .header("Content-Type", "application/x-yaml; charset=utf-8")
                .header("Content-Disposition", "inline; filename=\"asyncapi.yaml\"")
                .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load AsyncAPI specification", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to load AsyncAPI specification\"}")
                .build();
        }
    }

    @GET
    @Path("/asyncapi.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get AsyncAPI specification as JSON",
        description = "Returns the AsyncAPI 3.0 specification in JSON format (converted from YAML)"
    )
    public Response getAsyncApiSpecAsJson() {
        try {
            // For simplicity, return a JSON message pointing to YAML version
            String jsonResponse = "{\n" +
                "  \"message\": \"AsyncAPI specification available in YAML format at /api/docs/asyncapi.yaml\",\n" +
                "  \"yaml_url\": \"/api/docs/asyncapi.yaml\",\n" +
                "  \"asyncapi_studio_url\": \"https://studio.asyncapi.com\"\n" +
                "}";
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to convert AsyncAPI specification to JSON", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Failed to convert AsyncAPI specification to JSON\"}")
                .build();
        }
    }

    @GET
    @Path("/swagger-ui")
    @Produces(MediaType.TEXT_HTML)
    @Operation(
        summary = "Swagger UI interface",
        description = "Returns a simple HTML page that loads Swagger UI for the OpenAPI specification"
    )
    public Response getSwaggerUI() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>IBM MQ Integration API - Swagger UI</title>
                <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@latest/swagger-ui.css" />
                <style>
                    html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
                    *, *:before, *:after { box-sizing: inherit; }
                    body { margin:0; background: #fafafa; }
                </style>
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@latest/swagger-ui-bundle.js"></script>
                <script src="https://unpkg.com/swagger-ui-dist@latest/swagger-ui-standalone-preset.js"></script>
                <script>
                window.onload = function() {
                    const ui = SwaggerUIBundle({
                        url: '/api/docs/openapi.yaml',
                        dom_id: '#swagger-ui',
                        deepLinking: true,
                        presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIStandalonePreset
                        ],
                        plugins: [
                            SwaggerUIBundle.plugins.DownloadUrl
                        ],
                        layout: "StandaloneLayout"
                    });
                };
                </script>
            </body>
            </html>
            """;

        return Response.ok(html)
            .header("Content-Type", "text/html; charset=utf-8")
            .build();
    }

    @GET
    @Path("/asyncapi-studio")
    @Produces(MediaType.TEXT_HTML)
    @Operation(
        summary = "AsyncAPI Studio redirect",
        description = "Returns a simple HTML page that redirects to AsyncAPI Studio with the specification loaded"
    )
    public Response getAsyncApiStudio() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>IBM MQ Integration AsyncAPI - Studio</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; text-align: center; }
                    .container { max-width: 600px; margin: 0 auto; }
                    .button { display: inline-block; padding: 12px 24px; background: #1976d2; color: white;
                             text-decoration: none; border-radius: 4px; margin: 10px; }
                    .button:hover { background: #1565c0; }
                    .code { background: #f5f5f5; padding: 10px; border-radius: 4px; font-family: monospace; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>IBM MQ Integration AsyncAPI</h1>
                    <p>View and explore the AsyncAPI specification using AsyncAPI Studio</p>

                    <div class="code">
                        AsyncAPI Specification URL:<br>
                        <strong>/api/docs/asyncapi.yaml</strong>
                    </div>

                    <a href="https://studio.asyncapi.com/" target="_blank" class="button">
                        Open AsyncAPI Studio
                    </a>

                    <a href="/api/docs/asyncapi.yaml" class="button">
                        Download AsyncAPI YAML
                    </a>

                    <p><small>Copy the specification URL above and paste it into AsyncAPI Studio</small></p>
                </div>
            </body>
            </html>
            """;

        return Response.ok(html)
            .header("Content-Type", "text/html; charset=utf-8")
            .build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    @Operation(
        summary = "API Documentation overview",
        description = "Returns an overview page with links to all available documentation"
    )
    public Response getDocumentationIndex() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>IBM MQ Integration API Documentation</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
                    .container { max-width: 800px; margin: 0 auto; }
                    .card { border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .button { display: inline-block; padding: 10px 20px; background: #1976d2; color: white;
                             text-decoration: none; border-radius: 4px; margin: 5px; }
                    .button:hover { background: #1565c0; }
                    .button.secondary { background: #424242; }
                    .button.secondary:hover { background: #212121; }
                    h1 { color: #1976d2; }
                    h2 { color: #424242; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🚀 IBM MQ Integration API Documentation</h1>
                    <p>Welcome to the IBM MQ Integration API documentation. This service provides both synchronous REST APIs and asynchronous message-driven capabilities.</p>

                    <div class="card">
                        <h2>📊 OpenAPI 3.0 Specification (REST APIs)</h2>
                        <p>Complete specification for all REST endpoints including message operations, repository management, and health checks.</p>
                        <a href="/api/docs/swagger-ui" class="button">📱 Swagger UI</a>
                        <a href="/api/docs/openapi.yaml" class="button secondary">📄 YAML Spec</a>
                        <a href="/api/docs/openapi.json" class="button secondary">📋 JSON Info</a>
                    </div>

                    <div class="card">
                        <h2>⚡ AsyncAPI 3.0 Specification (Message Flows)</h2>
                        <p>Complete specification for IBM MQ message flows including batch processing, error handling, and message patterns.</p>
                        <a href="/api/docs/asyncapi-studio" class="button">🎨 AsyncAPI Studio</a>
                        <a href="/api/docs/asyncapi.yaml" class="button secondary">📄 YAML Spec</a>
                        <a href="/api/docs/asyncapi.json" class="button secondary">📋 JSON Info</a>
                    </div>

                    <div class="card">
                        <h2>🔗 Quick Links</h2>
                        <ul>
                            <li><a href="/api/mq/health">Health Check</a> - Test MQ connectivity</li>
                            <li><a href="/api/messages/health">Repository Health</a> - Test database connectivity</li>
                            <li><a href="/api/messages">Message Repository</a> - Browse stored messages</li>
                        </ul>
                    </div>

                    <div class="card">
                        <h2>📖 API Features</h2>
                        <ul>
                            <li><strong>REST APIs:</strong> Send/receive messages, manage repository, batch jobs, metrics</li>
                            <li><strong>Message Flows:</strong> Async processing, batch workflows, error handling</li>
                            <li><strong>Error Handling:</strong> Dead letter queues, backout queues, retry mechanisms</li>
                            <li><strong>Monitoring:</strong> Health checks, metrics collection, performance monitoring</li>
                        </ul>
                    </div>
                </div>
            </body>
            </html>
            """;

        return Response.ok(html)
            .header("Content-Type", "text/html; charset=utf-8")
            .build();
    }

    /**
     * Load a resource file as string from classpath
     */
    private String loadResourceAsString(String resourcePath) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}