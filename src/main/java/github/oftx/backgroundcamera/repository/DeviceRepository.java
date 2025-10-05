package github.oftx.backgroundcamera.repository;

import github.oftx.backgroundcamera.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    // 【新增】根据用户名查找该用户拥有的所有设备
    List<Device> findByUser_Username(String username);
}