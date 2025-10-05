package github.oftx.backgroundcamera.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 【新增】注入我们所有的自定义拦截器
    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final DeviceTokenChannelInterceptor deviceTokenChannelInterceptor;

    public WebSocketConfig(JwtChannelInterceptor jwtChannelInterceptor, DeviceTokenChannelInterceptor deviceTokenChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
        this.deviceTokenChannelInterceptor = deviceTokenChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    // 【新增】重写此方法以注册我们的自定义拦截器
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 注册我们的拦截器，并定义它们的执行顺序
        // 请求会先经过设备Token拦截器，再经过JWT拦截器
        registration.interceptors(deviceTokenChannelInterceptor, jwtChannelInterceptor);
    }
}