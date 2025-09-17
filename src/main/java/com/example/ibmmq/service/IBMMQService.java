package com.example.ibmmq.service;

import com.example.ibmmq.config.IBMMQConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class IBMMQService {

    private static final Logger LOGGER = Logger.getLogger(IBMMQService.class.getName());

    @Inject
    private ConnectionFactory connectionFactory;

    @Inject
    private IBMMQConfig config;

    public void sendMessage(String message) {
        sendMessage(config.getRequestQueue(), message);
    }

    public void sendMessage(String queueName, String message) {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);

            TextMessage textMessage = session.createTextMessage(message);
            producer.send(textMessage);

            LOGGER.info("Message sent successfully to queue: " + queueName);

        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "Failed to send message to queue: " + queueName, e);
            throw new RuntimeException("Failed to send message", e);
        }
    }

    public String receiveMessage() {
        return receiveMessage(config.getResponseQueue());
    }

    public String receiveMessage(String queueName) {
        return receiveMessage(queueName, 5000);
    }

    public String receiveMessage(String queueName, long timeout) {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

            connection.start();

            Queue queue = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(queue);

            Message message = consumer.receive(timeout);

            if (message instanceof TextMessage) {
                String text = ((TextMessage) message).getText();
                LOGGER.info("Message received successfully from queue: " + queueName);
                return text;
            } else if (message != null) {
                LOGGER.warning("Received non-text message from queue: " + queueName);
                return message.toString();
            } else {
                LOGGER.info("No message received from queue: " + queueName + " within timeout: " + timeout + "ms");
                return null;
            }

        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "Failed to receive message from queue: " + queueName, e);
            throw new RuntimeException("Failed to receive message", e);
        }
    }

    public String sendAndReceive(String requestMessage) {
        return sendAndReceive(requestMessage, config.getRequestQueue(), config.getResponseQueue());
    }

    public String sendAndReceive(String requestMessage, String requestQueue, String responseQueue) {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {

            connection.start();

            Queue reqQueue = session.createQueue(requestQueue);
            Queue respQueue = session.createQueue(responseQueue);

            MessageProducer producer = session.createProducer(reqQueue);
            MessageConsumer consumer = session.createConsumer(respQueue);

            TextMessage request = session.createTextMessage(requestMessage);
            String correlationId = java.util.UUID.randomUUID().toString();
            request.setJMSCorrelationID(correlationId);
            request.setJMSReplyTo(respQueue);

            producer.send(request);
            LOGGER.info("Request sent with correlation ID: " + correlationId);

            Message response = consumer.receive(10000);

            if (response instanceof TextMessage &&
                correlationId.equals(response.getJMSCorrelationID())) {
                String responseText = ((TextMessage) response).getText();
                LOGGER.info("Response received for correlation ID: " + correlationId);
                return responseText;
            } else {
                LOGGER.warning("No matching response received for correlation ID: " + correlationId);
                return null;
            }

        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "Failed to perform send and receive operation", e);
            throw new RuntimeException("Failed to perform send and receive operation", e);
        }
    }
}