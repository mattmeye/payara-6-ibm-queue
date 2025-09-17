package com.example.ibmmq.unit.entity;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MQMessage Entity Tests")
class MQMessageTest {

    private MQMessage message;

    @BeforeEach
    void setUp() {
        message = TestDataBuilder.createTestMessage();
    }

    @Test
    @DisplayName("Should create message with required fields")
    void shouldCreateMessageWithRequiredFields() {
        // Given
        String messageId = "TEST_ID";
        String queueName = "TEST.QUEUE";
        String content = "Test content";

        // When
        MQMessage newMessage = new MQMessage(messageId, queueName, content);

        // Then
        assertThat(newMessage.getMessageId()).isEqualTo(messageId);
        assertThat(newMessage.getQueueName()).isEqualTo(queueName);
        assertThat(newMessage.getMessageContent()).isEqualTo(content);
        assertThat(newMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.RECEIVED);
        assertThat(newMessage.getRetryCount()).isZero();
        assertThat(newMessage.getReceivedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should mark message as processing")
    void shouldMarkMessageAsProcessing() {
        // When
        message.markAsProcessing();

        // Then
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSING);
    }

    @Test
    @DisplayName("Should mark message as processed with timestamp")
    void shouldMarkMessageAsProcessedWithTimestamp() {
        // Given
        LocalDateTime beforeProcessing = LocalDateTime.now();

        // When
        message.markAsProcessed();

        // Then
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(message.getProcessedAt()).isNotNull();
        assertThat(message.getProcessedAt()).isAfter(beforeProcessing);
    }

    @Test
    @DisplayName("Should mark message as failed with error message")
    void shouldMarkMessageAsFailedWithErrorMessage() {
        // Given
        String errorMessage = "Test error occurred";
        LocalDateTime beforeFailure = LocalDateTime.now();

        // When
        message.markAsFailed(errorMessage);

        // Then
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(message.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(message.getProcessedAt()).isNotNull();
        assertThat(message.getProcessedAt()).isAfter(beforeFailure);
    }

    @Test
    @DisplayName("Should increment retry count and set retry status")
    void shouldIncrementRetryCountAndSetRetryStatus() {
        // Given
        int initialRetryCount = message.getRetryCount();

        // When
        message.incrementRetryCount();

        // Then
        assertThat(message.getRetryCount()).isEqualTo(initialRetryCount + 1);
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.RETRY);
    }

    @Test
    @DisplayName("Should handle multiple retry increments")
    void shouldHandleMultipleRetryIncrements() {
        // When
        message.incrementRetryCount();
        message.incrementRetryCount();
        message.incrementRetryCount();

        // Then
        assertThat(message.getRetryCount()).isEqualTo(3);
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.RETRY);
    }

    @Test
    @DisplayName("Should set all properties correctly")
    void shouldSetAllPropertiesCorrectly() {
        // Given
        String correlationId = "CORR_ID_123";
        String messageType = "BYTES";
        Integer priority = 7;
        Long expiry = System.currentTimeMillis() + 60000;

        // When
        message.setCorrelationId(correlationId);
        message.setMessageType(messageType);
        message.setPriority(priority);
        message.setExpiry(expiry);

        // Then
        assertThat(message.getCorrelationId()).isEqualTo(correlationId);
        assertThat(message.getMessageType()).isEqualTo(messageType);
        assertThat(message.getPriority()).isEqualTo(priority);
        assertThat(message.getExpiry()).isEqualTo(expiry);
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        // When
        message.setCorrelationId(null);
        message.setMessageType(null);
        message.setPriority(null);
        message.setExpiry(null);
        message.markAsFailed(null);

        // Then
        assertThat(message.getCorrelationId()).isNull();
        assertThat(message.getMessageType()).isNull();
        assertThat(message.getPriority()).isNull();
        assertThat(message.getExpiry()).isNull();
        assertThat(message.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should maintain version for optimistic locking")
    void shouldMaintainVersionForOptimisticLocking() {
        // Given
        Long initialVersion = message.getVersion();

        // When
        message.setVersion(5L);

        // Then
        assertThat(message.getVersion()).isEqualTo(5L);
        assertThat(message.getVersion()).isNotEqualTo(initialVersion);
    }

    @Test
    @DisplayName("Should create default message with current timestamp")
    void shouldCreateDefaultMessageWithCurrentTimestamp() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now();

        // When
        MQMessage newMessage = new MQMessage();

        // Then
        assertThat(newMessage.getReceivedAt()).isNotNull();
        assertThat(newMessage.getReceivedAt()).isAfter(beforeCreation.minusSeconds(1));
        assertThat(newMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.RECEIVED);
        assertThat(newMessage.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("Should handle empty message content")
    void shouldHandleEmptyMessageContent() {
        // When
        message.setMessageContent("");

        // Then
        assertThat(message.getMessageContent()).isEmpty();
    }

    @Test
    @DisplayName("Should handle large message content")
    void shouldHandleLargeMessageContent() {
        // Given
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("Large content ");
        }
        String content = largeContent.toString();

        // When
        message.setMessageContent(content);

        // Then
        assertThat(message.getMessageContent()).isEqualTo(content);
        assertThat(message.getMessageContent().length()).isGreaterThan(100000);
    }

    @Test
    @DisplayName("Should handle special characters in content")
    void shouldHandleSpecialCharactersInContent() {
        // Given
        String specialContent = "Special chars: äöü ß € áéíóú ñ 中文 日本語 한국어 🚀 ✅";

        // When
        message.setMessageContent(specialContent);

        // Then
        assertThat(message.getMessageContent()).isEqualTo(specialContent);
    }
}