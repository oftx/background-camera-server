package github.oftx.backgroundcamera.dto;

public class DeviceBindingResponseDto {
    private String deviceId;
    private String authToken;

    public DeviceBindingResponseDto(String deviceId, String authToken) {
        this.deviceId = deviceId;
        this.authToken = authToken;
    }

    // Getters
    public String getDeviceId() { return deviceId; }
    public String getAuthToken() { return authToken; }
}