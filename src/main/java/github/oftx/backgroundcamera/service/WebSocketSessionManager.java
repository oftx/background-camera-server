package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.dto.DeviceOnlineStatusDto; // <-- 1. 导入新的 DTO
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- 2. 导入 SimpMessagingTemplate
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

    private final ConcurrentHashMap<String, String> deviceIdToSessionId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionIdToDeviceId = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate; // <-- 3. 添加成员变量

    // <-- 4. 修改构造函数，注入 SimpMessagingTemplate
    public WebSocketSessionManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void registerSession(String deviceId, String sessionId) {
        deviceIdToSessionId.put(deviceId, sessionId);
        sessionIdToDeviceId.put(sessionId, deviceId);
        log.info("WebSocket session registered: Device [{}] <=> Session [{}]", deviceId, sessionId);

        // <-- 5. 在设备注册后，广播其 "在线" 状态
        broadcastStatusUpdate(deviceId, true);
    }

    public void unregisterSession(String sessionId) {
        String deviceId = sessionIdToDeviceId.remove(sessionId);
        if (deviceId != null) {
            deviceIdToSessionId.remove(deviceId);
            log.info("WebSocket session unregistered: Device [{}] went offline.", deviceId);

            // <-- 6. 在设备注销后，广播其 "离线" 状态
            broadcastStatusUpdate(deviceId, false);
        }
    }

    public boolean isDeviceOnline(String deviceId) {
        return deviceIdToSessionId.containsKey(deviceId);
    }

    // <-- 7. 新增一个私有辅助方法来广播消息
    private void broadcastStatusUpdate(String deviceId, boolean isOnline) {
        String destination = "/topic/device/status/" + deviceId;
        DeviceOnlineStatusDto statusUpdate = new DeviceOnlineStatusDto(deviceId, isOnline);

        // 使用 messagingTemplate 发送消息到指定主题
        messagingTemplate.convertAndSend(destination, statusUpdate);

        log.info("Broadcast status update to {}: Device [{}] is now {}", destination, deviceId, isOnline ? "ONLINE" : "OFFLINE");
    }
}