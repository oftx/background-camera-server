package github.oftx.backgroundcamera.dto;

import java.util.Objects;

/**
 * A DTO specifically for broadcasting a device's online status change via WebSocket.
 */
public class DeviceOnlineStatusDto {
    private String deviceId;
    private boolean isOnline;

    public DeviceOnlineStatusDto() {
    }

    public DeviceOnlineStatusDto(String deviceId, boolean isOnline) {
        this.deviceId = deviceId;
        this.isOnline = isOnline;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceOnlineStatusDto that = (DeviceOnlineStatusDto) o;
        return isOnline == that.isOnline && Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, isOnline);
    }

    @Override
    public String toString() {
        return "DeviceOnlineStatusDto{" +
                "deviceId='" + deviceId + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }
}