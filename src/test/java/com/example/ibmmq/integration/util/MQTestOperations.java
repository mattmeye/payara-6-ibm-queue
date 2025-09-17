package com.example.ibmmq.integration.util;

import com.example.ibmmq.client.ApiException;
import com.example.ibmmq.client.api.MqApi;
import com.example.ibmmq.client.api.MessagesApi;
import com.example.ibmmq.client.api.HealthApi;
import com.example.ibmmq.client.model.*;

import java.time.Duration;
import java.time.Instant;

/**
 * High-level test operations using the generated OpenAPI client.
 * Provides common test scenarios and utilities for integration tests.
 */
public class MQTestOperations {

    private final MqApi mqApi;
    private final MessagesApi messagesApi;
    private final HealthApi healthApi;

    public MQTestOperations(IBMMQClientConfiguration config) {
        this.mqApi = config.getMqApi();
        this.messagesApi = config.getMessagesApi();
        this.healthApi = config.getHealthApi();
    }

    /**
     * Send a message to the default queue and verify success
     */
    public SuccessResponse sendMessage(String message) throws ApiException {
        return mqApi.mqSendPost(message);
    }

    /**
     * Send a message to a specific queue and verify success
     */
    public SuccessResponse sendMessageToQueue(String queueName, String message) throws ApiException {
        return mqApi.mqSendQueuePost(queueName, message);
    }

    /**
     * Receive a message from the default queue
     */
    public MessageResponse receiveMessage() throws ApiException {
        return mqApi.mqReceiveGet();
    }

    /**
     * Receive a message from a specific queue
     */
    public MessageResponse receiveMessageFromQueue(String queueName) throws ApiException {
        return mqApi.mqReceiveQueueGet(queueName);
    }

    /**
     * Send a message and immediately receive the response
     */
    public ResponseMessage sendAndReceive(String message) throws ApiException {
        return mqApi.mqSendreceivePost(message);
    }

    /**
     * Wait for a message to be processed (polling until status changes)
     */
    public MQMessage waitForMessageProcessing(Long messageId, MessageStatus expectedStatus, Duration timeout) throws ApiException, InterruptedException {
        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            MQMessage message = messagesApi.messagesIdGet(messageId);
            if (expectedStatus.equals(message.getStatus())) {
                return message;
            }
            Thread.sleep(1000); // Wait 1 second before retry
        }

        throw new RuntimeException("Message did not reach expected status within timeout");
    }

    /**
     * Send multiple messages for load testing
     */
    public void sendMultipleMessages(String baseMessage, int count) throws ApiException {
        for (int i = 0; i < count; i++) {
            String message = baseMessage + " #" + (i + 1);
            sendMessage(message);
        }
    }

    /**
     * Clean up old test messages
     */
    public CleanupResponse cleanupTestMessages(Integer days) throws ApiException {
        return messagesApi.messagesCleanupPost(days);
    }

    /**
     * Get message count by status
     */
    public CountResponse getMessageCountByStatus(MessageStatus status) throws ApiException {
        return messagesApi.messagesCountByStatusStatusGet(status);
    }

    /**
     * Get message count by status string
     */
    public CountResponse getMessageCountByStatus(String statusString) throws ApiException {
        // Convert string to MessageStatus enum
        MessageStatus status = MessageStatus.fromValue(statusString);
        return messagesApi.messagesCountByStatusStatusGet(status);
    }

    /**
     * Get all messages from a specific queue
     */
    public MessageList getMessagesByQueue(String queueName) throws ApiException {
        return messagesApi.messagesByQueueQueueNameGet(queueName);
    }

    /**
     * Check if MQ service is healthy
     */
    public boolean isMQHealthy() {
        try {
            HealthResponse health = healthApi.mqHealthGet();
            return health.getStatus() != null && "healthy".equals(health.getStatus().getValue());
        } catch (ApiException e) {
            return false;
        }
    }

    /**
     * Check if Messages service is healthy
     */
    public boolean isMessagesServiceHealthy() {
        try {
            HealthResponse health = healthApi.messagesHealthGet();
            return health.getStatus() != null && "healthy".equals(health.getStatus().getValue());
        } catch (ApiException e) {
            return false;
        }
    }

    /**
     * Wait for services to become healthy
     */
    public void waitForHealthyServices(Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            if (isMQHealthy() && isMessagesServiceHealthy()) {
                return;
            }
            Thread.sleep(2000); // Wait 2 seconds before retry
        }

        throw new RuntimeException("Services did not become healthy within timeout");
    }

    /**
     * Generate a unique test message
     */
    public static String generateTestMessage(String prefix) {
        return prefix + " - " + System.currentTimeMillis();
    }

    /**
     * Generate a test message with specific content
     */
    public static String generateTestMessage(String prefix, String content) {
        return prefix + " - " + content + " - " + System.currentTimeMillis();
    }
}