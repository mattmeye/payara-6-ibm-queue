package com.example.ibmmq.integration.rest;

import com.example.ibmmq.client.ApiClient;
import com.example.ibmmq.client.ApiException;
import com.example.ibmmq.client.api.MqApi;
import com.example.ibmmq.client.api.MessagesApi;
import com.example.ibmmq.client.api.HealthApi;
import com.example.ibmmq.client.model.*;
import com.example.ibmmq.integration.util.IBMMQClientConfiguration;
import com.example.ibmmq.integration.util.MQTestOperations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Generated OpenAPI Client Tests")
class GeneratedClientTest {

    @Test
    @DisplayName("Generated client classes should be properly structured")
    void generatedClientClassesShouldBeProperlyStructured() {
        // Test that all generated classes are accessible and can be instantiated
        ApiClient apiClient = new ApiClient();
        assertThat(apiClient).isNotNull();
        assertThat(apiClient.getBasePath()).isNotNull();

        MqApi mqApi = new MqApi(apiClient);
        assertThat(mqApi).isNotNull();

        MessagesApi messagesApi = new MessagesApi(apiClient);
        assertThat(messagesApi).isNotNull();

        HealthApi healthApi = new HealthApi(apiClient);
        assertThat(healthApi).isNotNull();
    }

    @Test
    @DisplayName("Generated model classes should have expected properties")
    void generatedModelClassesShouldHaveExpectedProperties() {
        // Test SuccessResponse model
        SuccessResponse successResponse = new SuccessResponse();
        assertThat(successResponse).isNotNull();

        successResponse.setStatus(SuccessResponse.StatusEnum.SUCCESS);
        successResponse.setMessage("Test message");

        assertThat(successResponse.getStatus()).isEqualTo(SuccessResponse.StatusEnum.SUCCESS);
        assertThat(successResponse.getMessage()).isEqualTo("Test message");

        // Test ResponseMessage model
        ResponseMessage responseMessage = new ResponseMessage();
        assertThat(responseMessage).isNotNull();

        responseMessage.setStatus(ResponseMessage.StatusEnum.SUCCESS);
        responseMessage.setResponse("Test response");

        assertThat(responseMessage.getStatus()).isEqualTo(ResponseMessage.StatusEnum.SUCCESS);
        assertThat(responseMessage.getResponse()).isEqualTo("Test response");

        // Test MessageStatus enum
        MessageStatus status = MessageStatus.RECEIVED;
        assertThat(status).isNotNull();
        assertThat(status.getValue()).isEqualTo("RECEIVED");

        // Test enum conversion
        MessageStatus processedStatus = MessageStatus.fromValue("PROCESSED");
        assertThat(processedStatus).isEqualTo(MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Client configuration utility should work correctly")
    void clientConfigurationUtilityShouldWorkCorrectly() {
        String baseUrl = "http://localhost:8080/api";
        IBMMQClientConfiguration config = IBMMQClientConfiguration.withBaseUrl(baseUrl);

        assertThat(config).isNotNull();
        assertThat(config.getMqApi()).isNotNull();
        assertThat(config.getMessagesApi()).isNotNull();
        assertThat(config.getHealthApi()).isNotNull();
    }

    @Test
    @DisplayName("MQTestOperations utility should be properly structured")
    void mqTestOperationsUtilityShouldBeProperlyStructured() {
        String baseUrl = "http://localhost:8080/api";
        IBMMQClientConfiguration config = IBMMQClientConfiguration.withBaseUrl(baseUrl);
        MQTestOperations operations = new MQTestOperations(config);

        assertThat(operations).isNotNull();

        // Test utility methods
        String testMessage1 = MQTestOperations.generateTestMessage("TEST");
        assertThat(testMessage1).startsWith("TEST - ");
        assertThat(testMessage1).contains(String.valueOf(System.currentTimeMillis()).substring(0, 8)); // Check timestamp prefix

        String testMessage2 = MQTestOperations.generateTestMessage("TEST", "content");
        assertThat(testMessage2).startsWith("TEST - content - ");
    }

    @Test
    @DisplayName("Generated enums should handle all expected values")
    void generatedEnumsShouldHandleAllExpectedValues() {
        // Test MessageStatus enum values
        assertThat(MessageStatus.RECEIVED.getValue()).isEqualTo("RECEIVED");
        assertThat(MessageStatus.PROCESSING.getValue()).isEqualTo("PROCESSING");
        assertThat(MessageStatus.PROCESSED.getValue()).isEqualTo("PROCESSED");
        assertThat(MessageStatus.FAILED.getValue()).isEqualTo("FAILED");
        assertThat(MessageStatus.RETRY.getValue()).isEqualTo("RETRY");
        assertThat(MessageStatus.BACKOUT.getValue()).isEqualTo("BACKOUT");

        // Test enum conversions
        assertThat(MessageStatus.fromValue("RECEIVED")).isEqualTo(MessageStatus.RECEIVED);
        assertThat(MessageStatus.fromValue("PROCESSING")).isEqualTo(MessageStatus.PROCESSING);
        assertThat(MessageStatus.fromValue("PROCESSED")).isEqualTo(MessageStatus.PROCESSED);
        assertThat(MessageStatus.fromValue("FAILED")).isEqualTo(MessageStatus.FAILED);
        assertThat(MessageStatus.fromValue("RETRY")).isEqualTo(MessageStatus.RETRY);
        assertThat(MessageStatus.fromValue("BACKOUT")).isEqualTo(MessageStatus.BACKOUT);

        // Test invalid enum value
        assertThatThrownBy(() -> MessageStatus.fromValue("INVALID"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unexpected value");
    }

    @Test
    @DisplayName("Model serialization should work correctly")
    void modelSerializationShouldWorkCorrectly() {
        SuccessResponse response = new SuccessResponse()
            .status(SuccessResponse.StatusEnum.SUCCESS)
            .message("Test message");

        // Test toJson method
        String json = response.toJson();
        assertThat(json).isNotNull();
        assertThat(json).contains("success");
        assertThat(json).contains("Test message");

        // Test fromJson method
        try {
            SuccessResponse deserialized = SuccessResponse.fromJson(json);
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getStatus()).isEqualTo(SuccessResponse.StatusEnum.SUCCESS);
            assertThat(deserialized.getMessage()).isEqualTo("Test message");
        } catch (IOException e) {
            fail("JSON deserialization should not throw IOException", e);
        }
    }

    @Test
    @DisplayName("API client should be configurable")
    void apiClientShouldBeConfigurable() {
        ApiClient client = new ApiClient();

        // Test base path configuration
        String customBasePath = "http://example.com:9090/api";
        client.setBasePath(customBasePath);
        assertThat(client.getBasePath()).isEqualTo(customBasePath);

        // Test timeout configuration
        client.setConnectTimeout(30000);
        assertThat(client.getConnectTimeout()).isEqualTo(30000);

        client.setReadTimeout(60000);
        assertThat(client.getReadTimeout()).isEqualTo(60000);
    }

    @Test
    @DisplayName("Exception handling should work as expected")
    void exceptionHandlingShouldWorkAsExpected() {
        ApiException exception = new ApiException(404, "Not Found");

        assertThat(exception.getCode()).isEqualTo(404);
        assertThat(exception.getMessage()).contains("Not Found");
        assertThat(exception.getMessage()).contains("404");

        ApiException exceptionWithBody = new ApiException(500, "Internal Server Error", null, "Error body");
        assertThat(exceptionWithBody.getCode()).isEqualTo(500);
        assertThat(exceptionWithBody.getResponseBody()).isEqualTo("Error body");
        assertThat(exceptionWithBody.getMessage()).contains("Internal Server Error");
        assertThat(exceptionWithBody.getMessage()).contains("500");
    }
}