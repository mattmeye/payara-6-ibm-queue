package com.example.ibmmq.openapi;

import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.info.Contact;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.info.License;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.eclipse.microprofile.openapi.models.tags.Tag;
import org.eclipse.microprofile.openapi.OASFactory;

import java.util.Arrays;
import java.util.List;

/**
 * OpenAPI Model Reader that dynamically generates OpenAPI specification
 * from application structure and configuration.
 */
public class IBMMQOpenApiModelReader implements OASModelReader {

    @Override
    public OpenAPI buildModel() {
        return OASFactory.createOpenAPI()
            .info(createInfo())
            .servers(createServers())
            .tags(createTags());
    }

    private Info createInfo() {
        return OASFactory.createInfo()
            .title("IBM MQ Integration API")
            .version("1.0.0")
            .description("""
                RESTful API for IBM MQ message processing, monitoring, and management.

                This API provides comprehensive functionality for:
                - Sending and receiving messages to/from IBM MQ queues
                - Managing message lifecycle and status
                - Monitoring batch processing jobs
                - Health checks and metrics collection

                ## Features
                - **Message Operations**: Send/receive messages, request-response patterns
                - **Repository Management**: Query, filter, and manage stored messages
                - **Batch Processing**: Monitor and control batch job execution
                - **Health Monitoring**: System health checks and connectivity testing
                - **Metrics Collection**: Performance monitoring and metrics gathering

                ## Authentication
                The API supports Bearer Token authentication (JWT) for secure access.

                ## Error Handling
                All endpoints return consistent error responses with appropriate HTTP status codes.
                """)
            .contact(createContact())
            .license(createLicense());
    }

    private Contact createContact() {
        return OASFactory.createContact()
            .name("Development Team")
            .email("dev@example.com")
            .url("https://example.com/contact");
    }

    private License createLicense() {
        return OASFactory.createLicense()
            .name("Apache 2.0")
            .url("https://www.apache.org/licenses/LICENSE-2.0.html");
    }

    private List<Server> createServers() {
        Server devServer = OASFactory.createServer()
            .url("http://localhost:8080/api")
            .description("Development server");

        Server prodServer = OASFactory.createServer()
            .url("https://api.example.com/api")
            .description("Production server");

        return Arrays.asList(devServer, prodServer);
    }

    private List<Tag> createTags() {
        Tag mqTag = OASFactory.createTag()
            .name("mq")
            .description("IBM MQ message operations - send, receive, and request-response patterns");

        Tag messagesTag = OASFactory.createTag()
            .name("messages")
            .description("Message repository management - query, filter, and maintain message history");

        Tag batchTag = OASFactory.createTag()
            .name("batch")
            .description("Batch job processing - monitor and control batch operations");

        Tag metricsTag = OASFactory.createTag()
            .name("metrics")
            .description("Monitoring and metrics - performance data and system statistics");

        Tag healthTag = OASFactory.createTag()
            .name("health")
            .description("Health checks - system connectivity and service availability");

        Tag docsTag = OASFactory.createTag()
            .name("docs")
            .description("API Documentation - OpenAPI and AsyncAPI specifications");

        return Arrays.asList(mqTag, messagesTag, batchTag, metricsTag, healthTag, docsTag);
    }
}