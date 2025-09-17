package com.example.ibmmq.unit.security;

import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.security.SSLConfig;
import com.example.ibmmq.security.SSLConnectionFactoryProducer;
import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SSLConnectionFactoryProducer Tests")
class SSLConnectionFactoryProducerTest {

    @Mock
    private IBMMQConfig mqConfig;

    @Mock
    private SSLConfig sslConfig;

    @InjectMocks
    private SSLConnectionFactoryProducer sslConnectionFactoryProducer;

    @BeforeEach
    void setUp() {
        // Setup MQ configuration
        when(mqConfig.getHostname()).thenReturn("localhost");
        when(mqConfig.getPort()).thenReturn(1414);
        when(mqConfig.getChannel()).thenReturn("DEV.APP.SVRCONN");
        when(mqConfig.getQueueManager()).thenReturn("QM1");
        when(mqConfig.getUsername()).thenReturn("app");
        when(mqConfig.getPassword()).thenReturn("password");

        // Setup SSL configuration with basic defaults
        when(sslConfig.isSslEnabled()).thenReturn(false);
        when(sslConfig.getCipherSuite()).thenReturn("TLS_RSA_WITH_AES_128_CBC_SHA256");
        when(sslConfig.getSslProtocol()).thenReturn("TLSv1.2");
        when(sslConfig.getKeystorePath()).thenReturn("");
        when(sslConfig.getKeystorePassword()).thenReturn("");
        when(sslConfig.getKeystoreType()).thenReturn("JKS");
        when(sslConfig.getTruststorePath()).thenReturn("");
        when(sslConfig.getTruststorePassword()).thenReturn("");
        when(sslConfig.getTruststoreType()).thenReturn("JKS");
        when(sslConfig.isClientAuthRequired()).thenReturn(false);
        when(sslConfig.isValidateCertificate()).thenReturn(true);
        when(sslConfig.isValidateHostname()).thenReturn(true);
        when(sslConfig.isFipsRequired()).thenReturn(false);
        when(sslConfig.getSslPeerName()).thenReturn("");
        when(sslConfig.getSslResetCount()).thenReturn(0);
        when(sslConfig.getSslKeyRepository()).thenReturn("");
        when(sslConfig.getSslCryptoHardware()).thenReturn("");
    }

    @Test
    @DisplayName("Should create ConnectionFactory without SSL")
    void shouldCreateConnectionFactoryWithoutSsl() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(false);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        assertThat(connectionFactory).isInstanceOf(ConnectionFactory.class);
        verify(mqConfig, times(2)).getHostname();
        verify(mqConfig, times(2)).getPort();
        verify(mqConfig).getChannel();
        verify(mqConfig).getQueueManager();
        verify(sslConfig).isSslEnabled();
    }

    @Test
    @DisplayName("Should create ConnectionFactory with basic SSL")
    void shouldCreateConnectionFactoryWithBasicSsl() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig).isSslEnabled();
        verify(sslConfig, times(2)).getCipherSuite();
    }

    @Test
    @DisplayName("Should create ConnectionFactory with SSL peer name")
    void shouldCreateConnectionFactoryWithSslPeerName() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslPeerName()).thenReturn("CN=mqserver.example.com");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(2)).getSslPeerName();
    }

    @Test
    @DisplayName("Should create ConnectionFactory with SSL reset count")
    void shouldCreateConnectionFactoryWithSslResetCount() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslResetCount()).thenReturn(100);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(2)).getSslResetCount();
    }

    @Test
    @DisplayName("Should create ConnectionFactory with SSL key repository")
    void shouldCreateConnectionFactoryWithSslKeyRepository() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslKeyRepository()).thenReturn("/opt/mq/ssl/keys");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(2)).getSslKeyRepository();
    }

    @Test
    @DisplayName("Should create ConnectionFactory with SSL crypto hardware")
    void shouldCreateConnectionFactoryWithSslCryptoHardware() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslCryptoHardware()).thenReturn("PKCS11:/usr/lib/softhsm/libsofthsm2.so");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(2)).getSslCryptoHardware();
    }

    @Test
    @DisplayName("Should create ConnectionFactory with FIPS required")
    void shouldCreateConnectionFactoryWithFipsRequired() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.isFipsRequired()).thenReturn(true);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig).isFipsRequired();
    }

    @Test
    @DisplayName("Should create ConnectionFactory with certificate validation")
    void shouldCreateConnectionFactoryWithCertificateValidation() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.isValidateCertificate()).thenReturn(true);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig).isValidateCertificate();
    }

    @Test
    @DisplayName("Should handle empty username gracefully")
    void shouldHandleEmptyUsernameGracefully() {
        // Given
        when(mqConfig.getUsername()).thenReturn("");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(mqConfig, atLeast(2)).getUsername();
    }

    @Test
    @DisplayName("Should handle null username gracefully")
    void shouldHandleNullUsernameGracefully() {
        // Given
        when(mqConfig.getUsername()).thenReturn(null);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(mqConfig, times(1)).getUsername();
    }

    @Test
    @DisplayName("Should handle MQ configuration exception")
    void shouldHandleMqConfigurationException() {
        // Given
        when(mqConfig.getHostname()).thenThrow(new RuntimeException("Configuration error"));

        // When & Then
        assertThatThrownBy(() -> sslConnectionFactoryProducer.createSSLConnectionFactory())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to create SSL Jakarta JMS ConnectionFactory");
    }

    @Test
    @DisplayName("Should handle SSL configuration exception")
    void shouldHandleSslConfigurationException() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getCipherSuite()).thenThrow(new RuntimeException("SSL configuration error"));

        // When & Then
        assertThatThrownBy(() -> sslConnectionFactoryProducer.createSSLConnectionFactory())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to create SSL Jakarta JMS ConnectionFactory");
    }

    @Test
    @DisplayName("Should create ConnectionFactory with complete SSL configuration")
    void shouldCreateConnectionFactoryWithCompleteSslConfiguration() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getCipherSuite()).thenReturn("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        when(sslConfig.getSslPeerName()).thenReturn("CN=mqserver.example.com");
        when(sslConfig.getSslResetCount()).thenReturn(1000);
        when(sslConfig.getSslKeyRepository()).thenReturn("/opt/mq/ssl/keys");
        when(sslConfig.getSslCryptoHardware()).thenReturn("PKCS11:/usr/lib/softhsm/libsofthsm2.so");
        when(sslConfig.isFipsRequired()).thenReturn(true);
        when(sslConfig.isValidateCertificate()).thenReturn(true);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(2)).getCipherSuite();
        verify(sslConfig, times(2)).getSslPeerName();
        verify(sslConfig, times(2)).getSslResetCount();
        verify(sslConfig, times(2)).getSslKeyRepository();
        verify(sslConfig, times(2)).getSslCryptoHardware();
        verify(sslConfig).isFipsRequired();
        verify(sslConfig).isValidateCertificate();
    }

    @Test
    @DisplayName("Should set authentication properties when username is provided")
    void shouldSetAuthenticationPropertiesWhenUsernameIsProvided() {
        // Given
        when(mqConfig.getUsername()).thenReturn("mquser");
        when(mqConfig.getPassword()).thenReturn("mqpass");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(mqConfig, atLeast(2)).getUsername();
        verify(mqConfig).getPassword();
    }

    @Test
    @DisplayName("Should handle empty SSL peer name")
    void shouldHandleEmptySslPeerName() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslPeerName()).thenReturn("");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(1)).getSslPeerName();
    }

    @Test
    @DisplayName("Should handle zero SSL reset count")
    void shouldHandleZeroSslResetCount() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslResetCount()).thenReturn(0);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(1)).getSslResetCount();
    }

    @Test
    @DisplayName("Should handle empty SSL key repository")
    void shouldHandleEmptySslKeyRepository() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslKeyRepository()).thenReturn("");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(1)).getSslKeyRepository();
    }

    @Test
    @DisplayName("Should handle empty SSL crypto hardware")
    void shouldHandleEmptySslCryptoHardware() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslCryptoHardware()).thenReturn("");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig, times(1)).getSslCryptoHardware();
    }

    @Test
    @DisplayName("Should handle FIPS disabled")
    void shouldHandleFipsDisabled() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.isFipsRequired()).thenReturn(false);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig).isFipsRequired();
    }

    @Test
    @DisplayName("Should handle certificate validation disabled")
    void shouldHandleCertificateValidationDisabled() {
        // Given
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.isValidateCertificate()).thenReturn(false);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(sslConfig).isValidateCertificate();
    }

    @Test
    @DisplayName("Should create Jakarta JMS wrapper")
    void shouldCreateJakartaJmsWrapper() {
        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        assertThat(connectionFactory.getClass().getName()).contains("JakartaJMSAdapter");
    }

    @Test
    @DisplayName("Should handle special characters in configuration")
    void shouldHandleSpecialCharactersInConfiguration() {
        // Given
        when(mqConfig.getUsername()).thenReturn("user@domain.com");
        when(mqConfig.getPassword()).thenReturn("p@$$w0rd!#");
        when(sslConfig.isSslEnabled()).thenReturn(true);
        when(sslConfig.getSslPeerName()).thenReturn("CN=MQ Server (Test), O=Example Corp, C=US");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(mqConfig, atLeast(2)).getUsername();
        verify(mqConfig).getPassword();
        verify(sslConfig, times(2)).getSslPeerName();
    }

    @Test
    @DisplayName("Should handle IPv6 hostname")
    void shouldHandleIpv6Hostname() {
        // Given
        when(mqConfig.getHostname()).thenReturn("2001:db8::1");

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(mqConfig, times(2)).getHostname();
    }

    @Test
    @DisplayName("Should handle non-standard port")
    void shouldHandleNonStandardPort() {
        // Given
        when(mqConfig.getPort()).thenReturn(9999);

        // When
        ConnectionFactory connectionFactory = sslConnectionFactoryProducer.createSSLConnectionFactory();

        // Then
        assertThat(connectionFactory).isNotNull();
        verify(mqConfig, times(2)).getPort();
    }

    @Test
    @DisplayName("Should handle long queue manager name")
    void shouldHandleLongQueueManagerName() {
        // Given
        String longQueueManagerName = "VERY_LONG_QUEUE_MANAGER_NAME_FOR_TESTING_PURPOSES_123456789";
        when(mqConfig.getQueueManager()).thenReturn(longQueueManagerName);

        // When & Then - should throw RuntimeException for invalid queue manager name
        assertThatThrownBy(() -> sslConnectionFactoryProducer.createSSLConnectionFactory())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to create SSL Jakarta JMS ConnectionFactory");

        verify(mqConfig).getQueueManager();
    }
}