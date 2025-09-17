package com.example.ibmmq.unit.specification;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validation tests for OpenAPI and AsyncAPI specifications
 */
class SpecificationValidationTest {

    @Test
    void openApiSpecificationShouldBeValidYaml() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/openapi.yaml")) {
            assertThat(is).isNotNull();

            Yaml yaml = new Yaml();
            Map<String, Object> spec = yaml.load(is);

            // Validate basic OpenAPI structure
            assertThat(spec).containsKey("openapi");
            assertThat(spec).containsKey("info");
            assertThat(spec).containsKey("paths");
            assertThat(spec.get("openapi")).isEqualTo("3.0.3");

            // Validate info section
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) spec.get("info");
            assertThat(info).containsKey("title");
            assertThat(info).containsKey("version");
            assertThat(info.get("title")).isEqualTo("IBM MQ Integration API");
        }
    }

    @Test
    void asyncApiSpecificationShouldBeValidYaml() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/asyncapi.yaml")) {
            assertThat(is).isNotNull();

            Yaml yaml = new Yaml();
            Map<String, Object> spec = yaml.load(is);

            // Validate basic AsyncAPI structure
            assertThat(spec).containsKey("asyncapi");
            assertThat(spec).containsKey("info");
            assertThat(spec).containsKey("channels");
            assertThat(spec.get("asyncapi")).isEqualTo("3.0.0");

            // Validate info section
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) spec.get("info");
            assertThat(info).containsKey("title");
            assertThat(info).containsKey("version");
            assertThat(info.get("title")).isEqualTo("IBM MQ Integration AsyncAPI");
        }
    }

    @Test
    void openApiShouldContainExpectedEndpoints() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/openapi.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> spec = yaml.load(is);

            @SuppressWarnings("unchecked")
            Map<String, Object> paths = (Map<String, Object>) spec.get("paths");

            // Check for key endpoints
            assertThat(paths).containsKey("/mq/send/{queue}");
            assertThat(paths).containsKey("/mq/receive/{queue}");
            assertThat(paths).containsKey("/messages");
            assertThat(paths).containsKey("/mq/health");
        }
    }

    @Test
    void asyncApiShouldContainExpectedChannels() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/asyncapi.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> spec = yaml.load(is);

            @SuppressWarnings("unchecked")
            Map<String, Object> channels = (Map<String, Object>) spec.get("channels");

            // Check for key channels
            assertThat(channels).containsKey("request-queue");
            assertThat(channels).containsKey("response-queue");
            assertThat(channels).containsKey("batch-processing-queue");
            assertThat(channels).containsKey("dead-letter-queue");
        }
    }
}