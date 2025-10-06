package github.oftx.backgroundcamera.controller.websocket;

import github.oftx.backgroundcamera.dto.CommandPayloadDto;
// import github.oftx.backgroundcamera.dto.DeviceRegistrationDto; // 不再需要
import github.oftx.backgroundcamera.dto.DeviceStatusUpdateDto;
import github.oftx.backgroundcamera.repository.DeviceRepository;
// import github.oftx.backgroundcamera.service.WebSocketSessionManager; // 不再需要
// import jakarta.validation.Valid; // 不再需要
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.messaging.simp.stomp.StompHeaderAccessor; // 不再需要
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal;
// import java.time.Instant; // 不再需要

@Controller
public class DeviceControlSocketController {

    private static final Logger log = LoggerFactory.getLogger(DeviceControlSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;
    // 移除了不再需要的依赖
    // private final DeviceRepository deviceRepository;
    // private final WebSocketSessionManager sessionManager;

    // 更新构造函数
    public DeviceControlSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /*
     * 【重要】移除了 registerDevice 方法。
     * 设备的注册现在由 WebSocketConnectEventListener 自动处理，更加可靠。
     */
    // @MessageMapping("/device/register")
    // public void registerDevice(...) { ... }


    @MessageMapping("/device/command/{deviceId}")
    @PreAuthorize("@deviceSecurityService.canControlDevice(principal.username, #deviceId)")
    public void receiveCommand(@DestinationVariable String deviceId, @Payload CommandPayloadDto command, Principal principal) {
        // principal.getName() in this context is the username from JWT
        log.info("Command from authorized user '{}' received for device {}: {}", principal.getName(), deviceId, command);
        messagingTemplate.convertAndSend("/queue/device/command/" + deviceId, command);
    }

    @MessageMapping("/device/status")
    public void onDeviceStatusUpdate(@Payload DeviceStatusUpdateDto statusUpdate, Principal principal) {
        if (principal == null || !principal.getName().equals(statusUpdate.getDeviceId())) {
            log.warn("Unauthorized status update attempt. Authenticated principal '{}' tried to update status for device '{}'.",
                    (principal != null ? principal.getName() : "null"), statusUpdate.getDeviceId());
            return;
        }

        String deviceId = statusUpdate.getDeviceId();
        log.info("Status update from authenticated device {}: {}", deviceId, statusUpdate.getStatus());
        messagingTemplate.convertAndSend("/topic/device/status/" + deviceId, statusUpdate.getStatus());
    }
}