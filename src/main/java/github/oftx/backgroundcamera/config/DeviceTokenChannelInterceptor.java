package github.oftx.backgroundcamera.config;

import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class DeviceTokenChannelInterceptor implements ChannelInterceptor {

    private final DeviceRepository deviceRepository;
    private final PasswordEncoder passwordEncoder;

    public DeviceTokenChannelInterceptor(DeviceRepository deviceRepository, PasswordEncoder passwordEncoder) {
        this.deviceRepository = deviceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 尝试从header获取设备ID和设备Token
            String deviceId = accessor.getFirstNativeHeader("X-Device-Id");
            String deviceToken = accessor.getFirstNativeHeader("X-Device-Token");

            if (deviceId != null && deviceToken != null) {
                Optional<Device> deviceOpt = deviceRepository.findById(deviceId);

                // 验证Token是否有效
                if (deviceOpt.isPresent() && deviceOpt.get().getAuthTokenHash() != null) {
                    Device device = deviceOpt.get();
                    if (passwordEncoder.matches(deviceToken, device.getAuthTokenHash())) {
                        // 认证成功，为该WebSocket会话创建一个Principal
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                device.getId(), // Principal.getName() 将会是 deviceId
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_DEVICE"))
                        );
                        accessor.setUser(authentication);
                    }
                }
            }
        }
        return message;
    }
}