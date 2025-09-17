package com.example.ibmmq.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SSLConfig {

    @ConfigProperty(name = "ibm.mq.ssl.enabled", defaultValue = "false")
    private boolean sslEnabled;

    @ConfigProperty(name = "ibm.mq.ssl.cipher.suite", defaultValue = "TLS_RSA_WITH_AES_128_CBC_SHA256")
    private String cipherSuite;

    @ConfigProperty(name = "ibm.mq.ssl.protocol", defaultValue = "TLSv1.2")
    private String sslProtocol;

    @ConfigProperty(name = "ibm.mq.ssl.keystore.path", defaultValue = "")
    private String keystorePath;

    @ConfigProperty(name = "ibm.mq.ssl.keystore.password", defaultValue = "")
    private String keystorePassword;

    @ConfigProperty(name = "ibm.mq.ssl.keystore.type", defaultValue = "JKS")
    private String keystoreType;

    @ConfigProperty(name = "ibm.mq.ssl.truststore.path", defaultValue = "")
    private String truststorePath;

    @ConfigProperty(name = "ibm.mq.ssl.truststore.password", defaultValue = "")
    private String truststorePassword;

    @ConfigProperty(name = "ibm.mq.ssl.truststore.type", defaultValue = "JKS")
    private String truststoreType;

    @ConfigProperty(name = "ibm.mq.ssl.client.auth", defaultValue = "false")
    private boolean clientAuthRequired;

    @ConfigProperty(name = "ibm.mq.ssl.validate.certificate", defaultValue = "true")
    private boolean validateCertificate;

    @ConfigProperty(name = "ibm.mq.ssl.validate.hostname", defaultValue = "true")
    private boolean validateHostname;

    @ConfigProperty(name = "ibm.mq.ssl.fips.required", defaultValue = "false")
    private boolean fipsRequired;

    @ConfigProperty(name = "ibm.mq.ssl.peer.name", defaultValue = "")
    private String sslPeerName;

    @ConfigProperty(name = "ibm.mq.ssl.reset.count", defaultValue = "0")
    private int sslResetCount;

    @ConfigProperty(name = "ibm.mq.ssl.key.repository", defaultValue = "")
    private String sslKeyRepository;

    @ConfigProperty(name = "ibm.mq.ssl.crypto.hardware", defaultValue = "")
    private String sslCryptoHardware;

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public String getCipherSuite() {
        return cipherSuite;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public String getTruststoreType() {
        return truststoreType;
    }

    public boolean isClientAuthRequired() {
        return clientAuthRequired;
    }

    public boolean isValidateCertificate() {
        return validateCertificate;
    }

    public boolean isValidateHostname() {
        return validateHostname;
    }

    public boolean isFipsRequired() {
        return fipsRequired;
    }

    public String getSslPeerName() {
        return sslPeerName;
    }

    public int getSslResetCount() {
        return sslResetCount;
    }

    public String getSslKeyRepository() {
        return sslKeyRepository;
    }

    public String getSslCryptoHardware() {
        return sslCryptoHardware;
    }
}