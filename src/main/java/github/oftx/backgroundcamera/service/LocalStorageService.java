package github.oftx.backgroundcamera.service;

import github.oftx.backgroundcamera.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

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
        // 【修改】文件名不再使用原始文件名，而是生成一个唯一的UUID来防止冲突
        String extension = StringUtils.getFilenameExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID() + (extension != null ? "." + extension : "");

        if (uniqueFileName.contains("..")) {
            throw new StorageException("Filename contains invalid path sequence " + uniqueFileName);
        }

        try (InputStream inputStream = file.getInputStream()) {
            String year = String.valueOf(Year.now().getValue());
            String month = String.format("%02d", LocalDate.now().getMonthValue());

            Path deviceDir = this.storageBasePath.resolve(deviceId);
            Path yearDir = deviceDir.resolve(year);
            Path monthDir = yearDir.resolve(month);
            Files.createDirectories(monthDir);

            Path targetLocation = monthDir.resolve(uniqueFileName);
            // 这里不再需要 REPLACE_EXISTING，因为文件名总是唯一的，但保留也无妨
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Path relativePath = Paths.get(deviceId, year, month, uniqueFileName);
            return relativePath.toString().replace("\\", "/");

        } catch (IOException ex) {
            throw new StorageException("Failed to store file " + uniqueFileName, ex);
        }
    }
}