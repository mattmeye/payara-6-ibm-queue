package com.example.ibmmq.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@ApplicationScoped
public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    @Produces
    @ApplicationScoped
    public EntityManagerFactory createEntityManagerFactory() {
        try {
            Map<String, Object> properties = new HashMap<>();

            // Database connection properties
            properties.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
            properties.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://localhost:10201/mqdb");
            properties.put("jakarta.persistence.jdbc.user", "mquser");
            properties.put("jakarta.persistence.jdbc.password", "mqpassword");

            // EclipseLink properties
            properties.put("eclipselink.target-database", "PostgreSQL");
            properties.put("eclipselink.ddl-generation", "create-or-extend-tables");
            properties.put("eclipselink.ddl-generation.output-mode", "database");
            properties.put("eclipselink.logging.level", "INFO");
            properties.put("eclipselink.cache.shared.default", "false");

            EntityManagerFactory emf = Persistence.createEntityManagerFactory("mqPU", properties);
            LOGGER.info("EntityManagerFactory created successfully");
            return emf;
        } catch (Exception e) {
            LOGGER.severe("Failed to create EntityManagerFactory: " + e.getMessage());
            throw new RuntimeException("EntityManagerFactory creation failed", e);
        }
    }
}