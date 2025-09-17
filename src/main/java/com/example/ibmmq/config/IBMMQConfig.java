package com.example.ibmmq.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class IBMMQConfig {

    @ConfigProperty(name = "ibm.mq.queueManager", defaultValue = "QM1")
    private String queueManager;

    @ConfigProperty(name = "ibm.mq.hostname", defaultValue = "localhost")
    private String hostname;

    @ConfigProperty(name = "ibm.mq.port", defaultValue = "1414")
    private int port;

    @ConfigProperty(name = "ibm.mq.channel", defaultValue = "DEV.APP.SVRCONN")
    private String channel;

    @ConfigProperty(name = "ibm.mq.username", defaultValue = "app")
    private String username;

    @ConfigProperty(name = "ibm.mq.password", defaultValue = "passw0rd")
    private String password;

    @ConfigProperty(name = "ibm.mq.queue.request", defaultValue = "DEV.QUEUE.1")
    private String requestQueue;

    @ConfigProperty(name = "ibm.mq.queue.response", defaultValue = "DEV.QUEUE.2")
    private String responseQueue;

    public String getQueueManager() {
        return queueManager;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getChannel() {
        return channel;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRequestQueue() {
        return requestQueue;
    }

    public String getResponseQueue() {
        return responseQueue;
    }
}