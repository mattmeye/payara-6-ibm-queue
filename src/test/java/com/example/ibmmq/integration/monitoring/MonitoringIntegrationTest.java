package com.example.ibmmq.integration.monitoring;

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
@DisplayName("Monitoring and Metrics Integration Tests")
class MonitoringIntegrationTest {

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
    @DisplayName("Should provide application health metrics")
    void shouldProvideApplicationHealthMetrics() {
        given()
                .when()
                .get("/mq/health")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("healthy"))
                .body("service", equalTo("IBM MQ Integration"))
                .body("timestamp", notNullValue())
                .body("uptime", greaterThan(0));
    }

    @Test
    @DisplayName("Should expose Prometheus metrics endpoint")
    void shouldExposePrometheusMetricsEndpoint() {
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .contentType(containsString("text/plain"))
                .body(containsString("# HELP"))
                .body(containsString("# TYPE"))
                .body(containsString("jvm_"))
                .body(containsString("http_"));
    }

    @Test
    @DisplayName("Should track message send operations in metrics")
    void shouldTrackMessageSendOperationsInMetrics() {
        // Send a message to generate metrics
        String testMessage = "Metrics test message " + System.currentTimeMillis();

        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200);

        // Check metrics reflect the operation
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("mq_messages_sent"))
                .body(containsString("mq_send_duration"));
    }

    @Test
    @DisplayName("Should track message receive operations in metrics")
    void shouldTrackMessageReceiveOperationsInMetrics() {
        // Send a message first
        String testMessage = "Receive metrics test " + System.currentTimeMillis();
        given()
                .contentType(ContentType.TEXT)
                .body(testMessage)
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200);

        // Receive the message
        given()
                .when()
                .get("/mq/receive")
                .then()
                .statusCode(200);

        // Check metrics reflect both operations
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("mq_messages_received"))
                .body(containsString("mq_receive_duration"));
    }

    @Test
    @DisplayName("Should track error rates in metrics")
    void shouldTrackErrorRatesInMetrics() {
        // Generate an error by sending to invalid queue
        given()
                .contentType(ContentType.TEXT)
                .body("Error test message")
                .when()
                .post("/mq/send/INVALID..QUEUE..NAME")
                .then()
                .statusCode(anyOf(is(400), is(500)));

        // Check error metrics
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("mq_errors"))
                .body(containsString("http_requests_total"));
    }

    @Test
    @DisplayName("Should provide connection pool metrics")
    void shouldProvideConnectionPoolMetrics() {
        given()
                .when()
                .get("/mq/pool/status")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("totalConnections", greaterThanOrEqualTo(0))
                .body("activeConnections", greaterThanOrEqualTo(0))
                .body("availableConnections", greaterThanOrEqualTo(0))
                .body("maxPoolSize", greaterThan(0));
    }

    @Test
    @DisplayName("Should provide JVM metrics")
    void shouldProvideJVMMetrics() {
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("jvm_memory_used_bytes"))
                .body(containsString("jvm_gc_collection_seconds"))
                .body(containsString("jvm_threads_current"))
                .body(containsString("jvm_classes_loaded"));
    }

    @Test
    @DisplayName("Should provide HTTP request metrics")
    void shouldProvideHTTPRequestMetrics() {
        // Make some HTTP requests
        given()
                .when()
                .get("/mq/health")
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/mq/health")
                .then()
                .statusCode(200);

        // Check HTTP metrics
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("http_requests_total"))
                .body(containsString("http_request_duration_seconds"));
    }

    @Test
    @DisplayName("Should track batch job metrics")
    void shouldTrackBatchJobMetrics() {
        // Trigger a batch job
        given()
                .when()
                .post("/batch/start")
                .then()
                .statusCode(anyOf(is(200), is(202)));

        // Check batch metrics
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("batch_job"));
    }

    @Test
    @DisplayName("Should provide database connection metrics")
    void shouldProvideDatabaseConnectionMetrics() {
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("hikaricp_connections"))
                .body(containsString("database_"));
    }

    @Test
    @DisplayName("Should track custom business metrics")
    void shouldTrackCustomBusinessMetrics() {
        // Perform business operations that should be tracked
        given()
                .contentType(ContentType.TEXT)
                .body("Business metrics test")
                .when()
                .post("/mq/send")
                .then()
                .statusCode(200);

        // Check custom metrics
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("business_"))
                .body(containsString("application_"));
    }

    @Test
    @DisplayName("Should support metrics filtering by tags")
    void shouldSupportMetricsFilteringByTags() {
        given()
                .queryParam("name[]", "http_requests_total")
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("http_requests_total"))
                .body(not(containsString("jvm_memory")));
    }

    @Test
    @DisplayName("Should provide metrics in different formats")
    void shouldProvideMetricsInDifferentFormats() {
        // Prometheus format (default)
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .contentType(containsString("text/plain"));

        // JSON format (if supported)
        given()
                .header("Accept", "application/json")
                .when()
                .get("/metrics")
                .then()
                .statusCode(anyOf(is(200), is(406))); // 406 if not supported
    }

    @Test
    @DisplayName("Should handle high-frequency metrics collection")
    void shouldHandleHighFrequencyMetricsCollection() {
        // Send multiple messages quickly
        for (int i = 0; i < 10; i++) {
            given()
                    .contentType(ContentType.TEXT)
                    .body("High frequency test " + i)
                    .when()
                    .post("/mq/send")
                    .then()
                    .statusCode(200);
        }

        // Metrics should still be available and accurate
        given()
                .when()
                .get("/metrics")
                .then()
                .statusCode(200)
                .body(containsString("mq_messages_sent"));
    }

    @Test
    @DisplayName("Should provide application-specific metrics")
    void shouldProvideApplicationSpecificMetrics() {
        given()
                .when()
                .get("/mq/metrics")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("messagesSent", greaterThanOrEqualTo(0))
                .body("messagesReceived", greaterThanOrEqualTo(0))
                .body("averageProcessingTime", greaterThanOrEqualTo(0))
                .body("queueDepth", greaterThanOrEqualTo(0));
    }
}