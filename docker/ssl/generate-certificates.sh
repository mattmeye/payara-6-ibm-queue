#!/bin/bash

# Script to generate SSL certificates for IBM MQ development environment
# This creates self-signed certificates for testing purposes only

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERTS_DIR="$SCRIPT_DIR/certs"
VALIDITY_DAYS=365

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}IBM MQ SSL Certificate Generation Script${NC}"
echo "======================================="

# Create certificates directory
mkdir -p "$CERTS_DIR"
cd "$CERTS_DIR"

# Generate CA private key
echo -e "${YELLOW}Generating CA private key...${NC}"
openssl genrsa -out ca-key.pem 4096

# Generate CA certificate
echo -e "${YELLOW}Generating CA certificate...${NC}"
openssl req -new -x509 -days $VALIDITY_DAYS -key ca-key.pem -sha256 -out ca.pem -subj \
    "/C=DE/ST=Germany/L=Berlin/O=PayaraIBMMQ/OU=Development/CN=PayaraIBMMQ-CA"

# Generate server private key
echo -e "${YELLOW}Generating server private key...${NC}"
openssl genrsa -out server-key.pem 4096

# Generate server certificate signing request
echo -e "${YELLOW}Generating server certificate signing request...${NC}"
openssl req -subj "/C=DE/ST=Germany/L=Berlin/O=PayaraIBMMQ/OU=Development/CN=ibmmq" \
    -sha256 -new -key server-key.pem -out server.csr

# Create extensions file for server certificate
cat > server-extfile.cnf <<EOF
subjectAltName = DNS:ibmmq,DNS:localhost,IP:127.0.0.1
extendedKeyUsage = serverAuth
EOF

# Generate server certificate
echo -e "${YELLOW}Generating server certificate...${NC}"
openssl x509 -req -days $VALIDITY_DAYS -sha256 -in server.csr -CA ca.pem -CAkey ca-key.pem \
    -out server-cert.pem -extfile server-extfile.cnf -CAcreateserial

# Generate client private key
echo -e "${YELLOW}Generating client private key...${NC}"
openssl genrsa -out client-key.pem 4096

# Generate client certificate signing request
echo -e "${YELLOW}Generating client certificate signing request...${NC}"
openssl req -subj "/C=DE/ST=Germany/L=Berlin/O=PayaraIBMMQ/OU=Development/CN=payara-client" \
    -new -key client-key.pem -out client.csr

# Create extensions file for client certificate
cat > client-extfile.cnf <<EOF
extendedKeyUsage = clientAuth
EOF

# Generate client certificate
echo -e "${YELLOW}Generating client certificate...${NC}"
openssl x509 -req -days $VALIDITY_DAYS -sha256 -in client.csr -CA ca.pem -CAkey ca-key.pem \
    -out client-cert.pem -extfile client-extfile.cnf -CAcreateserial

# Create Java keystores
echo -e "${YELLOW}Creating Java keystores...${NC}"

# Server keystore
openssl pkcs12 -export -in server-cert.pem -inkey server-key.pem -out server-keystore.p12 \
    -name server -CAfile ca.pem -caname ca -password pass:changeit

keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore server-keystore.jks \
    -srckeystore server-keystore.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias server

# Client keystore
openssl pkcs12 -export -in client-cert.pem -inkey client-key.pem -out client-keystore.p12 \
    -name client -CAfile ca.pem -caname ca -password pass:changeit

keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore client-keystore.jks \
    -srckeystore client-keystore.p12 -srcstoretype PKCS12 -srcstorepass changeit -alias client

# Truststore (contains CA certificate)
keytool -import -trustcacerts -noprompt -alias ca -file ca.pem \
    -keystore truststore.jks -storepass changeit

# Set appropriate permissions
chmod 600 *-key.pem *.jks *.p12
chmod 644 *.pem *.csr *.cnf

# Clean up temporary files
rm -f *.csr *.cnf *.srl *.p12

echo ""
echo -e "${GREEN}SSL certificates generated successfully!${NC}"
echo ""
echo "Generated files:"
echo "├── Certificate Authority:"
echo "│   ├── ca.pem (CA certificate)"
echo "│   └── ca-key.pem (CA private key)"
echo "├── Server certificates:"
echo "│   ├── server-cert.pem (Server certificate)"
echo "│   ├── server-key.pem (Server private key)"
echo "│   └── server-keystore.jks (Server keystore for IBM MQ)"
echo "├── Client certificates:"
echo "│   ├── client-cert.pem (Client certificate)"
echo "│   ├── client-key.pem (Client private key)"
echo "│   └── client-keystore.jks (Client keystore for Payara)"
echo "└── truststore.jks (Truststore containing CA certificate)"
echo ""
echo -e "${YELLOW}Default keystore/truststore password: changeit${NC}"
echo ""
echo -e "${GREEN}Configuration for microprofile-config.properties:${NC}"
echo "ibm.mq.ssl.enabled=true"
echo "ibm.mq.ssl.cipher.suite=TLS_RSA_WITH_AES_256_CBC_SHA256"
echo "ibm.mq.ssl.keystore.path=docker/ssl/certs/client-keystore.jks"
echo "ibm.mq.ssl.keystore.password=changeit"
echo "ibm.mq.ssl.truststore.path=docker/ssl/certs/truststore.jks"
echo "ibm.mq.ssl.truststore.password=changeit"
echo ""
echo -e "${RED}WARNING: These certificates are for development only!${NC}"
echo -e "${RED}Do not use in production environments.${NC}"