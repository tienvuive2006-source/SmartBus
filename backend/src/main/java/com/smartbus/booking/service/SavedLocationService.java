package com.smartbus.booking.service;

import com.smartbus.booking.entity.SavedLocation;
import com.smartbus.booking.repository.SavedLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedLocationService {

    private final SavedLocationRepository repository;

    public List<SavedLocation> findAll() {
        return repository.findAllByOrderByNameAsc();
    }

    public SavedLocation create(SavedLocation request) {
        validate(request);
        String name = request.getName().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên vị trí đã tồn tại.");
        }
        request.setId(null);
        request.setName(name);
        request.setAddress(clean(request.getAddress()));
        return repository.save(request);
    }

    public SavedLocation update(Long id, SavedLocation request) {
        validate(request);
        SavedLocation location = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vị trí."));
        String name = request.getName().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Tên vị trí đã tồn tại.");
        }
        location.setName(name);
        location.setAddress(clean(request.getAddress()));
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        return repository.save(location);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy vị trí.");
        }
        repository.deleteById(id);
    }

    private void validate(SavedLocation location) {
        if (location.getName() == null || location.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên vị trí.");
        }
        if (location.getLatitude() == null || location.getLatitude() < -90 || location.getLatitude() > 90) {
            throw new IllegalArgumentException("Vĩ độ phải nằm trong khoảng -90 đến 90.");
        }
        if (location.getLongitude() == null || location.getLongitude() < -180 || location.getLongitude() > 180) {
            throw new IllegalArgumentException("Kinh độ phải nằm trong khoảng -180 đến 180.");
        }
    }

    private String clean(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
