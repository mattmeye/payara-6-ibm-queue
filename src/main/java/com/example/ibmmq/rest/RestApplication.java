package com.example.ibmmq.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "IBM MQ Integration API",
        version = "1.0.0",
        description = "RESTful API for IBM MQ message processing, monitoring, and management",
        contact = @Contact(
            name = "Development Team",
            email = "dev@example.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development server"),
        @Server(url = "https://api.example.com", description = "Production server")
    },
    tags = {
        @Tag(name = "mq", description = "IBM MQ message operations"),
        @Tag(name = "messages", description = "Message repository and management"),
        @Tag(name = "batch", description = "Batch job processing"),
        @Tag(name = "metrics", description = "Monitoring and metrics"),
        @Tag(name = "health", description = "Health checks")
    }
)
public class RestApplication extends Application {
}