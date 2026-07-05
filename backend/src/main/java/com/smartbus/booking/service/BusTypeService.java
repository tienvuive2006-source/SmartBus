package com.smartbus.booking.service;

import com.smartbus.booking.entity.BusType;
import com.smartbus.booking.repository.BusTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusTypeService {

    private final BusTypeRepository busTypeRepository;

    public List<BusType> getAllBusTypes() {
        return busTypeRepository.findAll();
    }

    @Transactional
    public BusType saveBusType(BusType busType) {
        if (busType.getSeatCount() == null) {
            busType.setSeatCount(24); // Mặc định nếu không điền
        }
        if (busType.getPriceMultiplier() == null) {
            busType.setPriceMultiplier(1.0);
        }
        return busTypeRepository.save(busType);
    }

    @Transactional
    public BusType updateBusType(Long id, BusType updated) {
        return busTypeRepository.findById(id).map(type -> {
            type.setName(updated.getName());
            type.setSeatCount(updated.getSeatCount() != null ? updated.getSeatCount() : 24);
            type.setPriceMultiplier(updated.getPriceMultiplier() != null ? updated.getPriceMultiplier() : 1.0);
            type.setDescription(updated.getDescription());
            type.setImageUrl(updated.getImageUrl()); // Bổ sung lưu hình ảnh
            return busTypeRepository.save(type);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy dòng xe mã: " + id));
    }

    @Transactional
    public void deleteBusType(Long id) {
        busTypeRepository.deleteById(id);
    }
}
