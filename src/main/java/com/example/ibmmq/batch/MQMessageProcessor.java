package com.example.ibmmq.batch;

import com.example.ibmmq.backout.SimpleBackoutQueueService;
import com.example.ibmmq.config.BackoutQueueConfig;
import com.example.ibmmq.entity.MQMessage;
import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.logging.Logger;

@Dependent
@Named
public class MQMessageProcessor implements ItemProcessor {

    private static final Logger LOGGER = Logger.getLogger(MQMessageProcessor.class.getName());

    @Inject
    private SimpleBackoutQueueService backoutQueueService;

    @Inject
    private BackoutQueueConfig backoutConfig;

    @Override
    public Object processItem(Object item) throws Exception {
        if (!(item instanceof MQMessage)) {
            String itemType = item != null ? item.getClass().getName() : "null";
            LOGGER.warning("Expected MQMessage but got: " + itemType);
            return null;
        }

        MQMessage message = (MQMessage) item;

        LOGGER.info("Processing message ID: " + message.getMessageId());

        try {
            message.markAsProcessing();

            String content = message.getMessageContent();
            if (content != null) {
                content = content.trim();

                if (content.isEmpty()) {
                    LOGGER.warning("Empty message content for message ID: " + message.getMessageId());
                    message.markAsFailed("Empty message content");
                    return message;
                }

                if (content.length() > 10000) {
                    LOGGER.warning("Message content too large for message ID: " + message.getMessageId());
                    content = content.substring(0, 10000) + "... [TRUNCATED]";
                    message.setMessageContent(content);
                }

                content = content.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
                message.setMessageContent(content);
            }

            validateMessage(message);

            message.markAsProcessed();

            LOGGER.info("Successfully processed message ID: " + message.getMessageId());
            return message;

        } catch (Exception e) {
            LOGGER.severe("Error processing message ID: " + message.getMessageId() + " - " + e.getMessage());

            // Check if message should go to backout queue
            if (shouldSendToBackoutQueue(message)) {
                backoutQueueService.sendToBackoutQueue(message, "Processing error: " + e.getMessage());
            } else {
                message.markAsFailed("Processing error: " + e.getMessage());
            }

            return message;
        }
    }

    private void validateMessage(MQMessage message) throws Exception {
        if (message.getMessageId() == null || message.getMessageId().trim().isEmpty()) {
            throw new Exception("Message ID is required");
        }

        if (message.getQueueName() == null || message.getQueueName().trim().isEmpty()) {
            throw new Exception("Queue name is required");
        }

        if (message.getMessageContent() == null) {
            throw new Exception("Message content cannot be null");
        }

        String content = message.getMessageContent().toLowerCase();
        if (content.contains("error") || content.contains("exception") || content.contains("failed")) {
            LOGGER.warning("Message contains error indicators: " + message.getMessageId());
        }
    }

    private boolean shouldSendToBackoutQueue(MQMessage message) {
        // Check if backout queues are enabled
        if (!backoutConfig.isBackoutEnabled()) {
            return false;
        }

        // Check if message has exceeded retry threshold
        if (message.getRetryCount() >= backoutConfig.getBackoutThreshold()) {
            return true;
        }

        // Check if message has been processed multiple times unsuccessfully
        return message.getBackoutCount() > 0;
    }
}