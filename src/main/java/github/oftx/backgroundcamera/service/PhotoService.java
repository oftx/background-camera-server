package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.dto.PhotoDto;
import github.oftx.backgroundcamera.entity.Device;
import github.oftx.backgroundcamera.entity.Photo;
import github.oftx.backgroundcamera.exception.ResourceNotFoundException;
import github.oftx.backgroundcamera.exception.StorageException;
import github.oftx.backgroundcamera.repository.DeviceRepository;
import github.oftx.backgroundcamera.repository.PhotoRepository;
import org.slf4j.Logger; // <-- 1. 导入 Logger
import org.slf4j.LoggerFactory; // <-- 1. 导入 LoggerFactory
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- 2. 导入 SimpMessagingTemplate
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

    private static final Logger log = LoggerFactory.getLogger(PhotoService.class); // <-- 1. 添加 Logger

    private final PhotoRepository photoRepository;
    private final DeviceRepository deviceRepository;
    private final StorageService storageService;
    private final SimpMessagingTemplate messagingTemplate; // <-- 3. 添加成员变量

    // <-- 4. 修改构造函数以注入 SimpMessagingTemplate
    public PhotoService(PhotoRepository photoRepository, DeviceRepository deviceRepository, StorageService storageService, SimpMessagingTemplate messagingTemplate) {
        this.photoRepository = photoRepository;
        this.deviceRepository = deviceRepository;
        this.storageService = storageService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public PhotoDto savePhoto(MultipartFile file, String deviceId, Instant timestamp, String originalFileName) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + deviceId));

        String sha256Hash;
        try {
            sha256Hash = calculateSha256(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new StorageException("Failed to calculate file hash", e);
        }

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
            Photo existingPhoto = existingPhotoOpt.get();
            photoToSave.setFilePath(existingPhoto.getFilePath());
            photoToSave.setFileUrl(existingPhoto.getFileUrl());
        } else {
            String relativePath = storageService.storeFile(file, deviceId, originalFileName);
            photoToSave.setFilePath(relativePath);
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/media/")
                    .path(relativePath)
                    .toUriString();
            photoToSave.setFileUrl(fileUrl);
        }

        Photo savedPhoto = photoRepository.save(photoToSave);

        PhotoDto photoDto = new PhotoDto(savedPhoto.getId(), deviceId, savedPhoto.getFileUrl(), savedPhoto.getCapturedAt());

        // --- FIX START: 广播新照片通知 ---
        String destination = "/topic/photos/new/" + deviceId;
        messagingTemplate.convertAndSend(destination, photoDto);
        log.info("Broadcast new photo notification to {}: photoId={}", destination, photoDto.getPhotoId());
        // --- FIX END ---

        return photoDto;
    }

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