package github.oftx.backgroundcamera.controller.rest;

import github.oftx.backgroundcamera.dto.DeviceBindingResponseDto;
import github.oftx.backgroundcamera.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/{deviceId}/bind")
    public ResponseEntity<DeviceBindingResponseDto> bindDevice(
            @PathVariable String deviceId,
            Principal principal) {
        // The principal is guaranteed by JwtAuthFilter for logged-in users
        String username = principal.getName();
        DeviceBindingResponseDto response = deviceService.bindDeviceToUser(deviceId, username);
        return ResponseEntity.ok(response);
    }
}