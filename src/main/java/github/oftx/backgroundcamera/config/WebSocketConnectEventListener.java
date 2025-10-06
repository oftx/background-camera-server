package github.oftx.backgroundcamera.config;

import github.oftx.backgroundcamera.service.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.security.Principal;

@Component
public class WebSocketConnectEventListener implements ApplicationListener<SessionConnectEvent> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConnectEventListener.class);
    private final WebSocketSessionManager sessionManager;

    public WebSocketConnectEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onApplicationEvent(@NonNull SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        // 我们的 DeviceTokenChannelInterceptor 会为被控端设备设置一个 Principal，
        // 其 getName() 方法返回的就是 deviceId。
        if (user != null) {
            String principalName = user.getName();
            String sessionId = accessor.getSessionId();

            // 我们只关心被控端设备的连接，而不是控制端用户的连接。
            // 可以在这里添加更严格的检查，例如检查 Principal 的类型或权限。
            // 简单起见，我们假设非用户名的 Principal 就是 deviceId。
            // 在我们的认证逻辑中，被控端的 Principal 是 deviceId(String)，
            // 控制端的 Principal 是 UserDetails。
            if (principalName != null && sessionId != null) {
                // 判断是否是设备（可以根据权限判断，这里简化处理）
                boolean isDevice = event.getMessage().getHeaders().get("simpUser", UsernamePasswordAuthenticationToken.class)
                        .getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_DEVICE"));

                if (isDevice) {
                    log.info("Device connected with principal name (deviceId): {}", principalName);
                    // 直接在连接事件中注册会话，这将自动触发上线广播
                    sessionManager.registerSession(principalName, sessionId);
                }
            }
        }
    }
}