package github.oftx.backgroundcamera.repository;

import github.oftx.backgroundcamera.entity.Photo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, String> {

    Page<Photo> findByDevice_Id(String deviceId, Pageable pageable);

    // 【修复】修改了JPQL查询以避免PostgreSQL的 "could not determine data type" 错误
    // 当 startDate 或 endDate 为 null 时，这种写法能让JPA提供程序正确处理。
    @Query("SELECT p FROM Photo p WHERE p.device.id = :deviceId " +
            "AND (cast(:startDate as org.hibernate.type.InstantType) IS NULL OR p.capturedAt >= :startDate) " +
            "AND (cast(:endDate as org.hibernate.type.InstantType) IS NULL OR p.capturedAt <= :endDate)")
    Page<Photo> findByDeviceIdAndDateRange(@Param("deviceId") String deviceId,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate,
                                           Pageable pageable);
}