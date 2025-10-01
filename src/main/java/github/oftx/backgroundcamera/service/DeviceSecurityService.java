package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("deviceSecurityService")
public class DeviceSecurityService {

    private final DeviceRepository deviceRepository;

    public DeviceSecurityService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public boolean canControlDevice(String username, String deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            return false;
        }
        Device device = deviceOpt.get();
        return device.getUser() != null && device.getUser().getUsername().equals(username);
    }
}