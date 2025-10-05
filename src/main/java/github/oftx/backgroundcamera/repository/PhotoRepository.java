package github.oftx.backgroundcamera.repository;

import github.oftx.backgroundcamera.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {

    // 【新增】根据SHA-256哈希值查找第一张匹配的照片，用于文件去重
    Optional<Photo> findFirstBySha256Hash(String sha256Hash);

    Page<Photo> findByDevice_Id(String deviceId, Pageable pageable);

    @Query("SELECT p FROM Photo p WHERE p.device.id = :deviceId " +
            "AND (cast(:startDate as org.hibernate.type.InstantType) IS NULL OR p.capturedAt >= :startDate) " +
            "AND (cast(:endDate as org.hibernate.type.InstantType) IS NULL OR p.capturedAt <= :endDate)")
    Page<Photo> findByDeviceIdAndDateRange(@Param("deviceId") String deviceId,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate,
                                           Pageable pageable);
}