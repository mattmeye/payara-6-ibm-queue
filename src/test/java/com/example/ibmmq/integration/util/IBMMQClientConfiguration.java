package com.example.ibmmq.integration.util;

import com.example.ibmmq.client.ApiClient;
import com.example.ibmmq.client.Configuration;
import com.example.ibmmq.client.api.MqApi;
import com.example.ibmmq.client.api.MessagesApi;
import com.example.ibmmq.client.api.HealthApi;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Configuration utility for IBM MQ OpenAPI client used in integration tests.
 * Provides pre-configured API clients with proper base URLs and logging.
 */
public class IBMMQClientConfiguration {

    private final String baseUrl;
    private final ApiClient apiClient;

    public IBMMQClientConfiguration(String baseUrl) {
        this.baseUrl = baseUrl;
        this.apiClient = createConfiguredClient();
    }

    /**
     * Creates a configured ApiClient with proper base URL and logging
     */
    private ApiClient createConfiguredClient() {
        ApiClient client = Configuration.getDefaultApiClient();
        client.setBasePath(baseUrl);

        // Add logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.getHttpClient().newBuilder()
            .addInterceptor(logging)
            .build();

        // Set reasonable timeouts
        client.setConnectTimeout(30000); // 30 seconds
        client.setReadTimeout(60000);    // 60 seconds
        client.setWriteTimeout(60000);   // 60 seconds

        return client;
    }

    /**
     * Get pre-configured MQ API client
     */
    public MqApi getMqApi() {
        return new MqApi(apiClient);
    }

    /**
     * Get pre-configured Messages API client
     */
    public MessagesApi getMessagesApi() {
        return new MessagesApi(apiClient);
    }

    /**
     * Get pre-configured Health API client
     */
    public HealthApi getHealthApi() {
        return new HealthApi(apiClient);
    }

    /**
     * Get the underlying ApiClient for advanced configuration
     */
    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Create client configuration from TestContainer
     */
    public static IBMMQClientConfiguration fromContainer(String host, Integer port) {
        String baseUrl = String.format("http://%s:%d/api", host, port);
        return new IBMMQClientConfiguration(baseUrl);
    }

    /**
     * Create client configuration with base URL
     */
    public static IBMMQClientConfiguration withBaseUrl(String baseUrl) {
        return new IBMMQClientConfiguration(baseUrl);
    }
}