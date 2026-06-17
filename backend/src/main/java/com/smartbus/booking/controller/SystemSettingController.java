package com.smartbus.booking.controller;

import com.smartbus.booking.entity.SystemSetting;
import com.smartbus.booking.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/settings")
@CrossOrigin(origins = "*")
public class SystemSettingController {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @GetMapping("/{key}")
    public ResponseEntity<?> getSetting(@PathVariable("key") String key) {
        Optional<SystemSetting> setting = systemSettingRepository.findById(key);
        if (setting.isPresent()) {
            return ResponseEntity.ok(Map.of("value", setting.get().getValue()));
        } else {
            return ResponseEntity.ok(Map.of("value", ""));
        }
    }

    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_SYSTEM_SETTING", entityName = "SystemSetting")
    @PutMapping("/{key}")
    public ResponseEntity<?> updateSetting(@PathVariable("key") String key, @RequestBody Map<String, String> payload) {
        String value = payload.get("value");
        SystemSetting setting = systemSettingRepository.findById(key).orElse(new SystemSetting(key, ""));
        setting.setValue(value);
        systemSettingRepository.save(setting);
        return ResponseEntity.ok(Map.of("message", "Cập nhật cài đặt thành công", "value", value));
    }
}
