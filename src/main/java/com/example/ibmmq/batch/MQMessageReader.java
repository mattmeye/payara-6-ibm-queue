package com.example.ibmmq.batch;

import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.entity.MQMessage;
import jakarta.batch.api.chunk.ItemReader;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.jms.*;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
@Named
public class MQMessageReader implements ItemReader {

    private static final Logger LOGGER = Logger.getLogger(MQMessageReader.class.getName());

    @Inject
    private ConnectionFactory connectionFactory;

    @Inject
    private IBMMQConfig config;

    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private String queueName;
    private long timeout = 5000;

    @Override
    public void open(Serializable checkpoint) throws Exception {
        LOGGER.info("Opening MQ Message Reader");

        queueName = config.getRequestQueue();

        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue(queueName);
        consumer = session.createConsumer(queue);

        connection.start();

        LOGGER.info("MQ Message Reader opened successfully for queue: " + queueName);
    }

    @Override
    public void close() throws Exception {
        LOGGER.info("Closing MQ Message Reader");

        try {
            if (consumer != null) {
                consumer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException e) {
            LOGGER.log(Level.WARNING, "Error closing MQ resources", e);
        }

        LOGGER.info("MQ Message Reader closed");
    }

    @Override
    public Object readItem() throws Exception {
        try {
            Message message = consumer.receive(timeout);

            if (message == null) {
                LOGGER.fine("No message received within timeout, ending batch");
                return null;
            }

            LOGGER.info("Message received from queue: " + queueName);

            MQMessage mqMessage = new MQMessage();
            mqMessage.setQueueName(queueName);
            mqMessage.setMessageId(message.getJMSMessageID());
            mqMessage.setCorrelationId(message.getJMSCorrelationID());
            mqMessage.setPriority(message.getJMSPriority());
            mqMessage.setExpiry(message.getJMSExpiration());

            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                mqMessage.setMessageContent(textMessage.getText());
                mqMessage.setMessageType("TEXT");
            } else if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(bytes);
                mqMessage.setMessageContent(new String(bytes));
                mqMessage.setMessageType("BYTES");
            } else {
                mqMessage.setMessageContent(message.toString());
                mqMessage.setMessageType(message.getClass().getSimpleName());
            }

            LOGGER.info("Created MQMessage entity for message ID: " + mqMessage.getMessageId());
            return mqMessage;

        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "Error reading message from MQ", e);
            throw new Exception("Failed to read message from MQ", e);
        }
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }
}