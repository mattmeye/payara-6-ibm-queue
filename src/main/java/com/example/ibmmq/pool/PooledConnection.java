package com.example.ibmmq.pool;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

import java.util.concurrent.atomic.AtomicBoolean;

public class PooledConnection {

    private final Connection connection;
    private final long createdTime;
    private volatile long lastUsed;
    private final AtomicBoolean inUse;

    public PooledConnection(Connection connection) {
        this.connection = connection;
        this.createdTime = System.currentTimeMillis();
        this.lastUsed = createdTime;
        this.inUse = new AtomicBoolean(false);
    }

    public Connection getConnection() {
        return connection;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public boolean isInUse() {
        return inUse.get();
    }

    public void markAsActive() {
        inUse.set(true);
        lastUsed = System.currentTimeMillis();
    }

    public void markAsAvailable() {
        inUse.set(false);
        lastUsed = System.currentTimeMillis();
    }

    public long getAge() {
        return System.currentTimeMillis() - createdTime;
    }

    public long getIdleTime() {
        return System.currentTimeMillis() - lastUsed;
    }

    public void close() throws JMSException {
        if (connection != null) {
            connection.close();
        }
    }
}