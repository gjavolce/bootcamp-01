package com.bojan.bootcamp_01.session;

import java.io.Serializable;
import java.time.Instant;

public class UserSession implements Serializable {
    private Long userId;
    private String deviceId;
    private String ipAddress;
    private Instant createdAt;
    private Instant lastActivity;
    private boolean active;

    public UserSession() {
    }

    public UserSession(Long userId, String deviceId, String ipAddress, Instant createdAt, Instant lastActivity,
            boolean active) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
        this.lastActivity = lastActivity;
        this.active = active;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Instant lastActivity) {
        this.lastActivity = lastActivity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
