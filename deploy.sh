#!/bin/bash

echo "=== Payara Application Deployment Script ==="

# Wait for Payara to be fully started
echo "Waiting for Payara server to be ready..."
timeout=60
count=0
while [ $count -lt $timeout ]; do
    if curl -s http://localhost:8080/ > /dev/null 2>&1; then
        echo "Payara server is ready!"
        break
    fi
    echo "Waiting for server... ($count/$timeout)"
    sleep 2
    count=$((count + 2))
done

if [ $count -ge $timeout ]; then
    echo "ERROR: Payara server did not start within $timeout seconds"
    exit 1
fi

# Deploy the application by copying to autodeploy folder
echo "Deploying payara6-ibmmq.war via autodeploy..."
podman compose exec payara cp /opt/payara/payara6-ibmmq.war /opt/payara/appserver/glassfish/domains/domain1/autodeploy/

# Wait for autodeploy to process
echo "Waiting for autodeploy to process..."
sleep 10

# Check deployment status using local asadmin
echo "Checking deployment status..."
podman compose exec payara bash -c "cd /opt/payara/appserver && ./bin/asadmin list-applications"

# Test endpoints
echo "Testing application endpoints..."
echo "1. Testing MQ health endpoint:"
curl -s http://localhost:8080/payara6-ibmmq/api/mq/health || echo "MQ health endpoint not responding"

echo -e "\n2. Testing batch jobs endpoint:"
curl -s http://localhost:8080/payara6-ibmmq/api/batch/jobs || echo "Batch jobs endpoint not responding"

echo -e "\n3. Testing metrics health endpoint:"
curl -s http://localhost:8080/payara6-ibmmq/api/metrics/health || echo "Metrics health endpoint not responding"

echo -e "\n=== Deployment completed ==="