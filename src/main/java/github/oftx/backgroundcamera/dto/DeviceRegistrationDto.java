package github.oftx.backgroundcamera.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class DeviceRegistrationDto {
    @NotBlank
    private String deviceId;

    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceRegistrationDto that = (DeviceRegistrationDto) o;
        return Objects.equals(deviceId, that.deviceId);
    }
    @Override
    public int hashCode() { return Objects.hash(deviceId); }
    @Override
    public String toString() { return "DeviceRegistrationDto{" + "deviceId='" + deviceId + '\'' + '}'; }
}