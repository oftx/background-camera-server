package github.oftx.backgroundcamera.dto;

import java.time.Instant;
import java.util.Objects;

public class PhotoDto {
    private String photoId;
    private String deviceId;
    private String url;
    private Instant timestamp;

    // Constructors
    public PhotoDto() {}
    public PhotoDto(String photoId, String deviceId, String url, Instant timestamp) {
        this.photoId = photoId;
        this.deviceId = deviceId;
        this.url = url;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhotoDto photoDto = (PhotoDto) o;
        return Objects.equals(photoId, photoDto.photoId) && Objects.equals(deviceId, photoDto.deviceId) && Objects.equals(url, photoDto.url) && Objects.equals(timestamp, photoDto.timestamp);
    }
    @Override
    public int hashCode() { return Objects.hash(photoId, deviceId, url, timestamp); }
    @Override
    public String toString() { return "PhotoDto{" + "photoId='" + photoId + '\'' + ", deviceId='" + deviceId + '\'' + ", url='" + url + '\'' + ", timestamp=" + timestamp + '}'; }
}