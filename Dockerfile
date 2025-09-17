FROM payara/server-full:6.2024.7-jdk17

# Copy application configuration
COPY src/main/resources/META-INF/microprofile-config.properties /opt/payara/appserver/domains/domain1/config/

# Copy the built application to a temporary location (not autodeploy yet)
COPY target/payara6-ibmmq.war /opt/payara/payara6-ibmmq.war

# Copy startup script
COPY startup.sh /opt/payara/startup.sh

# Expose ports
EXPOSE 8080 4848 9009

# Set environment variables for IBM MQ connection
ENV IBM_MQ_HOSTNAME=ibmmq \
    IBM_MQ_PORT=1414 \
    IBM_MQ_QUEUE_MANAGER=QM1 \
    IBM_MQ_CHANNEL=DEV.APP.SVRCONN \
    IBM_MQ_USERNAME=app \
    IBM_MQ_PASSWORD=passw0rd

# Start Payara Server using startup script
CMD ["bash", "/opt/payara/startup.sh"]