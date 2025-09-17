package com.example.ibmmq.transaction;

import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.repository.MQMessageRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.*;
import jakarta.transaction.Transactional;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TransactionalMQService {

    private static final Logger LOGGER = Logger.getLogger(TransactionalMQService.class.getName());

    @Inject
    private IBMMQConnectionPool connectionPool;

    @Inject
    private IBMMQConfig config;

    @Inject
    private MQMessageRepository messageRepository;

    @Transactional
    public void sendMessageTransactional(String queueName, String message) {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);

            TextMessage textMessage = session.createTextMessage(message);
            textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            textMessage.setStringProperty("APP_ID", "PayaraIBMMQApp");
            textMessage.setLongProperty("TIMESTAMP", System.currentTimeMillis());

            producer.send(textMessage);

            MQMessage mqMessage = new MQMessage();
            mqMessage.setMessageId(textMessage.getJMSMessageID());
            mqMessage.setQueueName(queueName);
            mqMessage.setMessageContent(message);
            mqMessage.setMessageType("TEXT");
            mqMessage.setStatus(MQMessage.MessageStatus.PROCESSED);
            mqMessage.markAsProcessed();

            messageRepository.save(mqMessage);

            session.commit();

            LOGGER.info("Transactional message sent and persisted successfully to queue: " + queueName);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send transactional message", e);
            throw new RuntimeException("Transactional message sending failed", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    @Transactional
    public String receiveMessageTransactional(String queueName, long timeout) {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue queue = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(queue);

            Message message = consumer.receive(timeout);

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String messageContent = textMessage.getText();

                MQMessage mqMessage = new MQMessage();
                mqMessage.setMessageId(textMessage.getJMSMessageID());
                mqMessage.setCorrelationId(textMessage.getJMSCorrelationID());
                mqMessage.setQueueName(queueName);
                mqMessage.setMessageContent(messageContent);
                mqMessage.setMessageType("TEXT");
                mqMessage.setPriority(textMessage.getJMSPriority());
                mqMessage.setExpiry(textMessage.getJMSExpiration());
                mqMessage.markAsProcessed();

                messageRepository.save(mqMessage);

                session.commit();

                LOGGER.info("Transactional message received and persisted from queue: " + queueName);
                return messageContent;

            } else if (message != null) {
                session.rollback();
                LOGGER.warning("Received non-text message, rolling back transaction");
                return null;
            } else {
                session.commit();
                LOGGER.fine("No message received within timeout, committing empty transaction");
                return null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to receive transactional message", e);
            throw new RuntimeException("Transactional message receiving failed", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    @Transactional
    public String sendAndReceiveTransactional(String requestMessage, String requestQueue, String responseQueue) {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            Queue reqQueue = session.createQueue(requestQueue);
            Queue respQueue = session.createQueue(responseQueue);

            MessageProducer producer = session.createProducer(reqQueue);
            MessageConsumer consumer = session.createConsumer(respQueue);

            TextMessage request = session.createTextMessage(requestMessage);
            String correlationId = java.util.UUID.randomUUID().toString();
            request.setJMSCorrelationID(correlationId);
            request.setJMSReplyTo(respQueue);
            request.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            request.setStringProperty("APP_ID", "PayaraIBMMQApp");
            request.setLongProperty("TIMESTAMP", System.currentTimeMillis());

            producer.send(request);

            MQMessage requestMsgEntity = new MQMessage();
            requestMsgEntity.setMessageId(request.getJMSMessageID());
            requestMsgEntity.setCorrelationId(correlationId);
            requestMsgEntity.setQueueName(requestQueue);
            requestMsgEntity.setMessageContent(requestMessage);
            requestMsgEntity.setMessageType("TEXT");
            requestMsgEntity.markAsProcessed();

            messageRepository.save(requestMsgEntity);

            Message response = consumer.receive(30000);

            if (response instanceof TextMessage &&
                correlationId.equals(response.getJMSCorrelationID())) {

                String responseText = ((TextMessage) response).getText();

                MQMessage responseMsgEntity = new MQMessage();
                responseMsgEntity.setMessageId(response.getJMSMessageID());
                responseMsgEntity.setCorrelationId(correlationId);
                responseMsgEntity.setQueueName(responseQueue);
                responseMsgEntity.setMessageContent(responseText);
                responseMsgEntity.setMessageType("TEXT");
                responseMsgEntity.markAsProcessed();

                messageRepository.save(responseMsgEntity);

                session.commit();

                LOGGER.info("Transactional request-response completed for correlation ID: " + correlationId);
                return responseText;

            } else {
                session.rollback();
                LOGGER.warning("No matching response received, rolling back transaction for correlation ID: " + correlationId);
                return null;
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to perform transactional send and receive", e);
            throw new RuntimeException("Transactional send and receive failed", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    @Transactional
    public void processMessageWithCompensation(String queueName, String message) {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            MQMessage mqMessage = new MQMessage();
            mqMessage.setQueueName(queueName);
            mqMessage.setMessageContent(message);
            mqMessage.setMessageType("TEXT");
            mqMessage.markAsProcessing();

            messageRepository.save(mqMessage);

            validateAndProcessMessage(message);

            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);

            TextMessage textMessage = session.createTextMessage("PROCESSED: " + message);
            textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            textMessage.setStringProperty("ORIGINAL_MESSAGE_ID", mqMessage.getMessageId());

            producer.send(textMessage);

            mqMessage.setMessageId(textMessage.getJMSMessageID());
            mqMessage.markAsProcessed();
            messageRepository.save(mqMessage);

            session.commit();

            LOGGER.info("Message processed with compensation successfully");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process message with compensation", e);
            throw new RuntimeException("Message processing with compensation failed", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    private void validateAndProcessMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be null or empty");
        }

        if (message.toLowerCase().contains("error")) {
            throw new RuntimeException("Message contains error keyword");
        }

        if (message.length() > 10000) {
            throw new IllegalArgumentException("Message too large");
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted");
        }
    }
}