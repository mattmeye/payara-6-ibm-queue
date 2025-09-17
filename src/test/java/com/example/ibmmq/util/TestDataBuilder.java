package com.example.ibmmq.util;

import com.example.ibmmq.entity.MQMessage;

import java.time.LocalDateTime;

public class TestDataBuilder {

    public static MQMessage createTestMessage() {
        return createTestMessage("TEST_MESSAGE_ID", "TEST.QUEUE", "Test message content");
    }

    public static MQMessage createTestMessage(String messageId, String queueName, String content) {
        MQMessage message = new MQMessage(messageId, queueName, content);
        message.setMessageType("TEXT");
        message.setPriority(4);
        message.setReceivedAt(LocalDateTime.now());
        return message;
    }

    public static MQMessage createProcessedMessage() {
        MQMessage message = createTestMessage();
        message.markAsProcessed();
        return message;
    }

    public static MQMessage createFailedMessage() {
        MQMessage message = createTestMessage();
        message.markAsFailed("Test error");
        return message;
    }

    public static MQMessage createRetryMessage() {
        MQMessage message = createTestMessage();
        message.incrementRetryCount();
        return message;
    }

    public static MQMessage createMessageWithCorrelation(String correlationId) {
        MQMessage message = createTestMessage();
        message.setCorrelationId(correlationId);
        return message;
    }

    public static MQMessage createLargeMessage() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            content.append("This is a large test message content. ");
        }
        return createTestMessage("LARGE_MESSAGE_ID", "TEST.QUEUE", content.toString());
    }

    public static MQMessage createMessageWithSpecialCharacters() {
        return createTestMessage("SPECIAL_CHARS_ID", "TEST.QUEUE",
            "Test message with special chars: äöü ß €  áéíóú ñ 中文 日本語 한국어");
    }

    public static MQMessage createExpiredMessage() {
        MQMessage message = createTestMessage();
        message.setExpiry(System.currentTimeMillis() - 10000); // Expired 10 seconds ago
        return message;
    }

    public static MQMessage[] createBatchOfMessages(int count) {
        MQMessage[] messages = new MQMessage[count];
        for (int i = 0; i < count; i++) {
            messages[i] = createTestMessage("BATCH_MSG_" + i, "BATCH.QUEUE", "Batch message " + i);
        }
        return messages;
    }
}