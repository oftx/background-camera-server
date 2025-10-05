package github.oftx.backgroundcamera.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateDeviceNameRequestDto {

    @NotBlank(message = "Device name cannot be blank.")
    @Size(max = 100, message = "Device name cannot exceed 100 characters.")
    private String name;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}