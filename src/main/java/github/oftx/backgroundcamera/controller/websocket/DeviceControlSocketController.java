package github.oftx.backgroundcamera.controller.websocket;

import github.oftx.backgroundcamera.dto.CommandPayloadDto;
import github.oftx.backgroundcamera.dto.DeviceRegistrationDto;
import github.oftx.backgroundcamera.dto.DeviceStatusUpdateDto;
import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import github.oftx.backgroundcamera.service.WebSocketSessionManager;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal; // 【新增】导入
import java.time.Instant;

@Controller
public class DeviceControlSocketController {

    private static final Logger log = LoggerFactory.getLogger(DeviceControlSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final DeviceRepository deviceRepository;
    private final WebSocketSessionManager sessionManager;

    public DeviceControlSocketController(SimpMessagingTemplate messagingTemplate, DeviceRepository deviceRepository, WebSocketSessionManager sessionManager) {
        this.messagingTemplate = messagingTemplate;
        this.deviceRepository = deviceRepository;
        this.sessionManager = sessionManager;
    }

    @MessageMapping("/device/register")
    public void registerDevice(@Payload @Valid DeviceRegistrationDto payload, StompHeaderAccessor headerAccessor) {
        String deviceId = payload.getDeviceId();
        String sessionId = headerAccessor.getSessionId();
        log.info("Device trying to register: {}", deviceId);

        sessionManager.registerSession(deviceId, sessionId);

        if (!deviceRepository.existsById(deviceId)) {
            Device newDevice = new Device();
            newDevice.setId(deviceId);
            // 使用之前定义的默认名称
            // newDevice.setName("New Device");
            newDevice.setCreatedAt(Instant.now());
            deviceRepository.save(newDevice);
            log.info("New device created and saved: {}", deviceId);
        } else {
            log.info("Existing device connected: {}", deviceId);
        }
    }

    @MessageMapping("/device/command/{deviceId}")
    @PreAuthorize("@deviceSecurityService.canControlDevice(principal.username, #deviceId)")
    public void receiveCommand(@DestinationVariable String deviceId, @Payload CommandPayloadDto command) {
        log.info("Command from authorized user '{}' received for device {}: {}", "principal.username", deviceId, command);
        messagingTemplate.convertAndSend("/queue/device/command/" + deviceId, command);
    }

    // 【修改】移除了@PreAuthorize注解，并添加了Principal参数和手动的安全检查
    @MessageMapping("/device/status")
    public void onDeviceStatusUpdate(@Payload DeviceStatusUpdateDto statusUpdate, Principal principal) {
        // Principal 由我们的 DeviceTokenChannelInterceptor 设置，principal.getName() 返回 deviceId
        if (principal == null || !principal.getName().equals(statusUpdate.getDeviceId())) {
            log.warn("Unauthorized status update attempt. Authenticated principal '{}' tried to update status for device '{}'.",
                    (principal != null ? principal.getName() : "null"), statusUpdate.getDeviceId());
            return; // 拒绝处理
        }

        // 安全检查通过后，继续执行业务逻辑
        String deviceId = statusUpdate.getDeviceId();
        log.info("Status update from authenticated device {}: {}", deviceId, statusUpdate.getStatus());
        messagingTemplate.convertAndSend("/topic/device/status/" + deviceId, statusUpdate.getStatus());
    }
}