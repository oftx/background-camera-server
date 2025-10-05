package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.dto.PhotoDto;
import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.entity.Photo;
import github.oftx.backgroundcamera.exception.ResourceNotFoundException;
import github.oftx.backgroundcamera.exception.StorageException;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import github.oftx.backgroundcamera.repository.PhotoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
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

        // 【新增】1. 计算文件的SHA-256哈希值
        String sha256Hash;
        try {
            sha256Hash = calculateSha256(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new StorageException("Failed to calculate file hash", e);
        }

        // 【新增】2. 检查该哈希值是否已存在于数据库 (文件去重)
        Optional<Photo> existingPhotoOpt = photoRepository.findFirstBySha256Hash(sha256Hash);

        Photo photoToSave = new Photo();
        photoToSave.setId(UUID.randomUUID().toString());
        photoToSave.setDevice(device);
        photoToSave.setCapturedAt(timestamp);
        photoToSave.setOriginalFileName(originalFileName);
        photoToSave.setContentType(file.getContentType());
        photoToSave.setFileSize(file.getSize());
        photoToSave.setSha256Hash(sha256Hash);

        if (existingPhotoOpt.isPresent()) {
            // 【新增】3a. 如果文件已存在，则复用现有文件的路径和URL
            Photo existingPhoto = existingPhotoOpt.get();
            photoToSave.setFilePath(existingPhoto.getFilePath());
            photoToSave.setFileUrl(existingPhoto.getFileUrl());
        } else {
            // 【新增】3b. 如果是新文件，则存储它并生成新的路径和URL
            String relativePath = storageService.storeFile(file, deviceId, originalFileName);
            photoToSave.setFilePath(relativePath);
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/media/")
                    .path(relativePath)
                    .toUriString();
            photoToSave.setFileUrl(fileUrl);
        }

        // 4. 保存代表本次上传事件的Photo记录到数据库
        Photo savedPhoto = photoRepository.save(photoToSave);

        return new PhotoDto(savedPhoto.getId(), deviceId, savedPhoto.getFileUrl(), savedPhoto.getCapturedAt());
    }

    // 【新增】用于计算文件SHA-256哈希值的辅助方法
    private String calculateSha256(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
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