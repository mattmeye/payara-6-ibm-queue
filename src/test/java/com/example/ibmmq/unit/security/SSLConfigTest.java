package com.example.ibmmq.unit.security;

import com.example.ibmmq.security.SSLConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SSLConfig Tests")
class SSLConfigTest {

    private SSLConfig sslConfig;

    @BeforeEach
    void setUp() {
        sslConfig = new SSLConfig();
    }

    @Test
    @DisplayName("Should have default SSL disabled")
    void shouldHaveDefaultSslDisabled() throws Exception {
        // Given - Default configuration
        setField("sslEnabled", false);

        // When & Then
        assertThat(sslConfig.isSslEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should have default cipher suite")
    void shouldHaveDefaultCipherSuite() throws Exception {
        // Given
        setField("cipherSuite", "TLS_RSA_WITH_AES_128_CBC_SHA256");

        // When & Then
        assertThat(sslConfig.getCipherSuite()).isEqualTo("TLS_RSA_WITH_AES_128_CBC_SHA256");
    }

    @Test
    @DisplayName("Should have default SSL protocol")
    void shouldHaveDefaultSslProtocol() throws Exception {
        // Given
        setField("sslProtocol", "TLSv1.2");

        // When & Then
        assertThat(sslConfig.getSslProtocol()).isEqualTo("TLSv1.2");
    }

    @Test
    @DisplayName("Should have empty default keystore path")
    void shouldHaveEmptyDefaultKeystorePath() throws Exception {
        // Given
        setField("keystorePath", "");

        // When & Then
        assertThat(sslConfig.getKeystorePath()).isEmpty();
    }

    @Test
    @DisplayName("Should have empty default keystore password")
    void shouldHaveEmptyDefaultKeystorePassword() throws Exception {
        // Given
        setField("keystorePassword", "");

        // When & Then
        assertThat(sslConfig.getKeystorePassword()).isEmpty();
    }

    @Test
    @DisplayName("Should have JKS as default keystore type")
    void shouldHaveJksAsDefaultKeystoreType() throws Exception {
        // Given
        setField("keystoreType", "JKS");

        // When & Then
        assertThat(sslConfig.getKeystoreType()).isEqualTo("JKS");
    }

    @Test
    @DisplayName("Should have empty default truststore path")
    void shouldHaveEmptyDefaultTruststorePath() throws Exception {
        // Given
        setField("truststorePath", "");

        // When & Then
        assertThat(sslConfig.getTruststorePath()).isEmpty();
    }

    @Test
    @DisplayName("Should have empty default truststore password")
    void shouldHaveEmptyDefaultTruststorePassword() throws Exception {
        // Given
        setField("truststorePassword", "");

        // When & Then
        assertThat(sslConfig.getTruststorePassword()).isEmpty();
    }

    @Test
    @DisplayName("Should have JKS as default truststore type")
    void shouldHaveJksAsDefaultTruststoreType() throws Exception {
        // Given
        setField("truststoreType", "JKS");

        // When & Then
        assertThat(sslConfig.getTruststoreType()).isEqualTo("JKS");
    }

    @Test
    @DisplayName("Should have client auth disabled by default")
    void shouldHaveClientAuthDisabledByDefault() throws Exception {
        // Given
        setField("clientAuthRequired", false);

        // When & Then
        assertThat(sslConfig.isClientAuthRequired()).isFalse();
    }

    @Test
    @DisplayName("Should have certificate validation enabled by default")
    void shouldHaveCertificateValidationEnabledByDefault() throws Exception {
        // Given
        setField("validateCertificate", true);

        // When & Then
        assertThat(sslConfig.isValidateCertificate()).isTrue();
    }

    @Test
    @DisplayName("Should have hostname validation enabled by default")
    void shouldHaveHostnameValidationEnabledByDefault() throws Exception {
        // Given
        setField("validateHostname", true);

        // When & Then
        assertThat(sslConfig.isValidateHostname()).isTrue();
    }

    @Test
    @DisplayName("Should have FIPS disabled by default")
    void shouldHaveFipsDisabledByDefault() throws Exception {
        // Given
        setField("fipsRequired", false);

        // When & Then
        assertThat(sslConfig.isFipsRequired()).isFalse();
    }

    @Test
    @DisplayName("Should have empty default SSL peer name")
    void shouldHaveEmptyDefaultSslPeerName() throws Exception {
        // Given
        setField("sslPeerName", "");

        // When & Then
        assertThat(sslConfig.getSslPeerName()).isEmpty();
    }

    @Test
    @DisplayName("Should have zero as default SSL reset count")
    void shouldHaveZeroAsDefaultSslResetCount() throws Exception {
        // Given
        setField("sslResetCount", 0);

        // When & Then
        assertThat(sslConfig.getSslResetCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should have empty default SSL key repository")
    void shouldHaveEmptyDefaultSslKeyRepository() throws Exception {
        // Given
        setField("sslKeyRepository", "");

        // When & Then
        assertThat(sslConfig.getSslKeyRepository()).isEmpty();
    }

    @Test
    @DisplayName("Should have empty default SSL crypto hardware")
    void shouldHaveEmptyDefaultSslCryptoHardware() throws Exception {
        // Given
        setField("sslCryptoHardware", "");

        // When & Then
        assertThat(sslConfig.getSslCryptoHardware()).isEmpty();
    }

    @Test
    @DisplayName("Should handle SSL enabled configuration")
    void shouldHandleSslEnabledConfiguration() throws Exception {
        // Given
        setField("sslEnabled", true);

        // When & Then
        assertThat(sslConfig.isSslEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should handle custom cipher suite")
    void shouldHandleCustomCipherSuite() throws Exception {
        // Given
        String customCipherSuite = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384";
        setField("cipherSuite", customCipherSuite);

        // When & Then
        assertThat(sslConfig.getCipherSuite()).isEqualTo(customCipherSuite);
    }

    @Test
    @DisplayName("Should handle custom SSL protocol")
    void shouldHandleCustomSslProtocol() throws Exception {
        // Given
        String customProtocol = "TLSv1.3";
        setField("sslProtocol", customProtocol);

        // When & Then
        assertThat(sslConfig.getSslProtocol()).isEqualTo(customProtocol);
    }

    @Test
    @DisplayName("Should handle custom keystore configuration")
    void shouldHandleCustomKeystoreConfiguration() throws Exception {
        // Given
        String keystorePath = "/opt/ssl/keystore.jks";
        String keystorePassword = "keystore123";
        String keystoreType = "PKCS12";

        setField("keystorePath", keystorePath);
        setField("keystorePassword", keystorePassword);
        setField("keystoreType", keystoreType);

        // When & Then
        assertThat(sslConfig.getKeystorePath()).isEqualTo(keystorePath);
        assertThat(sslConfig.getKeystorePassword()).isEqualTo(keystorePassword);
        assertThat(sslConfig.getKeystoreType()).isEqualTo(keystoreType);
    }

    @Test
    @DisplayName("Should handle custom truststore configuration")
    void shouldHandleCustomTruststoreConfiguration() throws Exception {
        // Given
        String truststorePath = "/opt/ssl/truststore.jks";
        String truststorePassword = "truststore456";
        String truststoreType = "PKCS12";

        setField("truststorePath", truststorePath);
        setField("truststorePassword", truststorePassword);
        setField("truststoreType", truststoreType);

        // When & Then
        assertThat(sslConfig.getTruststorePath()).isEqualTo(truststorePath);
        assertThat(sslConfig.getTruststorePassword()).isEqualTo(truststorePassword);
        assertThat(sslConfig.getTruststoreType()).isEqualTo(truststoreType);
    }

    @Test
    @DisplayName("Should handle client authentication required")
    void shouldHandleClientAuthenticationRequired() throws Exception {
        // Given
        setField("clientAuthRequired", true);

        // When & Then
        assertThat(sslConfig.isClientAuthRequired()).isTrue();
    }

    @Test
    @DisplayName("Should handle certificate validation disabled")
    void shouldHandleCertificateValidationDisabled() throws Exception {
        // Given
        setField("validateCertificate", false);

        // When & Then
        assertThat(sslConfig.isValidateCertificate()).isFalse();
    }

    @Test
    @DisplayName("Should handle hostname validation disabled")
    void shouldHandleHostnameValidationDisabled() throws Exception {
        // Given
        setField("validateHostname", false);

        // When & Then
        assertThat(sslConfig.isValidateHostname()).isFalse();
    }

    @Test
    @DisplayName("Should handle FIPS required")
    void shouldHandleFipsRequired() throws Exception {
        // Given
        setField("fipsRequired", true);

        // When & Then
        assertThat(sslConfig.isFipsRequired()).isTrue();
    }

    @Test
    @DisplayName("Should handle custom SSL peer name")
    void shouldHandleCustomSslPeerName() throws Exception {
        // Given
        String peerName = "CN=mqserver.example.com";
        setField("sslPeerName", peerName);

        // When & Then
        assertThat(sslConfig.getSslPeerName()).isEqualTo(peerName);
    }

    @Test
    @DisplayName("Should handle custom SSL reset count")
    void shouldHandleCustomSslResetCount() throws Exception {
        // Given
        int resetCount = 100;
        setField("sslResetCount", resetCount);

        // When & Then
        assertThat(sslConfig.getSslResetCount()).isEqualTo(resetCount);
    }

    @Test
    @DisplayName("Should handle custom SSL key repository")
    void shouldHandleCustomSslKeyRepository() throws Exception {
        // Given
        String keyRepository = "/opt/ssl/keys";
        setField("sslKeyRepository", keyRepository);

        // When & Then
        assertThat(sslConfig.getSslKeyRepository()).isEqualTo(keyRepository);
    }

    @Test
    @DisplayName("Should handle custom SSL crypto hardware")
    void shouldHandleCustomSslCryptoHardware() throws Exception {
        // Given
        String cryptoHardware = "PKCS11:library=/usr/lib/pkcs11.so";
        setField("sslCryptoHardware", cryptoHardware);

        // When & Then
        assertThat(sslConfig.getSslCryptoHardware()).isEqualTo(cryptoHardware);
    }

    @Test
    @DisplayName("Should handle all boolean properties in combination")
    void shouldHandleAllBooleanPropertiesInCombination() throws Exception {
        // Given - Enable all boolean properties
        setField("sslEnabled", true);
        setField("clientAuthRequired", true);
        setField("validateCertificate", true);
        setField("validateHostname", true);
        setField("fipsRequired", true);

        // When & Then
        assertThat(sslConfig.isSslEnabled()).isTrue();
        assertThat(sslConfig.isClientAuthRequired()).isTrue();
        assertThat(sslConfig.isValidateCertificate()).isTrue();
        assertThat(sslConfig.isValidateHostname()).isTrue();
        assertThat(sslConfig.isFipsRequired()).isTrue();
    }

    @Test
    @DisplayName("Should handle all string properties with whitespace")
    void shouldHandleAllStringPropertiesWithWhitespace() throws Exception {
        // Given - Set properties with leading/trailing whitespace
        setField("cipherSuite", "  TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384  ");
        setField("sslProtocol", "  TLSv1.3  ");
        setField("keystorePath", "  /opt/ssl/keystore.jks  ");
        setField("sslPeerName", "  CN=mqserver.example.com  ");

        // When & Then - Values should be returned as-is (with whitespace)
        assertThat(sslConfig.getCipherSuite()).isEqualTo("  TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384  ");
        assertThat(sslConfig.getSslProtocol()).isEqualTo("  TLSv1.3  ");
        assertThat(sslConfig.getKeystorePath()).isEqualTo("  /opt/ssl/keystore.jks  ");
        assertThat(sslConfig.getSslPeerName()).isEqualTo("  CN=mqserver.example.com  ");
    }

    @Test
    @DisplayName("Should handle negative SSL reset count")
    void shouldHandleNegativeSslResetCount() throws Exception {
        // Given
        int negativeResetCount = -1;
        setField("sslResetCount", negativeResetCount);

        // When & Then
        assertThat(sslConfig.getSslResetCount()).isEqualTo(negativeResetCount);
    }

    @Test
    @DisplayName("Should handle maximum SSL reset count")
    void shouldHandleMaximumSslResetCount() throws Exception {
        // Given
        int maxResetCount = Integer.MAX_VALUE;
        setField("sslResetCount", maxResetCount);

        // When & Then
        assertThat(sslConfig.getSslResetCount()).isEqualTo(maxResetCount);
    }

    @Test
    @DisplayName("Should handle complete SSL configuration")
    void shouldHandleCompleteSslConfiguration() throws Exception {
        // Given - Full SSL configuration
        setField("sslEnabled", true);
        setField("cipherSuite", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        setField("sslProtocol", "TLSv1.3");
        setField("keystorePath", "/opt/ssl/client-keystore.p12");
        setField("keystorePassword", "clientpass123");
        setField("keystoreType", "PKCS12");
        setField("truststorePath", "/opt/ssl/client-truststore.p12");
        setField("truststorePassword", "trustpass456");
        setField("truststoreType", "PKCS12");
        setField("clientAuthRequired", true);
        setField("validateCertificate", true);
        setField("validateHostname", true);
        setField("fipsRequired", false);
        setField("sslPeerName", "CN=mqserver.example.com,O=Example Corp");
        setField("sslResetCount", 1000);
        setField("sslKeyRepository", "/opt/mq/ssl/keys");
        setField("sslCryptoHardware", "PKCS11:/usr/lib/softhsm/libsofthsm2.so");

        // When & Then - Verify all properties
        assertThat(sslConfig.isSslEnabled()).isTrue();
        assertThat(sslConfig.getCipherSuite()).isEqualTo("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        assertThat(sslConfig.getSslProtocol()).isEqualTo("TLSv1.3");
        assertThat(sslConfig.getKeystorePath()).isEqualTo("/opt/ssl/client-keystore.p12");
        assertThat(sslConfig.getKeystorePassword()).isEqualTo("clientpass123");
        assertThat(sslConfig.getKeystoreType()).isEqualTo("PKCS12");
        assertThat(sslConfig.getTruststorePath()).isEqualTo("/opt/ssl/client-truststore.p12");
        assertThat(sslConfig.getTruststorePassword()).isEqualTo("trustpass456");
        assertThat(sslConfig.getTruststoreType()).isEqualTo("PKCS12");
        assertThat(sslConfig.isClientAuthRequired()).isTrue();
        assertThat(sslConfig.isValidateCertificate()).isTrue();
        assertThat(sslConfig.isValidateHostname()).isTrue();
        assertThat(sslConfig.isFipsRequired()).isFalse();
        assertThat(sslConfig.getSslPeerName()).isEqualTo("CN=mqserver.example.com,O=Example Corp");
        assertThat(sslConfig.getSslResetCount()).isEqualTo(1000);
        assertThat(sslConfig.getSslKeyRepository()).isEqualTo("/opt/mq/ssl/keys");
        assertThat(sslConfig.getSslCryptoHardware()).isEqualTo("PKCS11:/usr/lib/softhsm/libsofthsm2.so");
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = SSLConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(sslConfig, value);
    }
}