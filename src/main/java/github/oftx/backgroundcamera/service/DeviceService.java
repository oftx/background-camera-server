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
    private final WebSocketSessionManager webSocketSessionManager;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, WebSocketSessionManager webSocketSessionManager) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.webSocketSessionManager = webSocketSessionManager;
    }

    @Transactional
    public DeviceBindingResponseDto bindDeviceToUser(String deviceId, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseGet(() -> {
                    Device newDevice = new Device();
                    newDevice.setId(deviceId);
                    newDevice.setName("Device " + deviceId.substring(0, 6));
                    return newDevice;
                });

        if (device.getUser() != null && !device.getUser().getUsername().equals(username)) {
            throw new IllegalStateException("Device is already bound to another user.");
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

    @Transactional
    public void unbindDevice(String deviceId, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        if (device.getUser() == null || !device.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to unbind this device.");
        }

        device.setUser(null);
        device.setAuthTokenHash(null);
        deviceRepository.save(device);
    }

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

    @Transactional
    public DeviceDto updateDeviceName(String deviceId, String newName, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        if (device.getUser() == null || !device.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to modify this device.");
        }

        device.setName(newName);
        Device updatedDevice = deviceRepository.save(device);

        return new DeviceDto(
                updatedDevice.getId(),
                updatedDevice.getName(),
                webSocketSessionManager.isDeviceOnline(updatedDevice.getId())
        );
    }

    // 【新增】获取单个设备详情的方法
    @Transactional(readOnly = true)
    public DeviceDto getDeviceDetails(String deviceId, String username) {
        // 1. 查找设备
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        // 2. 权限校验：确保请求者是设备的所有者
        if (device.getUser() == null || !device.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to view this device's details.");
        }

        // 3. 将实体映射为DTO并返回
        return new DeviceDto(
                device.getId(),
                device.getName(),
                webSocketSessionManager.isDeviceOnline(device.getId())
        );
    }

    private String generateNewAuthToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}