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

    @Query("SELECT p FROM Photo p WHERE p.device.id = :deviceId " +
           "AND (:startDate IS NULL OR p.capturedAt >= :startDate) " +
           "AND (:endDate IS NULL OR p.capturedAt <= :endDate)")
    Page<Photo> findByDeviceIdAndDateRange(@Param("deviceId") String deviceId,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate,
                                           Pageable pageable);
}
