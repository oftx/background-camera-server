package github.oftx.backgroundcamera.dto;

import java.util.Objects;

public class DeviceStatusDto {
    private boolean isServiceRunning;
    private int captureInterval;
    private String selectedCameraId;

    // Getters and Setters
    public boolean isServiceRunning() { return isServiceRunning; }
    public void setServiceRunning(boolean serviceRunning) { isServiceRunning = serviceRunning; }
    public int getCaptureInterval() { return captureInterval; }
    public void setCaptureInterval(int captureInterval) { this.captureInterval = captureInterval; }
    public String getSelectedCameraId() { return selectedCameraId; }
    public void setSelectedCameraId(String selectedCameraId) { this.selectedCameraId = selectedCameraId; }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceStatusDto that = (DeviceStatusDto) o;
        return isServiceRunning == that.isServiceRunning && captureInterval == that.captureInterval && Objects.equals(selectedCameraId, that.selectedCameraId);
    }
    @Override
    public int hashCode() { return Objects.hash(isServiceRunning, captureInterval, selectedCameraId); }
    @Override
    public String toString() { return "DeviceStatusDto{" + "isServiceRunning=" + isServiceRunning + ", captureInterval=" + captureInterval + ", selectedCameraId='" + selectedCameraId + '\'' + '}'; }
}