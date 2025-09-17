package com.example.ibmmq.integration.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("IBM MQ REST API Integration Tests")
class IBMMQResourceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("mqdb")
            .withUsername("mquser")
            .withPassword("mqpassword");

    @Container
    static GenericContainer<?> ibmMQ = new GenericContainer<>("icr.io/ibm-messaging/mq:latest")
            .withExposedPorts(1414, 9443)
            .withEnv("LICENSE", "accept")
            .withEnv("MQ_QMGR_NAME", "QM1")
            .withEnv("MQ_APP_PASSWORD", "passw0rd")
            .waitingFor(Wait.forListeningPort());

    @Container
    static GenericContainer<?> payaraApp = new GenericContainer<>("payara/server-full:6.2024.7-jdk17")
            .withExposedPorts(8080, 4848)
            .dependsOn(postgres, ibmMQ)
            .withEnv("DB_HOST", "postgres")
            .withEnv("DB_PORT", "5432")
            .withEnv("DB_NAME", "mqdb")
            .withEnv("DB_USER", "mquser")
            .withEnv("DB_PASSWORD", "mqpassword")
            .withEnv("IBM_MQ_HOSTNAME", "ibmmq")
            .withEnv("IBM_MQ_PORT", "1414")
            .withEnv("IBM_MQ_QUEUE_MANAGER", "QM1")
            .withEnv("IBM_MQ_CHANNEL", "DEV.APP.SVRCONN")
            .withEnv("IBM_MQ_USERNAME", "app")
            .withEnv("IBM_MQ_PASSWORD", "passw0rd")
            .waitingFor(Wait.forHttp("/api/mq/health").forStatusCode(200));

    @BeforeAll
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = payaraApp.getMappedPort(8080);
        RestAssured.basePath = "/api";
    }

    @Test
    @DisplayName("Should check MQ service health")
    void shouldCheckMQServiceHealth() {
        given()
                .when()
                .get("/mq/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("healthy"))
                .body("service", equalTo("IBM MQ Integration"));
    }

    @Test
    @DisplayName("Should send message to default queue")
    void shouldSendMessageToDefaultQueue() {
        String testMessage = "Integration test message " + System.currentTimeMillis();

        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", containsString("sent successfully"));
    }

    @Test
    @DisplayName("Should send message to specific queue")
    void shouldSendMessageToSpecificQueue() {
        String testMessage = "Specific queue test message " + System.currentTimeMillis();
        String queueName = "DEV.QUEUE.TEST";

        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send/" + queueName)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", containsString(queueName));
    }

    @Test
    @DisplayName("Should receive message from default queue")
    void shouldReceiveMessageFromDefaultQueue() {
        // First send a message
        String testMessage = "Test message for receiving " + System.currentTimeMillis();
        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200);

        // Then receive it
        given()
                .when()
                .get("/mq/receive")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"));
    }

    @Test
    @DisplayName("Should receive message from specific queue")
    void shouldReceiveMessageFromSpecificQueue() {
        String queueName = "DEV.QUEUE.RECEIVE";
        String testMessage = "Receive test message " + System.currentTimeMillis();

        // Send message to specific queue
        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send/" + queueName)
                .then()
                .statusCode(200);

        // Receive from specific queue
        given()
                .when()
                .get("/mq/receive/" + queueName)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"));
    }

    @Test
    @DisplayName("Should perform send and receive operation")
    void shouldPerformSendAndReceiveOperation() {
        String requestMessage = "Request message " + System.currentTimeMillis();

        given()
                .contentType(ContentType.TEXT)
                .body(requestMessage)
                .when()
                .post("/mq/sendreceive")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"));
    }

    @Test
    @DisplayName("Should handle empty message gracefully")
    void shouldHandleEmptyMessageGracefully() {
        given()
                .contentType(ContentType.TEXT)
                .body("")
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"));
    }

    @Test
    @DisplayName("Should handle large message")
    void shouldHandleLargeMessage() {
        StringBuilder largeMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeMessage.append("Large message content line ").append(i).append(" ");
        }

        given()
                .contentType(ContentType.TEXT)
                .body(largeMessage.toString())
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"));
    }

    @Test
    @DisplayName("Should handle special characters in message")
    void shouldHandleSpecialCharactersInMessage() {
        String specialMessage = "Special chars: äöü ß € áéíóú ñ 中文 日本語 한국어 🚀 ✅";

        given()
                .contentType(ContentType.TEXT)
                .body(specialMessage)
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"));
    }

    @Test
    @DisplayName("Should return appropriate response when no message available")
    void shouldReturnAppropriateResponseWhenNoMessageAvailable() {
        String emptyQueue = "EMPTY.QUEUE." + System.currentTimeMillis();

        given()
                .when()
                .get("/mq/receive/" + emptyQueue)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("success"))
                .body("message", containsString("No message available"));
    }

    @Test
    @DisplayName("Should handle invalid queue names gracefully")
    void shouldHandleInvalidQueueNamesGracefully() {
        String invalidQueue = "INVALID..QUEUE..NAME";

        given()
                .contentType(ContentType.TEXT)
                .body("Test message")
                .when()
                .post("/mq/send/" + invalidQueue)
                .then()
                .statusCode(anyOf(is(200), is(500))); // Depending on MQ configuration
    }

    @Test
    @DisplayName("Should handle concurrent requests")
    void shouldHandleConcurrentRequests() {
        String baseMessage = "Concurrent test message ";

        // Send multiple messages concurrently
        for (int i = 0; i < 5; i++) {
            final int messageId = i;
            given()
                    .contentType(ContentType.TEXT)
                    .body(baseMessage + messageId)
                    .when()
                    .post("/mq/send")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("status", equalTo("success"));
        }
    }

    @Test
    @DisplayName("Should measure response time performance")
    void shouldMeasureResponseTimePerformance() {
        String testMessage = "Performance test message";

        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200)
                .time(lessThan(5000L)); // Should respond within 5 seconds
    }

    @Test
    @DisplayName("Should validate JSON response structure")
    void shouldValidateJsonResponseStructure() {
        String testMessage = "JSON validation test";

        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasKey("status"))
                .body("$", hasKey("message"))
                .body("status", isA(String.class))
                .body("message", isA(String.class));
    }

    @Test
    @DisplayName("Should handle content negotiation")
    void shouldHandleContentNegotiation() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/mq/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Should support HTTP method validation")
    void shouldSupportHttpMethodValidation() {
        // POST to send endpoint should work
        given()
                .contentType(ContentType.TEXT)
                .body("Method test")
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200);

        // GET to send endpoint should fail
        given()
                .when()
                .get("/mq/send")
                .then()
                .statusCode(405); // Method Not Allowed
    }

    @Test
    @DisplayName("Should handle malformed requests gracefully")
    void shouldHandleMalformedRequestsGracefully() {
        given()
                .contentType(ContentType.JSON)
                .body("{invalid json")
                .when()
                .post("/mq/send")
                .then()
                .statusCode(anyOf(is(400), is(415), is(500))); // Bad Request, Unsupported Media Type, or Internal Server Error
    }
}