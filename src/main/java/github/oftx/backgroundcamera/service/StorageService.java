package github.oftx.backgroundcamera.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String storeFile(MultipartFile file, String deviceId, String originalFileName);
}
