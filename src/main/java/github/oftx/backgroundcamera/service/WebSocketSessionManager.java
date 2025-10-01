package github.oftx.backgroundcamera.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private static final Logger log = LoggerFactory.getLogger(WebSocketSessionManager.class);

    private final ConcurrentHashMap<String, String> deviceIdToSessionId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionIdToDeviceId = new ConcurrentHashMap<>();

    public void registerSession(String deviceId, String sessionId) {
        deviceIdToSessionId.put(deviceId, sessionId);
        sessionIdToDeviceId.put(sessionId, deviceId);
        log.info("WebSocket session registered: Device [{}] <=> Session [{}]", deviceId, sessionId);
    }

    public void unregisterSession(String sessionId) {
        String deviceId = sessionIdToDeviceId.remove(sessionId);
        if (deviceId != null) {
            deviceIdToSessionId.remove(deviceId);
            log.info("WebSocket session unregistered: Device [{}] went offline.", deviceId);
        }
    }
    
    public boolean isDeviceOnline(String deviceId) {
        return deviceIdToSessionId.containsKey(deviceId);
    }
}
