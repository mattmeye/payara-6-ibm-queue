package com.example.ibmmq.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Named
public class BackoutQueueConfig {

    @ConfigProperty(name = "ibmmq.backout.enabled", defaultValue = "true")
    private boolean backoutEnabled;

    @ConfigProperty(name = "ibmmq.backout.threshold", defaultValue = "3")
    private int backoutThreshold;

    @ConfigProperty(name = "ibmmq.backout.queue.suffix", defaultValue = ".BACKOUT")
    private String backoutQueueSuffix;

    @ConfigProperty(name = "ibmmq.backout.replace.dlq", defaultValue = "false")
    private boolean replaceDlq;

    @ConfigProperty(name = "ibmmq.backout.retention.days", defaultValue = "30")
    private int retentionDays;

    // Getters
    public boolean isBackoutEnabled() { return backoutEnabled; }
    public int getBackoutThreshold() { return backoutThreshold; }
    public String getBackoutQueueSuffix() { return backoutQueueSuffix; }
    public boolean isReplaceDlq() { return replaceDlq; }
    public int getRetentionDays() { return retentionDays; }

    // Setters for testing
    public void setBackoutEnabled(boolean backoutEnabled) { this.backoutEnabled = backoutEnabled; }
    public void setBackoutThreshold(int backoutThreshold) { this.backoutThreshold = backoutThreshold; }
    public void setBackoutQueueSuffix(String backoutQueueSuffix) { this.backoutQueueSuffix = backoutQueueSuffix; }
    public void setReplaceDlq(boolean replaceDlq) { this.replaceDlq = replaceDlq; }
    public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
}