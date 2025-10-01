package github.oftx.backgroundcamera.controller.rest;

import github.oftx.backgroundcamera.dto.PhotoDto;
import github.oftx.backgroundcamera.service.PhotoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<PhotoDto> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deviceId") String deviceId,
            @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp) {
        
        // TODO: This endpoint should also be secured, probably via a device-specific API key/token
        // For now, it's left as-is from the original implementation for simplicity.
        String originalFileName = Objects.requireNonNullElse(file.getOriginalFilename(), "upload-" + Instant.now().toEpochMilli());
        PhotoDto savedPhoto = photoService.savePhoto(file, deviceId, timestamp, originalFileName);
        return new ResponseEntity<>(savedPhoto, HttpStatus.CREATED);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Page<PhotoDto>> getPhotosByDevice(
            @PathVariable String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @PageableDefault(size = 20, sort = "capturedAt") Pageable pageable,
            Principal principal) {
        
        Page<PhotoDto> photos = photoService.findPhotosByDeviceId(deviceId, startDate, endDate, pageable, principal.getName());
        return ResponseEntity.ok(photos);
    }
}
