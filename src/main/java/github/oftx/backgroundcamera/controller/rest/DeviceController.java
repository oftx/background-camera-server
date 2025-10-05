package github.oftx.backgroundcamera.controller.rest;

import github.oftx.backgroundcamera.dto.DeviceBindingResponseDto;
import github.oftx.backgroundcamera.dto.DeviceDto;
import github.oftx.backgroundcamera.dto.UpdateDeviceNameRequestDto;
import github.oftx.backgroundcamera.service.DeviceService;
import jakarta.validation.Valid;
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

    // 【新增】获取单个设备详情的API端点
    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDto> getDeviceById(
            @PathVariable String deviceId,
            Principal principal) {
        String username = principal.getName();
        DeviceDto deviceDetails = deviceService.getDeviceDetails(deviceId, username);
        return ResponseEntity.ok(deviceDetails);
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