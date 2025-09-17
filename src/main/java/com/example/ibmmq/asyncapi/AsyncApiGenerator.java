package com.example.ibmmq.asyncapi;

import com.example.ibmmq.entity.MQMessage;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automatically generates AsyncAPI specification from application message classes
 * and IBM MQ configuration.
 */
@ApplicationScoped
public class AsyncApiGenerator {

    private static final Logger LOGGER = Logger.getLogger(AsyncApiGenerator.class.getName());

    @Inject
    @ConfigProperty(name = "asyncapi.enabled", defaultValue = "true")
    private Boolean asyncApiEnabled;

    @Inject
    @ConfigProperty(name = "asyncapi.outputPath", defaultValue = "target/generated-asyncapi.yaml")
    private String outputPath;

    @Inject
    @ConfigProperty(name = "ibm.mq.queueManager", defaultValue = "QM1")
    private String queueManager;

    @Inject
    @ConfigProperty(name = "ibm.mq.hostname", defaultValue = "localhost")
    private String mqHostname;

    @Inject
    @ConfigProperty(name = "ibm.mq.port", defaultValue = "1414")
    private Integer mqPort;

    @Inject
    @ConfigProperty(name = "ibm.mq.channel", defaultValue = "DEV.APP.SVRCONN")
    private String mqChannel;

    @PostConstruct
    public void generateAsyncApiSpec() {
        if (!asyncApiEnabled) {
            LOGGER.info("AsyncAPI generation is disabled");
            return;
        }

        try {
            LOGGER.info("Generating AsyncAPI specification...");
            JSONObject asyncApiSpec = createAsyncApiSpecification();
            writeSpecificationToFile(asyncApiSpec);
            LOGGER.info("AsyncAPI specification generated successfully at: " + outputPath);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to generate AsyncAPI specification", e);
        }
    }

    private JSONObject createAsyncApiSpecification() {
        JSONObject spec = new JSONObject();

        // AsyncAPI version and info
        spec.put("asyncapi", "3.0.0");
        spec.put("info", createInfoSection());
        spec.put("defaultContentType", "text/plain");
        spec.put("servers", createServersSection());
        spec.put("channels", createChannelsSection());
        spec.put("operations", createOperationsSection());
        spec.put("components", createComponentsSection());

        return spec;
    }

    private JSONObject createInfoSection() {
        JSONObject info = new JSONObject();
        info.put("title", "IBM MQ Integration AsyncAPI (Auto-Generated)");
        info.put("version", "1.0.0");
        info.put("description", """
            Automatically generated AsyncAPI specification for IBM MQ Integration Service.

            Generated on: %s

            This specification is automatically updated when:
            - Message classes are modified
            - IBM MQ configuration changes
            - Application is redeployed
            """.formatted(LocalDateTime.now()));

        JSONObject contact = new JSONObject();
        contact.put("name", "Development Team");
        contact.put("email", "dev@example.com");
        info.put("contact", contact);

        JSONObject license = new JSONObject();
        license.put("name", "Apache 2.0");
        license.put("url", "https://www.apache.org/licenses/LICENSE-2.0.html");
        info.put("license", license);

        return info;
    }

    private JSONObject createServersSection() {
        JSONObject servers = new JSONObject();

        JSONObject development = new JSONObject();
        development.put("host", mqHostname + ":" + mqPort);
        development.put("protocol", "ibmmq");
        development.put("description", "Development IBM MQ server");

        JSONObject variables = new JSONObject();
        variables.put("queueManager", new JSONObject()
            .put("description", "IBM MQ Queue Manager")
            .put("default", queueManager));
        variables.put("channel", new JSONObject()
            .put("description", "IBM MQ Channel")
            .put("default", mqChannel));
        development.put("variables", variables);

        JSONArray security = new JSONArray();
        security.put(new JSONObject().put("mqBasicAuth", new JSONArray()));
        development.put("security", security);

        servers.put("development", development);

        return servers;
    }

    private JSONObject createChannelsSection() {
        JSONObject channels = new JSONObject();

        // Request queue
        JSONObject requestQueue = new JSONObject();
        requestQueue.put("address", "DEV.QUEUE.1");
        requestQueue.put("title", "Request Queue");
        requestQueue.put("description", "Primary queue for incoming messages");

        JSONObject requestMessages = new JSONObject();
        requestMessages.put("incomingMessage", new JSONObject().put("$ref", "#/components/messages/IncomingMessage"));
        requestQueue.put("messages", requestMessages);

        channels.put("request-queue", requestQueue);

        // Response queue
        JSONObject responseQueue = new JSONObject();
        responseQueue.put("address", "DEV.QUEUE.2");
        responseQueue.put("title", "Response Queue");
        responseQueue.put("description", "Queue for outgoing response messages");

        JSONObject responseMessages = new JSONObject();
        responseMessages.put("outgoingMessage", new JSONObject().put("$ref", "#/components/messages/OutgoingMessage"));
        responseQueue.put("messages", responseMessages);

        channels.put("response-queue", responseQueue);

        // Dead letter queue
        JSONObject dlq = new JSONObject();
        dlq.put("address", "DEV.QUEUE.DLQ");
        dlq.put("title", "Dead Letter Queue");
        dlq.put("description", "Queue for failed messages");

        JSONObject dlqMessages = new JSONObject();
        dlqMessages.put("failedMessage", new JSONObject().put("$ref", "#/components/messages/FailedMessage"));
        dlq.put("messages", dlqMessages);

        channels.put("dead-letter-queue", dlq);

        return channels;
    }

    private JSONObject createOperationsSection() {
        JSONObject operations = new JSONObject();

        // Send message operation
        JSONObject sendMessage = new JSONObject();
        sendMessage.put("action", "send");
        sendMessage.put("channel", new JSONObject().put("$ref", "#/channels/request-queue"));
        sendMessage.put("title", "Send Message to Request Queue");
        sendMessage.put("description", "Publishes a message to the primary request queue");
        operations.put("sendMessage", sendMessage);

        // Receive message operation
        JSONObject receiveMessage = new JSONObject();
        receiveMessage.put("action", "receive");
        receiveMessage.put("channel", new JSONObject().put("$ref", "#/channels/request-queue"));
        receiveMessage.put("title", "Receive Message from Request Queue");
        receiveMessage.put("description", "Consumes messages from the request queue");
        operations.put("receiveMessage", receiveMessage);

        // Send response operation
        JSONObject sendResponse = new JSONObject();
        sendResponse.put("action", "send");
        sendResponse.put("channel", new JSONObject().put("$ref", "#/channels/response-queue"));
        sendResponse.put("title", "Send Response Message");
        sendResponse.put("description", "Sends processed response messages");
        operations.put("sendResponse", sendResponse);

        return operations;
    }

    private JSONObject createComponentsSection() {
        JSONObject components = new JSONObject();
        components.put("messages", createMessagesSection());
        components.put("schemas", createSchemasSection());
        components.put("messageTraits", createMessageTraitsSection());
        components.put("securitySchemes", createSecuritySchemesSection());

        return components;
    }

    private JSONObject createMessagesSection() {
        JSONObject messages = new JSONObject();

        // Incoming message
        JSONObject incomingMessage = new JSONObject();
        incomingMessage.put("name", "IncomingMessage");
        incomingMessage.put("title", "Incoming Message");
        incomingMessage.put("contentType", "text/plain");
        incomingMessage.put("payload", new JSONObject().put("$ref", "#/components/schemas/TextMessage"));
        messages.put("IncomingMessage", incomingMessage);

        // Outgoing message
        JSONObject outgoingMessage = new JSONObject();
        outgoingMessage.put("name", "OutgoingMessage");
        outgoingMessage.put("title", "Outgoing Message");
        outgoingMessage.put("contentType", "text/plain");
        outgoingMessage.put("payload", new JSONObject().put("$ref", "#/components/schemas/ProcessedMessage"));
        messages.put("OutgoingMessage", outgoingMessage);

        // Failed message
        JSONObject failedMessage = new JSONObject();
        failedMessage.put("name", "FailedMessage");
        failedMessage.put("title", "Failed Message");
        failedMessage.put("contentType", "text/plain");
        failedMessage.put("payload", new JSONObject().put("$ref", "#/components/schemas/FailedPayload"));
        messages.put("FailedMessage", failedMessage);

        return messages;
    }

    private JSONObject createSchemasSection() {
        JSONObject schemas = new JSONObject();

        // Text message schema
        JSONObject textMessage = new JSONObject();
        textMessage.put("type", "string");
        textMessage.put("description", "Simple text message content");
        textMessage.put("examples", Arrays.asList("Hello World", "Processing request #12345"));
        schemas.put("TextMessage", textMessage);

        // Processed message schema (generated from MQMessage entity)
        JSONObject processedMessage = createSchemaFromEntity(MQMessage.class);
        schemas.put("ProcessedMessage", processedMessage);

        // Failed payload schema
        JSONObject failedPayload = new JSONObject();
        failedPayload.put("type", "object");
        failedPayload.put("description", "Failed message with error information");

        JSONObject failedProperties = new JSONObject();
        failedProperties.put("originalMessage", new JSONObject()
            .put("type", "string")
            .put("description", "Original message that failed"));
        failedProperties.put("error", new JSONObject()
            .put("type", "string")
            .put("description", "Error description"));
        failedProperties.put("timestamp", new JSONObject()
            .put("type", "string")
            .put("format", "date-time")
            .put("description", "Failure timestamp"));

        failedPayload.put("properties", failedProperties);
        failedPayload.put("required", Arrays.asList("originalMessage", "error", "timestamp"));
        schemas.put("FailedPayload", failedPayload);

        return schemas;
    }

    private JSONObject createSchemaFromEntity(Class<?> entityClass) {
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("description", "Generated schema for " + entityClass.getSimpleName());

        JSONObject properties = new JSONObject();
        List<String> required = Arrays.asList("id", "messageId", "queueName", "status");

        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue; // Skip static fields
            }

            String fieldName = field.getName();
            JSONObject fieldSchema = new JSONObject();

            // Map Java types to JSON schema types
            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                fieldSchema.put("type", "string");
            } else if (fieldType == Long.class || fieldType == long.class) {
                fieldSchema.put("type", "integer");
                fieldSchema.put("format", "int64");
            } else if (fieldType == Integer.class || fieldType == int.class) {
                fieldSchema.put("type", "integer");
            } else if (fieldType == LocalDateTime.class) {
                fieldSchema.put("type", "string");
                fieldSchema.put("format", "date-time");
            } else if (fieldType.isEnum()) {
                fieldSchema.put("type", "string");
                Object[] enumConstants = fieldType.getEnumConstants();
                JSONArray enumValues = new JSONArray();
                for (Object enumConstant : enumConstants) {
                    enumValues.put(enumConstant.toString());
                }
                fieldSchema.put("enum", enumValues);
            } else {
                fieldSchema.put("type", "object");
            }

            // Add descriptions based on field names
            fieldSchema.put("description", generateFieldDescription(fieldName));

            properties.put(fieldName, fieldSchema);
        }

        schema.put("properties", properties);
        schema.put("required", required);

        return schema;
    }

    private String generateFieldDescription(String fieldName) {
        return switch (fieldName) {
            case "id" -> "Database primary key";
            case "messageId" -> "Unique message identifier";
            case "correlationId" -> "Correlation ID for request-response patterns";
            case "queueName" -> "Source or target queue name";
            case "messageContent" -> "Message payload content";
            case "messageType" -> "Message type classification";
            case "priority" -> "Message priority (0-9)";
            case "expiry" -> "Message expiry timestamp";
            case "receivedAt" -> "When the message was received";
            case "processedAt" -> "When the message was processed";
            case "status" -> "Message processing status";
            case "errorMessage" -> "Error message if processing failed";
            case "retryCount" -> "Number of retry attempts";
            case "backoutCount" -> "Number of backout attempts";
            case "backoutAt" -> "When the message was backed out";
            case "version" -> "Entity version for optimistic locking";
            default -> "Field: " + fieldName;
        };
    }

    private JSONObject createMessageTraitsSection() {
        JSONObject traits = new JSONObject();

        JSONObject mqTrait = new JSONObject();
        JSONObject headers = new JSONObject();
        headers.put("type", "object");

        JSONObject headerProperties = new JSONObject();
        headerProperties.put("JMSMessageID", new JSONObject()
            .put("type", "string")
            .put("description", "IBM MQ message identifier"));
        headerProperties.put("JMSCorrelationID", new JSONObject()
            .put("type", "string")
            .put("description", "Correlation identifier"));
        headerProperties.put("JMSTimestamp", new JSONObject()
            .put("type", "integer")
            .put("format", "int64")
            .put("description", "Message timestamp"));

        headers.put("properties", headerProperties);
        mqTrait.put("headers", headers);
        traits.put("MQMessageTrait", mqTrait);

        return traits;
    }

    private JSONObject createSecuritySchemesSection() {
        JSONObject schemes = new JSONObject();

        JSONObject basicAuth = new JSONObject();
        basicAuth.put("type", "userPassword");
        basicAuth.put("description", "IBM MQ basic authentication");
        schemes.put("mqBasicAuth", basicAuth);

        return schemes;
    }

    private void writeSpecificationToFile(JSONObject spec) throws IOException {
        Path filePath = Paths.get(outputPath);
        filePath.getParent().toFile().mkdirs(); // Create directories if they don't exist

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // Convert JSON to YAML-like format (simplified)
            String yamlContent = jsonToYaml(spec, 0);
            writer.write(yamlContent);
        }
    }

    private String jsonToYaml(Object obj, int indent) {
        StringBuilder yaml = new StringBuilder();
        String indentation = "  ".repeat(indent);

        if (obj instanceof JSONObject jsonObj) {
            for (String key : jsonObj.keySet()) {
                Object value = jsonObj.get(key);
                yaml.append(indentation).append(key).append(":");

                if (value instanceof JSONObject || value instanceof JSONArray) {
                    yaml.append("\n").append(jsonToYaml(value, indent + 1));
                } else if (value instanceof String) {
                    String stringValue = (String) value;
                    if (stringValue.contains("\n")) {
                        yaml.append(" |\n");
                        for (String line : stringValue.split("\n")) {
                            yaml.append(indentation).append("  ").append(line).append("\n");
                        }
                    } else {
                        yaml.append(" ").append(stringValue).append("\n");
                    }
                } else {
                    yaml.append(" ").append(value).append("\n");
                }
            }
        } else if (obj instanceof JSONArray jsonArray) {
            for (int i = 0; i < jsonArray.length(); i++) {
                Object item = jsonArray.get(i);
                yaml.append(indentation).append("- ");

                if (item instanceof JSONObject || item instanceof JSONArray) {
                    yaml.append("\n").append(jsonToYaml(item, indent + 1));
                } else {
                    yaml.append(item).append("\n");
                }
            }
        }

        return yaml.toString();
    }
}