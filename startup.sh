#!/bin/bash

echo "Starting Payara Server..."

# Clean up any existing domain first
echo "Cleaning up existing domain..."
asadmin stop-domain domain1 2>/dev/null || true

# Start Payara domain
echo "Starting domain1..."
asadmin start-domain --verbose domain1

# Wait for server to be fully ready
echo "Waiting for server to be ready..."
timeout=60
count=0
while [ $count -lt $timeout ]; do
    if asadmin list-applications >/dev/null 2>&1; then
        echo "Server is ready!"
        break
    fi
    echo "Waiting... ($count/$timeout)"
    sleep 2
    count=$((count + 2))
done

if [ $count -ge $timeout ]; then
    echo "Server did not start properly within $timeout seconds"
    echo "Checking logs..."
    tail -50 /opt/payara/appserver/glassfish/domains/domain1/logs/server.log
    exit 1
fi

echo "Server started successfully and is ready for deployment."

# Deploy the application
echo "Deploying application..."
if [ -f "/opt/payara/payara6-ibmmq.war" ]; then
    asadmin deploy --contextroot "/api" /opt/payara/payara6-ibmmq.war
    if [ $? -eq 0 ]; then
        echo "Application deployed successfully!"
    else
        echo "Application deployment failed!"
        echo "Checking logs for deployment errors..."
        tail -20 /opt/payara/appserver/glassfish/domains/domain1/logs/server.log
    fi
else
    echo "WAR file not found at /opt/payara/payara6-ibmmq.war"
fi

# Keep container running and follow logs
tail -f /opt/payara/appserver/glassfish/domains/domain1/logs/server.log