package com.smartbus.booking.service;

import com.smartbus.booking.entity.Bus;
import com.smartbus.booking.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    @Transactional
    public Bus saveBus(Bus bus) {
        // Chuyển trạng thái chữ hoa thống nhất
        if (bus.getStatus() != null) {
            bus.setStatus(bus.getStatus().toUpperCase());
        } else {
            bus.setStatus("ĐANG NGHỈ");
        }
        return busRepository.save(bus);
    }

    @Transactional
    public Bus updateBus(Long id, Bus updatedBus) {
        return busRepository.findById(id).map(bus -> {
            bus.setLicensePlate(updatedBus.getLicensePlate());
            bus.setBusType(updatedBus.getBusType());
            bus.setDriverName(updatedBus.getDriverName());
            bus.setStatus(updatedBus.getStatus() != null ? updatedBus.getStatus().toUpperCase() : "ĐANG NGHỈ");
            bus.setCurrentStation(updatedBus.getCurrentStation());
            bus.setImageUrl(updatedBus.getImageUrl()); // Lưu link ảnh xe thật
            return busRepository.save(bus);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy xe với mã ID: " + id));
    }

    @Transactional
    public void deleteBus(Long id) {
        busRepository.deleteById(id);
    }
}
