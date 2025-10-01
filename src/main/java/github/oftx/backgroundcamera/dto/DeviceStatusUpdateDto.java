package github.oftx.backgroundcamera.dto;

import java.util.Objects;

public class DeviceStatusUpdateDto {
    private String deviceId;
    private DeviceStatusDto status;

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public DeviceStatusDto getStatus() { return status; }
    public void setStatus(DeviceStatusDto status) { this.status = status; }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceStatusUpdateDto that = (DeviceStatusUpdateDto) o;
        return Objects.equals(deviceId, that.deviceId) && Objects.equals(status, that.status);
    }
    @Override
    public int hashCode() { return Objects.hash(deviceId, status); }
    @Override
    public String toString() { return "DeviceStatusUpdateDto{" + "deviceId='" + deviceId + '\'' + ", status=" + status + '}'; }
}