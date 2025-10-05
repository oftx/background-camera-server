package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.dto.DeviceBindingResponseDto;
import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.entity.User;
import github.oftx.backgroundcamera.exception.ResourceNotFoundException;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import github.oftx.backgroundcamera.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DeviceBindingResponseDto bindDeviceToUser(String deviceId, String username) {
        // 【修复】如果设备不存在，则创建一个新设备，而不是抛出异常
        Device device = deviceRepository.findById(deviceId)
                .orElseGet(() -> {
                    Device newDevice = new Device();
                    newDevice.setId(deviceId);
                    newDevice.setName("New Device (Bound via API)"); // 设置一个默认名称
                    // createdAt 由实体中的 @PrePersist 或默认值自动设置
                    return newDevice;
                });

        if (device.getUser() != null) {
            throw new IllegalStateException("Device is already bound to a user.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String authToken = generateNewAuthToken();
        String hashedToken = passwordEncoder.encode(authToken);

        device.setUser(user);
        device.setAuthTokenHash(hashedToken);
        deviceRepository.save(device);

        return new DeviceBindingResponseDto(deviceId, authToken);
    }

    private String generateNewAuthToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}