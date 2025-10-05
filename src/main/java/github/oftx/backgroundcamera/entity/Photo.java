package github.oftx.backgroundcamera.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "photos", indexes = {
        @Index(name = "idx_photos_device_id_captured_at", columnList = "device_id, capturedAt DESC"),
        // 【新增】为哈希值添加索引，以快速查找重复文件
        @Index(name = "idx_photos_sha256_hash", columnList = "sha256Hash")
})
public class Photo {
    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false, length = 1024)
    private String filePath;

    @Column(nullable = false, length = 1024)
    private String fileUrl;

    // 【新增】用于存储文件内容的SHA-256哈希值
    @Column(length = 64) // SHA-256 hash is 64 hex characters
    private String sha256Hash;

    private String originalFileName;
    private String contentType;
    private long fileSize;

    @Column(nullable = false)
    private Instant capturedAt;

    private Instant uploadedAt = Instant.now();

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Device getDevice() { return device; }
    public void setDevice(Device device) { this.device = device; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}