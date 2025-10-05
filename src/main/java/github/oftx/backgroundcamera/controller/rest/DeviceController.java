package github.oftx.backgroundcamera.controller.rest;

import github.oftx.backgroundcamera.dto.DeviceBindingResponseDto;
import github.oftx.backgroundcamera.dto.DeviceDto;
import github.oftx.backgroundcamera.service.DeviceService;
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

    // 【新增】获取当前用户绑定的所有设备列表
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

    // 【新增】解绑指定设备
    @PostMapping("/{deviceId}/unbind")
    public ResponseEntity<Void> unbindDevice(
            @PathVariable String deviceId,
            Principal principal) {
        String username = principal.getName();
        deviceService.unbindDevice(deviceId, username);
        // HTTP 204 No Content 是表示成功执行无返回内容的操作的理想状态码
        return ResponseEntity.noContent().build();
    }
}