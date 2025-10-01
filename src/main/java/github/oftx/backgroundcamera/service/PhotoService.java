package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.dto.PhotoDto;
import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.entity.Photo;
import github.oftx.backgroundcamera.exception.ResourceNotFoundException;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import github.oftx.backgroundcamera.repository.PhotoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.UUID;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final DeviceRepository deviceRepository;
    private final StorageService storageService;

    public PhotoService(PhotoRepository photoRepository, DeviceRepository deviceRepository, StorageService storageService) {
        this.photoRepository = photoRepository;
        this.deviceRepository = deviceRepository;
        this.storageService = storageService;
    }

    @Transactional
    public PhotoDto savePhoto(MultipartFile file, String deviceId, Instant timestamp, String originalFileName) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        String relativePath = storageService.storeFile(file, deviceId, originalFileName);

        Photo photo = new Photo();
        photo.setId(UUID.randomUUID().toString());
        photo.setDevice(device);
        photo.setCapturedAt(timestamp);
        photo.setFilePath(relativePath);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/media/")
                .path(relativePath)
                .toUriString();
        photo.setFileUrl(fileUrl);
        photo.setOriginalFileName(originalFileName);
        photo.setContentType(file.getContentType());
        photo.setFileSize(file.getSize());

        Photo savedPhoto = photoRepository.save(photo);

        return new PhotoDto(savedPhoto.getId(), deviceId, savedPhoto.getFileUrl(), savedPhoto.getCapturedAt());
    }

    @Transactional(readOnly = true)
    public Page<PhotoDto> findPhotosByDeviceId(String deviceId, Instant startDate, Instant endDate, Pageable pageable, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        if (device.getUser() == null || !device.getUser().getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to this device's photos.");
        }

        Page<Photo> photoPage = photoRepository.findByDeviceIdAndDateRange(deviceId, startDate, endDate, pageable);
        return photoPage.map(photo -> new PhotoDto(photo.getId(), deviceId, photo.getFileUrl(), photo.getCapturedAt()));
    }
}
