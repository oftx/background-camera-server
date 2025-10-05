package github.oftx.backgroundcamera.dto;

import java.util.Objects;

public class DeviceDto {
    private String id;
    private String name;
    private boolean isOnline;

    public DeviceDto() {
    }

    public DeviceDto(String id, String name, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.isOnline = isOnline;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDto deviceDto = (DeviceDto) o;
        return isOnline == deviceDto.isOnline && Objects.equals(id, deviceDto.id) && Objects.equals(name, deviceDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, isOnline);
    }
}