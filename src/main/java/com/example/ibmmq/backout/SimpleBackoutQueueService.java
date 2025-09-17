package com.example.ibmmq.backout;

import com.example.ibmmq.config.BackoutQueueConfig;
import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.repository.MQMessageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.*;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SimpleBackoutQueueService {

    private static final Logger LOGGER = Logger.getLogger(SimpleBackoutQueueService.class.getName());

    @Inject
    private IBMMQConnectionPool connectionPool;

    @Inject
    private BackoutQueueConfig backoutConfig;

    @Inject
    private IBMMQConfig mqConfig;

    @Inject
    private MQMessageRepository messageRepository;

    /**
     * Sendet eine Nachricht UNVERÄNDERT in die Backout Queue
     * Funktioniert wie eine Dead Letter Queue - keine Redelivery
     */
    public void sendToBackoutQueue(MQMessage mqMessage, String errorReason) {
        if (!backoutConfig.isBackoutEnabled()) {
            LOGGER.warning("Backout queues disabled for message: " + mqMessage.getMessageId());
            // Fallback zu normalem Failed-Status
            mqMessage.markAsFailed(errorReason);
            messageRepository.save(mqMessage);
            return;
        }

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            String backoutQueueName = mqMessage.getQueueName() + backoutConfig.getBackoutQueueSuffix();
            Queue backoutQueue = session.createQueue(backoutQueueName);
            MessageProducer producer = session.createProducer(backoutQueue);

            // Originale Nachricht UNVERÄNDERT senden
            TextMessage backoutMessage = session.createTextMessage(mqMessage.getMessageContent());

            // Nur minimale Metadaten hinzufügen (optional)
            backoutMessage.setStringProperty("ORIGINAL_QUEUE", mqMessage.getQueueName());
            backoutMessage.setStringProperty("ORIGINAL_MESSAGE_ID", mqMessage.getMessageId());
            backoutMessage.setStringProperty("BACKOUT_REASON", errorReason);
            backoutMessage.setStringProperty("BACKOUT_TIMESTAMP", LocalDateTime.now().toString());
            backoutMessage.setStringProperty("APP_ID", "PayaraIBMMQApp");

            // Originale JMS-Properties beibehalten
            if (mqMessage.getCorrelationId() != null) {
                backoutMessage.setJMSCorrelationID(mqMessage.getCorrelationId());
            }
            if (mqMessage.getPriority() != null) {
                backoutMessage.setJMSPriority(mqMessage.getPriority());
            }

            producer.send(backoutMessage);

            // Datenbank-Status aktualisieren
            mqMessage.markAsBackout(errorReason);
            messageRepository.save(mqMessage);

            LOGGER.info("Message sent to Backout Queue: " + backoutQueueName +
                       " - Message ID: " + mqMessage.getMessageId() +
                       " - Reason: " + errorReason);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send message to Backout Queue: " + mqMessage.getMessageId(), e);
            // Fallback: Message als failed markieren
            mqMessage.markAsFailed("Backout failed: " + e.getMessage() + " | Original: " + errorReason);
            messageRepository.save(mqMessage);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    /**
     * Sendet originale JMS Message unverändert in Backout Queue
     */
    public void sendToBackoutQueue(String originalQueue, Message originalMessage, String errorReason) {
        if (!backoutConfig.isBackoutEnabled()) {
            LOGGER.warning("Backout queues disabled, message will be lost!");
            return;
        }

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            String backoutQueueName = originalQueue + backoutConfig.getBackoutQueueSuffix();
            Queue backoutQueue = session.createQueue(backoutQueueName);
            MessageProducer producer = session.createProducer(backoutQueue);

            // Originalnaricht kopieren und senden
            Message backoutMessage = copyMessage(session, originalMessage);

            // Nur minimale Backout-Metadaten hinzufügen
            backoutMessage.setStringProperty("ORIGINAL_QUEUE", originalQueue);
            backoutMessage.setStringProperty("BACKOUT_REASON", errorReason);
            backoutMessage.setStringProperty("BACKOUT_TIMESTAMP", LocalDateTime.now().toString());
            backoutMessage.setStringProperty("APP_ID", "PayaraIBMMQApp");

            producer.send(backoutMessage);

            LOGGER.info("Original message sent to Backout Queue: " + backoutQueueName +
                       " - Message ID: " + originalMessage.getJMSMessageID() +
                       " - Reason: " + errorReason);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send original message to Backout Queue", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    /**
     * Statistiken für Backout Queue
     */
    public BackoutQueueStats getBackoutQueueStats(String originalQueueName) {
        String backoutQueueName = originalQueueName + backoutConfig.getBackoutQueueSuffix();

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            int backoutMessageCount = countMessagesInQueue(session, backoutQueueName);
            long dbBackoutCount = messageRepository.countByStatus(MQMessage.MessageStatus.BACKOUT);

            return new BackoutQueueStats(backoutQueueName, backoutMessageCount, dbBackoutCount);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get backout queue stats for: " + originalQueueName, e);
            return new BackoutQueueStats(backoutQueueName, -1, -1);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    /**
     * Verschiebt ALLE Nachrichten von der Backout Queue zurück zur Original-Queue
     */
    public int moveAllBackToOriginalQueue(String backoutQueueName, String originalQueueName) {
        Connection connection = null;
        int movedCount = 0;
        try {
            connection = connectionPool.getConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue backoutQueue = session.createQueue(backoutQueueName);
            Queue originalQueue = session.createQueue(originalQueueName);

            MessageConsumer consumer = session.createConsumer(backoutQueue);
            MessageProducer producer = session.createProducer(originalQueue);
            connection.start();

            Message message;
            while ((message = consumer.receive(1000)) != null) {
                // Backout-Properties entfernen und zurück zur Original-Queue senden
                Message cleanMessage = copyMessage(session, message);
                cleanMessage.clearProperties();
                copyOriginalProperties(message, cleanMessage);

                producer.send(cleanMessage);
                movedCount++;
            }

            LOGGER.info("Moved " + movedCount + " messages from " + backoutQueueName + " back to " + originalQueueName);
            return movedCount;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to move all messages back from " + backoutQueueName + " to " + originalQueueName, e);
            return movedCount;
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    /**
     * Verschiebt eine bestimmte Anzahl von Nachrichten von der Backout Queue zurück zur Original-Queue
     */
    public int moveBatchBackToOriginalQueue(String backoutQueueName, String originalQueueName, int batchSize) {
        Connection connection = null;
        int movedCount = 0;
        try {
            connection = connectionPool.getConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue backoutQueue = session.createQueue(backoutQueueName);
            Queue originalQueue = session.createQueue(originalQueueName);

            MessageConsumer consumer = session.createConsumer(backoutQueue);
            MessageProducer producer = session.createProducer(originalQueue);
            connection.start();

            Message message;
            while (movedCount < batchSize && (message = consumer.receive(1000)) != null) {
                // Backout-Properties entfernen und zurück zur Original-Queue senden
                Message cleanMessage = copyMessage(session, message);
                cleanMessage.clearProperties();
                copyOriginalProperties(message, cleanMessage);

                producer.send(cleanMessage);
                movedCount++;
            }

            LOGGER.info("Moved " + movedCount + " messages (batch) from " + backoutQueueName + " back to " + originalQueueName);
            return movedCount;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to move batch messages back from " + backoutQueueName + " to " + originalQueueName, e);
            return movedCount;
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    private Message copyMessage(Session session, Message originalMessage) throws JMSException {
        Message copy;

        if (originalMessage instanceof TextMessage) {
            copy = session.createTextMessage(((TextMessage) originalMessage).getText());
        } else if (originalMessage instanceof BytesMessage) {
            BytesMessage originalBytes = (BytesMessage) originalMessage;
            BytesMessage copyBytes = session.createBytesMessage();
            byte[] buffer = new byte[(int) originalBytes.getBodyLength()];
            originalBytes.readBytes(buffer);
            copyBytes.writeBytes(buffer);
            copy = copyBytes;
        } else if (originalMessage instanceof ObjectMessage) {
            copy = session.createObjectMessage(((ObjectMessage) originalMessage).getObject());
        } else {
            // MapMessage, StreamMessage, etc.
            copy = session.createTextMessage(originalMessage.toString());
        }

        // Standard JMS Headers kopieren
        if (originalMessage.getJMSCorrelationID() != null) {
            copy.setJMSCorrelationID(originalMessage.getJMSCorrelationID());
        }
        copy.setJMSPriority(originalMessage.getJMSPriority());
        copy.setJMSExpiration(originalMessage.getJMSExpiration());

        return copy;
    }

    private void copyOriginalProperties(Message from, Message to) throws JMSException {
        java.util.Enumeration<?> propertyNames = from.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();

            // Backout-Properties nicht kopieren
            if (!propertyName.startsWith("BACKOUT_") &&
                !propertyName.equals("ORIGINAL_QUEUE") &&
                !propertyName.equals("ORIGINAL_MESSAGE_ID")) {

                Object propertyValue = from.getObjectProperty(propertyName);
                to.setObjectProperty(propertyName, propertyValue);
            }
        }
    }

    private int countMessagesInQueue(Session session, String queueName) throws JMSException {
        Queue queue = session.createQueue(queueName);
        QueueBrowser browser = session.createBrowser(queue);

        int count = 0;
        java.util.Enumeration<?> messages = browser.getEnumeration();
        while (messages.hasMoreElements()) {
            messages.nextElement();
            count++;
        }

        return count;
    }

    public static class BackoutQueueStats {
        private final String backoutQueueName;
        private final int backoutMessageCount;
        private final long dbBackoutCount;

        public BackoutQueueStats(String backoutQueueName, int backoutMessageCount, long dbBackoutCount) {
            this.backoutQueueName = backoutQueueName;
            this.backoutMessageCount = backoutMessageCount;
            this.dbBackoutCount = dbBackoutCount;
        }

        public String getBackoutQueueName() { return backoutQueueName; }
        public int getBackoutMessageCount() { return backoutMessageCount; }
        public long getDbBackoutCount() { return dbBackoutCount; }
    }
}