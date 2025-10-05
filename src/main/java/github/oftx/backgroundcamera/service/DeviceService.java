package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.dto.DeviceBindingResponseDto;
import github.oftx.backgroundcamera.dto.DeviceDto;
import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.entity.User;
import github.oftx.backgroundcamera.exception.ResourceNotFoundException;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import github.oftx.backgroundcamera.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebSocketSessionManager webSocketSessionManager; // 【新增】

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, WebSocketSessionManager webSocketSessionManager) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.webSocketSessionManager = webSocketSessionManager; // 【新增】
    }

    @Transactional
    public DeviceBindingResponseDto bindDeviceToUser(String deviceId, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseGet(() -> {
                    Device newDevice = new Device();
                    newDevice.setId(deviceId);
                    newDevice.setName("Device " + deviceId.substring(0, 6)); // 设置一个更具识别性的默认名称
                    return newDevice;
                });

        // 【修改】实现幂等绑定逻辑
        // 检查设备是否已绑定到 *另一个* 用户
        if (device.getUser() != null && !device.getUser().getUsername().equals(username)) {
            throw new IllegalStateException("Device is already bound to another user.");
        }

        // 如果设备未绑定，或已绑定到当前用户，都继续执行绑定/重新绑定流程
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String authToken = generateNewAuthToken();
        String hashedToken = passwordEncoder.encode(authToken);

        device.setUser(user);
        device.setAuthTokenHash(hashedToken);
        deviceRepository.save(device);

        return new DeviceBindingResponseDto(deviceId, authToken);
    }

    // 【新增】解绑设备
    @Transactional
    public void unbindDevice(String deviceId, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        // 安全检查：确保只有设备所有者才能解绑
        if (device.getUser() == null || !device.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to unbind this device.");
        }

        device.setUser(null);
        device.setAuthTokenHash(null);
        deviceRepository.save(device);
    }

    // 【新增】获取用户的所有设备列表
    @Transactional(readOnly = true)
    public List<DeviceDto> findDevicesByUsername(String username) {
        List<Device> devices = deviceRepository.findByUser_Username(username);
        return devices.stream()
                .map(device -> new DeviceDto(
                        device.getId(),
                        device.getName(),
                        webSocketSessionManager.isDeviceOnline(device.getId())
                ))
                .collect(Collectors.toList());
    }

    private String generateNewAuthToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}