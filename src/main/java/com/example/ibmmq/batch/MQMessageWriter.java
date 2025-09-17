package com.example.ibmmq.batch;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.repository.MQMessageRepository;
import jakarta.batch.api.chunk.ItemWriter;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
@Named
public class MQMessageWriter implements ItemWriter {

    private static final Logger LOGGER = Logger.getLogger(MQMessageWriter.class.getName());

    @Inject
    private MQMessageRepository messageRepository;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        LOGGER.info("Opening MQ Message Writer");
    }

    @Override
    public void close() throws Exception {
        LOGGER.info("Closing MQ Message Writer");
    }

    @Override
    public void writeItems(List<Object> items) throws Exception {
        LOGGER.info("Writing " + items.size() + " messages to PostgreSQL");

        int successCount = 0;
        int errorCount = 0;

        for (Object item : items) {
            if (!(item instanceof MQMessage)) {
                String itemType = item != null ? item.getClass().getName() : "null";
                LOGGER.warning("Expected MQMessage but got: " + itemType);
                errorCount++;
                continue;
            }

            MQMessage message = (MQMessage) item;

            try {
                if (messageRepository.findByMessageId(message.getMessageId()).isPresent()) {
                    LOGGER.warning("Message with ID already exists, updating: " + message.getMessageId());
                    MQMessage existing = messageRepository.findByMessageId(message.getMessageId()).get();
                    existing.setMessageContent(message.getMessageContent());
                    existing.setStatus(message.getStatus());
                    existing.setErrorMessage(message.getErrorMessage());
                    existing.setProcessedAt(message.getProcessedAt());
                    existing.setRetryCount(message.getRetryCount());
                    messageRepository.save(existing);
                } else {
                    messageRepository.save(message);
                }

                successCount++;
                LOGGER.fine("Successfully saved message ID: " + message.getMessageId());

            } catch (Exception e) {
                errorCount++;
                LOGGER.log(Level.SEVERE, "Failed to save message ID: " + message.getMessageId(), e);

                try {
                    message.markAsFailed("Database save error: " + e.getMessage());
                    messageRepository.save(message);
                } catch (Exception saveErrorException) {
                    LOGGER.log(Level.SEVERE, "Failed to save error state for message ID: " + message.getMessageId(), saveErrorException);
                }
            }
        }

        LOGGER.info("Batch write completed - Success: " + successCount + ", Errors: " + errorCount);

        if (errorCount > 0) {
            throw new Exception("Failed to write " + errorCount + " out of " + items.size() + " messages");
        }
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }
}