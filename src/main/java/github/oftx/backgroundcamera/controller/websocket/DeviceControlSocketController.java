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
import org.springframework.stereotype.Controller;

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
            newDevice.setName("New Device");
            newDevice.setCreatedAt(Instant.now());
            // Note: User assignment would happen through a separate binding process
            deviceRepository.save(newDevice);
            log.info("New device created and saved: {}", deviceId);
        } else {
            log.info("Existing device connected: {}", deviceId);
        }
    }

    @MessageMapping("/device/command/{deviceId}")
    public void receiveCommand(@DestinationVariable String deviceId, @Payload CommandPayloadDto command) {
        log.info("Command received for device {}: {}", deviceId, command);
        // TODO: Add authorization check to ensure the principal can control this deviceId
        messagingTemplate.convertAndSend("/queue/device/command/" + deviceId, command);
    }

    @MessageMapping("/device/status")
    public void onDeviceStatusUpdate(@Payload DeviceStatusUpdateDto statusUpdate) {
        String deviceId = statusUpdate.getDeviceId();
        // TODO: Add authorization check to ensure the source of this message is the actual device
        log.info("Status update from device {}: {}", deviceId, statusUpdate.getStatus());
        messagingTemplate.convertAndSend("/topic/device/status/" + deviceId, statusUpdate.getStatus());
    }
}
