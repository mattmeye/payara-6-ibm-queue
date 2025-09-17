package com.example.ibmmq.integration.rest;

import com.example.ibmmq.client.ApiException;
import com.example.ibmmq.client.model.*;
import com.example.ibmmq.integration.util.IBMMQClientConfiguration;
import com.example.ibmmq.integration.util.MQTestOperations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("IBM MQ REST API Integration Tests with Generated Client")
class IBMMQResourceClientIntegrationTest {

    private MQTestOperations mqOperations;

    @BeforeAll
    void setUp() throws InterruptedException {
        // Use localhost since Payara Micro is running locally
        String baseUrl = "http://localhost:8080/api";

        IBMMQClientConfiguration clientConfig = IBMMQClientConfiguration.withBaseUrl(baseUrl);
        mqOperations = new MQTestOperations(clientConfig);

        // Wait for services to be healthy
        mqOperations.waitForHealthyServices(Duration.ofMinutes(2));
    }

    @Test
    @DisplayName("Should send message to default queue using generated client")
    void shouldSendMessageToDefaultQueue() throws ApiException {
        // Given
        String testMessage = MQTestOperations.generateTestMessage("Integration Test");

        // When
        SuccessResponse response = mqOperations.sendMessage(testMessage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getMessage()).containsIgnoringCase("sent successfully");
    }

    @Test
    @DisplayName("Should send message to specific queue using generated client")
    void shouldSendMessageToSpecificQueue() throws ApiException {
        // Given
        String queueName = "DEV.QUEUE.1";
        String testMessage = MQTestOperations.generateTestMessage("Specific Queue Test");

        // When
        SuccessResponse response = mqOperations.sendMessageToQueue(queueName, testMessage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getMessage()).contains(queueName);
    }

    @Test
    @DisplayName("Should receive message from default queue using generated client")
    void shouldReceiveMessageFromDefaultQueue() throws ApiException {
        // Given - Send a message first
        String testMessage = MQTestOperations.generateTestMessage("Receive Test");
        mqOperations.sendMessage(testMessage);

        // When
        MessageResponse response = mqOperations.receiveMessage();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("success");
        // Message might be the one we sent or another test message
        assertThat(response.getMessage()).isNotNull();
    }

    @Test
    @DisplayName("Should perform send-receive operation using generated client")
    void shouldPerformSendReceiveOperation() throws ApiException {
        // Given
        String testMessage = MQTestOperations.generateTestMessage("Send-Receive Test");

        // When
        ResponseMessage response = mqOperations.sendAndReceive(testMessage);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getResponse()).isNotNull();
    }

    @Test
    @DisplayName("Should check MQ health using generated client")
    void shouldCheckMQHealth() {
        // When
        boolean isHealthy = mqOperations.isMQHealthy();

        // Then
        assertThat(isHealthy).isTrue();
    }

    @Test
    @DisplayName("Should check Messages service health using generated client")
    void shouldCheckMessagesServiceHealth() {
        // When
        boolean isHealthy = mqOperations.isMessagesServiceHealthy();

        // Then
        assertThat(isHealthy).isTrue();
    }

    @Test
    @DisplayName("Should get messages by queue using generated client")
    void shouldGetMessagesByQueue() throws ApiException {
        // Given
        String queueName = "DEV.QUEUE.1";
        String testMessage = MQTestOperations.generateTestMessage("Queue Filter Test");

        // Send a message to ensure there's at least one
        mqOperations.sendMessageToQueue(queueName, testMessage);

        // When
        MessageList messageList = mqOperations.getMessagesByQueue(queueName);

        // Then
        assertThat(messageList).isNotNull();
        assertThat(messageList.getMessages()).isNotNull();
        // May be empty if messages were already processed
    }

    @Test
    @DisplayName("Should get message count by status using generated client")
    void shouldGetMessageCountByStatus() throws ApiException {
        // When
        CountResponse response = mqOperations.getMessageCountByStatus("PROCESSED");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCount()).isNotNull();
        assertThat(response.getCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should send multiple messages for load testing")
    void shouldSendMultipleMessages() throws ApiException {
        // Given
        String baseMessage = MQTestOperations.generateTestMessage("Load Test");
        int messageCount = 5;

        // When & Then - Should not throw exceptions
        assertThatCode(() -> mqOperations.sendMultipleMessages(baseMessage, messageCount))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle API exceptions gracefully")
    void shouldHandleApiExceptionsGracefully() {
        // When trying to send to an invalid queue
        assertThatThrownBy(() ->
            mqOperations.sendMessageToQueue("INVALID.QUEUE.NAME", "test message")
        ).isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("Should validate response models have expected fields")
    void shouldValidateResponseModels() throws ApiException {
        // Given
        String testMessage = MQTestOperations.generateTestMessage("Model Validation Test");

        // When
        SuccessResponse response = mqOperations.sendMessage(testMessage);

        // Then - Validate all expected fields are present
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getMessage()).isNotNull();
    }
}