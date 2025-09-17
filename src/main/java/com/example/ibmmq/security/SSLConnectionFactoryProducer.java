package com.example.ibmmq.security;

import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.adapter.JakartaJMSAdapter;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

@Alternative
@ApplicationScoped
public class SSLConnectionFactoryProducer {

    private static final Logger LOGGER = Logger.getLogger(SSLConnectionFactoryProducer.class.getName());

    @Inject
    private IBMMQConfig mqConfig;

    @Inject
    private SSLConfig sslConfig;

    @Produces
    @ApplicationScoped
    public ConnectionFactory createSSLConnectionFactory() {
        try {
            MQConnectionFactory connectionFactory = new MQConnectionFactory();

            connectionFactory.setHostName(mqConfig.getHostname());
            connectionFactory.setPort(mqConfig.getPort());
            connectionFactory.setChannel(mqConfig.getChannel());
            connectionFactory.setQueueManager(mqConfig.getQueueManager());
            connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

            if (mqConfig.getUsername() != null && !mqConfig.getUsername().isEmpty()) {
                connectionFactory.setStringProperty(WMQConstants.USERID, mqConfig.getUsername());
                connectionFactory.setStringProperty(WMQConstants.PASSWORD, mqConfig.getPassword());
            }

            connectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            connectionFactory.setStringProperty(WMQConstants.WMQ_CONNECTION_NAME_LIST,
                mqConfig.getHostname() + "(" + mqConfig.getPort() + ")");

            if (sslConfig.isSslEnabled()) {
                configureSsl(connectionFactory);
            }

            // Wrap the IBM MQ ConnectionFactory to provide Jakarta JMS interface
            ConnectionFactory jakartaConnectionFactory = new JakartaJMSAdapter.ConnectionFactoryWrapper(connectionFactory);

            LOGGER.info("SSL-enabled Jakarta JMS ConnectionFactory created successfully (wrapping IBM MQ Client)");
            return jakartaConnectionFactory;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create SSL Jakarta JMS ConnectionFactory", e);
            throw new RuntimeException("Failed to create SSL Jakarta JMS ConnectionFactory", e);
        }
    }

    private void configureSsl(MQConnectionFactory connectionFactory) throws Exception {
        LOGGER.info("Configuring SSL for IBM MQ connection");

        connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, sslConfig.getCipherSuite());

        if (!sslConfig.getSslPeerName().isEmpty()) {
            connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_PEER_NAME, sslConfig.getSslPeerName());
        }

        // SSL Reset count property
        if (sslConfig.getSslResetCount() > 0) {
            connectionFactory.setIntProperty("XMSC_WMQ_SSL_RESET_COUNT", sslConfig.getSslResetCount());
        }

        if (!sslConfig.getSslKeyRepository().isEmpty()) {
            connectionFactory.setStringProperty(WMQConstants.WMQ_SSL_KEY_REPOSITORY, sslConfig.getSslKeyRepository());
        }

        if (!sslConfig.getSslCryptoHardware().isEmpty()) {
            connectionFactory.setStringProperty("XMSC_WMQ_SSL_CRYPTO_HARDWARE", sslConfig.getSslCryptoHardware());
        }

        connectionFactory.setBooleanProperty(WMQConstants.WMQ_SSL_FIPS_REQUIRED, sslConfig.isFipsRequired());

        if (sslConfig.isValidateCertificate()) {
            connectionFactory.setBooleanProperty("XMSC_WMQ_SSL_CERT_STORES", true);
        }

        if (!sslConfig.getKeystorePath().isEmpty() && !sslConfig.getTruststorePath().isEmpty()) {
            configureJavaSSL(connectionFactory);
        }

        LOGGER.info("SSL configuration completed - Cipher Suite: " + sslConfig.getCipherSuite() +
                   ", Protocol: " + sslConfig.getSslProtocol());
    }

    private void configureJavaSSL(MQConnectionFactory connectionFactory) throws Exception {
        LOGGER.info("Configuring Java SSL context for IBM MQ");

        SSLContext sslContext = createSSLContext();
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        connectionFactory.setObjectProperty(WMQConstants.WMQ_SSL_SOCKET_FACTORY, socketFactory);

        System.setProperty("javax.net.ssl.keyStore", sslConfig.getKeystorePath());
        System.setProperty("javax.net.ssl.keyStorePassword", sslConfig.getKeystorePassword());
        System.setProperty("javax.net.ssl.keyStoreType", sslConfig.getKeystoreType());

        System.setProperty("javax.net.ssl.trustStore", sslConfig.getTruststorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", sslConfig.getTruststorePassword());
        System.setProperty("javax.net.ssl.trustStoreType", sslConfig.getTruststoreType());

        if (!sslConfig.isValidateHostname()) {
            System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
        }

        LOGGER.info("Java SSL context configured successfully");
    }

    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance(sslConfig.getSslProtocol());

        KeyStore keyStore = null;
        if (!sslConfig.getKeystorePath().isEmpty()) {
            keyStore = loadKeyStore(sslConfig.getKeystorePath(),
                                  sslConfig.getKeystorePassword(),
                                  sslConfig.getKeystoreType());
        }

        KeyStore trustStore = null;
        if (!sslConfig.getTruststorePath().isEmpty()) {
            trustStore = loadKeyStore(sslConfig.getTruststorePath(),
                                    sslConfig.getTruststorePassword(),
                                    sslConfig.getTruststoreType());
        }

        javax.net.ssl.KeyManagerFactory keyManagerFactory = null;
        if (keyStore != null) {
            keyManagerFactory = javax.net.ssl.KeyManagerFactory.getInstance(
                javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, sslConfig.getKeystorePassword().toCharArray());
        }

        javax.net.ssl.TrustManagerFactory trustManagerFactory = null;
        if (trustStore != null) {
            trustManagerFactory = javax.net.ssl.TrustManagerFactory.getInstance(
                javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
        }

        sslContext.init(
            keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
            trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null,
            new java.security.SecureRandom()
        );

        return sslContext;
    }

    private KeyStore loadKeyStore(String path, String password, String type) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(type);
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password.toCharArray());
        }
        LOGGER.fine("Loaded keystore: " + path + " (type: " + type + ")");
        return keyStore;
    }
}