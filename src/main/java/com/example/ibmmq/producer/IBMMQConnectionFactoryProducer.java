package com.example.ibmmq.producer;

import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.adapter.JakartaJMSAdapter;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class IBMMQConnectionFactoryProducer {

    private static final Logger LOGGER = Logger.getLogger(IBMMQConnectionFactoryProducer.class.getName());

    @Inject
    private IBMMQConfig config;

    @Produces
    @ApplicationScoped
    public ConnectionFactory createJakartaConnectionFactory() {
        try {
            // Create the IBM MQ ConnectionFactory (javax.jms)
            MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();

            mqConnectionFactory.setHostName(config.getHostname());
            mqConnectionFactory.setPort(config.getPort());
            mqConnectionFactory.setChannel(config.getChannel());
            mqConnectionFactory.setQueueManager(config.getQueueManager());
            mqConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                mqConnectionFactory.setStringProperty(WMQConstants.USERID, config.getUsername());
                mqConnectionFactory.setStringProperty(WMQConstants.PASSWORD, config.getPassword());
            }

            mqConnectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            mqConnectionFactory.setStringProperty(WMQConstants.WMQ_CONNECTION_NAME_LIST,
                config.getHostname() + "(" + config.getPort() + ")");

            // Wrap the IBM MQ ConnectionFactory to provide Jakarta JMS interface
            ConnectionFactory jakartaConnectionFactory = new JakartaJMSAdapter.ConnectionFactoryWrapper(mqConnectionFactory);

            LOGGER.info("Jakarta JMS ConnectionFactory created successfully (wrapping IBM MQ Client)");
            return jakartaConnectionFactory;

        } catch (javax.jms.JMSException e) {
            LOGGER.log(Level.SEVERE, "Failed to create Jakarta JMS ConnectionFactory", e);
            throw new RuntimeException("Failed to create Jakarta JMS ConnectionFactory", e);
        }
    }
}