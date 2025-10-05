package github.oftx.backgroundcamera.controller.rest;

import github.oftx.backgroundcamera.dto.DeviceBindingResponseDto;
import github.oftx.backgroundcamera.dto.DeviceDto;
import github.oftx.backgroundcamera.dto.UpdateDeviceNameRequestDto; // 【新增】导入
import github.oftx.backgroundcamera.service.DeviceService;
import jakarta.validation.Valid; // 【新增】导入
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDto>> getDevices(Principal principal) {
        String username = principal.getName();
        List<DeviceDto> devices = deviceService.findDevicesByUsername(username);
        return ResponseEntity.ok(devices);
    }

    @PostMapping("/{deviceId}/bind")
    public ResponseEntity<DeviceBindingResponseDto> bindDevice(
            @PathVariable String deviceId,
            Principal principal) {
        String username = principal.getName();
        DeviceBindingResponseDto response = deviceService.bindDeviceToUser(deviceId, username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{deviceId}/unbind")
    public ResponseEntity<Void> unbindDevice(
            @PathVariable String deviceId,
            Principal principal) {
        String username = principal.getName();
        deviceService.unbindDevice(deviceId, username);
        return ResponseEntity.noContent().build();
    }

    // 【新增】更新设备名称的API端点
    @PatchMapping("/{deviceId}")
    public ResponseEntity<DeviceDto> updateDeviceName(
            @PathVariable String deviceId,
            @RequestBody @Valid UpdateDeviceNameRequestDto request,
            Principal principal) {
        String username = principal.getName();
        DeviceDto updatedDevice = deviceService.updateDeviceName(deviceId, request.getName(), username);
        return ResponseEntity.ok(updatedDevice);
    }
}