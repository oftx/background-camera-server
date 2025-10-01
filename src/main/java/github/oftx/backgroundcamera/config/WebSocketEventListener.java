package github.oftx.backgroundcamera.config;

import github.oftx.backgroundcamera.service.WebSocketSessionManager;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener implements ApplicationListener<SessionDisconnectEvent> {

    private final WebSocketSessionManager sessionManager;

    public WebSocketEventListener(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onApplicationEvent(@NonNull SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        sessionManager.unregisterSession(sessionId);
    }
}
