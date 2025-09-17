package com.example.ibmmq.openapi;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.examples.Example;
import org.eclipse.microprofile.openapi.models.media.Content;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.eclipse.microprofile.openapi.OASFactory;

import java.util.Map;

/**
 * OpenAPI Filter that enhances the automatically generated OpenAPI specification
 * with additional responses, examples, and security configurations.
 */
public class IBMMQOpenApiFilter implements OASFilter {

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        // Add security schemes
        addSecuritySchemes(openAPI);

        // Add global security requirement
        SecurityRequirement securityRequirement = OASFactory.createSecurityRequirement()
            .addScheme("bearerAuth");
        openAPI.addSecurityRequirement(securityRequirement);
    }

    @Override
    public Operation filterOperation(Operation operation) {
        // Add common response examples
        enhanceResponses(operation);

        // Add request examples for POST operations
        enhanceRequestExamples(operation);

        return operation;
    }

    @Override
    public Parameter filterParameter(Parameter parameter) {
        // Add examples to path parameters
        if (parameter.getIn() == Parameter.In.PATH) {
            if ("queue".equals(parameter.getName())) {
                parameter.example("DEV.QUEUE.1");
                parameter.description("IBM MQ queue name (format: [ENV].[TYPE].[NUMBER])");
            } else if ("id".equals(parameter.getName())) {
                parameter.example("12345");
                parameter.description("Database record ID");
            } else if ("status".equals(parameter.getName())) {
                parameter.example("PROCESSED");
                parameter.description("Message processing status");
            }
        }

        // Add examples to query parameters
        if (parameter.getIn() == Parameter.In.QUERY) {
            if ("days".equals(parameter.getName())) {
                parameter.example("30");
                parameter.description("Number of days to retain messages");
            }
        }

        return parameter;
    }

    @Override
    public APIResponse filterAPIResponse(APIResponse apiResponse) {
        // Enhance response content with examples
        if (apiResponse.getContent() != null) {
            Map<String, MediaType> contentMap = apiResponse.getContent().getMediaTypes();
            if (contentMap != null) {
                for (Map.Entry<String, MediaType> entry : contentMap.entrySet()) {
                    String mediaTypeKey = entry.getKey();
                    MediaType mediaType = entry.getValue();

                    if ("application/json".equals(mediaTypeKey)) {
                        addJsonExamples(mediaType, apiResponse);
                    }
                }
            }
        }

        return apiResponse;
    }

    private void addSecuritySchemes(OpenAPI openAPI) {
        SecurityScheme bearerAuth = OASFactory.createSecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT Bearer token authentication");

        if (openAPI.getComponents() == null) {
            openAPI.components(OASFactory.createComponents());
        }

        openAPI.getComponents().addSecurityScheme("bearerAuth", bearerAuth);
    }

    private void enhanceResponses(Operation operation) {
        if (operation.getResponses() == null) {
            operation.responses(OASFactory.createAPIResponses());
        }

        APIResponses responses = operation.getResponses();

        // Add common error responses if not present
        if (!responses.hasAPIResponse("400")) {
            APIResponse badRequest = OASFactory.createAPIResponse()
                .description("Bad Request - Invalid input parameters")
                .content(OASFactory.createContent()
                    .addMediaType("application/json", createErrorMediaType("Invalid request parameters")));
            responses.addAPIResponse("400", badRequest);
        }

        if (!responses.hasAPIResponse("401")) {
            APIResponse unauthorized = OASFactory.createAPIResponse()
                .description("Unauthorized - Authentication required")
                .content(OASFactory.createContent()
                    .addMediaType("application/json", createErrorMediaType("Authentication token required")));
            responses.addAPIResponse("401", unauthorized);
        }

        if (!responses.hasAPIResponse("500")) {
            APIResponse serverError = OASFactory.createAPIResponse()
                .description("Internal Server Error")
                .content(OASFactory.createContent()
                    .addMediaType("application/json", createErrorMediaType("Internal server error occurred")));
            responses.addAPIResponse("500", serverError);
        }
    }

    private void enhanceRequestExamples(Operation operation) {
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();

            // Add examples for text/plain content
            if (content.hasMediaType("text/plain")) {
                MediaType textMediaType = content.getMediaType("text/plain");
                if (textMediaType.getExamples() == null) {
                    textMediaType.examples(Map.of(
                        "simple", createExample("Simple text message", "Hello World"),
                        "json", createExample("JSON message", "{\"orderId\": \"12345\", \"customer\": \"John Doe\"}"),
                        "xml", createExample("XML message", "<order><id>12345</id><customer>John Doe</customer></order>")
                    ));
                }
            }
        }
    }

    private void addJsonExamples(MediaType mediaType, APIResponse response) {
        String description = response.getDescription();

        if (description != null) {
            Map<String, Example> examples = Map.of();

            if (description.contains("success")) {
                examples = Map.of(
                    "success", createExample("Successful operation",
                        "{\"status\":\"success\",\"message\":\"Operation completed successfully\"}")
                );
            } else if (description.contains("error") || description.contains("fail")) {
                examples = Map.of(
                    "error", createExample("Error response",
                        "{\"status\":\"error\",\"message\":\"Operation failed\"}"),
                    "validation_error", createExample("Validation error",
                        "{\"status\":\"error\",\"message\":\"Invalid input parameters\",\"details\":[\"Field 'id' is required\"]}")
                );
            } else if (description.contains("Message")) {
                examples = Map.of(
                    "with_message", createExample("Message received",
                        "{\"status\":\"success\",\"message\":\"Hello World\"}"),
                    "no_message", createExample("No message available",
                        "{\"status\":\"success\",\"message\":\"No message available\"}")
                );
            } else if (description.contains("health")) {
                examples = Map.of(
                    "healthy", createExample("Service healthy",
                        "{\"status\":\"healthy\",\"service\":\"IBM MQ Integration\",\"timestamp\":\"2023-12-17T12:34:56Z\"}")
                );
            }

            if (!examples.isEmpty()) {
                mediaType.examples(examples);
            }
        }
    }

    private MediaType createErrorMediaType(String errorMessage) {
        Schema errorSchema = OASFactory.createSchema()
            .type(Schema.SchemaType.OBJECT)
            .addProperty("status", OASFactory.createSchema()
                .type(Schema.SchemaType.STRING)
                .example("error"))
            .addProperty("message", OASFactory.createSchema()
                .type(Schema.SchemaType.STRING)
                .example(errorMessage));

        return OASFactory.createMediaType()
            .schema(errorSchema)
            .example("{\"status\":\"error\",\"message\":\"" + errorMessage + "\"}");
    }

    private Example createExample(String summary, String value) {
        return OASFactory.createExample()
            .summary(summary)
            .value(value);
    }
}