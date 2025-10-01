package github.oftx.backgroundcamera.controller.rest;

import github.oftx.backgroundcamera.dto.PhotoDto;
import github.oftx.backgroundcamera.service.PhotoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// MODIFIED: Import for security
import org.springframework.security.access.AccessDeniedException;
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

    // MODIFIED: Method signature updated to include Principal for authorization
    @PostMapping("/upload")
    public ResponseEntity<PhotoDto> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deviceId") String deviceId,
            @RequestParam("timestamp") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant timestamp,
            Principal principal) {

        // MODIFIED: CRITICAL authorization check
        // The principal.getName() is the deviceId, authenticated by DeviceTokenAuthFilter
        if (principal == null || !principal.getName().equals(deviceId)) {
            throw new AccessDeniedException("The authenticated device is not authorized to upload photos for deviceId: " + deviceId);
        }

        String originalFileName = Objects.requireNonNullElse(file.getOriginalFilename(), "upload-" + Instant.now().toEpochMilli());
        PhotoDto savedPhoto = photoService.savePhoto(file, deviceId, timestamp, originalFileName);
        return new ResponseEntity<>(savedPhoto, HttpStatus.CREATED);
    }

    // ... (rest of the file is unchanged) ...
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