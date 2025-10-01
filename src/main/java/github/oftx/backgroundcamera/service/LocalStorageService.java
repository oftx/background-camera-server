package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate; // 【修复】确保导入
import java.time.Year;

@Service
public class LocalStorageService implements StorageService {

    private final Path storageBasePath;

    public LocalStorageService(@Value("${storage.base-path}") String basePath) {
        this.storageBasePath = Paths.get(basePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageBasePath);
        } catch (IOException e) {
            throw new StorageException("Could not create storage directory: " + this.storageBasePath, e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String deviceId, String originalFileName) {
        String fileName = StringUtils.cleanPath(originalFileName);

        if (fileName.contains("..")) {
            throw new StorageException("Filename contains invalid path sequence " + fileName);
        }

        try {
            String year = String.valueOf(Year.now().getValue());
            // 【修复】使用 LocalDate.now().getMonthValue() 获取月份数字
            String month = String.format("%02d", LocalDate.now().getMonthValue());

            Path deviceDir = this.storageBasePath.resolve(deviceId);
            Path yearDir = deviceDir.resolve(year);
            Path monthDir = yearDir.resolve(month);
            Files.createDirectories(monthDir);

            Path targetLocation = monthDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Path relativePath = Paths.get(deviceId, year, month, fileName);
            return relativePath.toString().replace("\\", "/");

        } catch (IOException ex) {
            throw new StorageException("Failed to store file " + fileName, ex);
        }
    }
}