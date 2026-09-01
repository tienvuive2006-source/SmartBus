package com.smartbus.booking.service;

import com.smartbus.booking.dto.VehicleMaintenanceCompleteRequest;
import com.smartbus.booking.dto.VehicleMaintenanceRequest;
import com.smartbus.booking.entity.Bus;
import com.smartbus.booking.entity.VehicleMaintenance;
import com.smartbus.booking.repository.BusRepository;
import com.smartbus.booking.repository.TripRepository;
import com.smartbus.booking.repository.VehicleMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleMaintenanceService {
    private static final List<String> ACTIVE_STATUSES = List.of("SCHEDULED", "IN_PROGRESS");
    private final VehicleMaintenanceRepository maintenanceRepository;
    private final BusRepository busRepository;
    private final TripRepository tripRepository;
    private final VehicleMileageService mileageService;
    private final FundService fundService;

    public List<VehicleMaintenance> getAll() {
        return maintenanceRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public VehicleMaintenance create(VehicleMaintenanceRequest request) {
        if (request.busId() == null || request.maintenanceType() == null || request.maintenanceType().isBlank() || request.scheduledStart() == null)
            throw new IllegalArgumentException("Vui lòng chọn xe, loại bảo trì và thời gian bắt đầu.");
        Bus bus = getBus(request.busId());
        if (maintenanceRepository.existsByBusIdAndStatusIn(bus.getId(), ACTIVE_STATUSES))
            throw new IllegalArgumentException("Xe đang có một phiếu bảo trì chưa hoàn tất.");
        if (request.expectedEnd() != null && request.expectedEnd().isBefore(request.scheduledStart()))
            throw new IllegalArgumentException("Thời gian hoàn thành dự kiến phải sau thời gian bắt đầu.");
        if (request.maintenanceIntervalKm() != null && request.maintenanceIntervalKm() > 0) {
            bus.setMaintenanceIntervalKm(request.maintenanceIntervalKm());
            busRepository.save(bus);
        }
        return maintenanceRepository.save(VehicleMaintenance.builder()
                .bus(bus).maintenanceType(request.maintenanceType().trim()).description(request.description())
                .scheduledStart(request.scheduledStart()).expectedEnd(request.expectedEnd())
                .estimatedCost(request.estimatedCost()).garageName(request.garageName()).notes(request.notes()).build());
    }

    @Transactional
    public VehicleMaintenance start(Long id) {
        VehicleMaintenance maintenance = getMaintenance(id);
        if (!"SCHEDULED".equals(maintenance.getStatus())) throw new IllegalArgumentException("Phiếu không ở trạng thái đã lên lịch.");
        boolean running = tripRepository.existsByAssignedLicensePlateIgnoreCaseAndStatusIgnoreCase(
                maintenance.getBus().getLicensePlate(), "IN_PROGRESS");
        if (running) throw new IllegalArgumentException("Xe đang chạy chuyến. Hãy hoàn thành hoặc đổi xe trước khi bảo trì.");
        maintenance.setStatus("IN_PROGRESS");
        maintenance.getBus().setStatus("BẢO TRÌ");
        busRepository.save(maintenance.getBus());
        return maintenanceRepository.save(maintenance);
    }

    @Transactional
    public VehicleMaintenance complete(Long id, VehicleMaintenanceCompleteRequest request, String performedBy) {
        VehicleMaintenance maintenance = getMaintenance(id);
        if (!"IN_PROGRESS".equals(maintenance.getStatus())) throw new IllegalArgumentException("Phiếu chưa được bắt đầu bảo trì.");
        double actualCost = request.actualCost() == null ? 0 : request.actualCost();
        if (actualCost < 0) throw new IllegalArgumentException("Chi phí thực tế không được nhỏ hơn 0.");
        String fundType = normalizeFundType(request.fundType(), actualCost);
        Bus bus = mileageService.resetAfterMaintenance(maintenance.getBus(), request.currentMileage(), request.notes());
        bus.setStatus("ĐANG NGHỈ");
        busRepository.save(bus);
        maintenance.setStatus("COMPLETED");
        maintenance.setCompletedAt(LocalDateTime.now());
        maintenance.setOdometerAtService(bus.getCurrentMileage());
        maintenance.setActualCost(actualCost);
        if (request.notes() != null && !request.notes().isBlank()) maintenance.setNotes(request.notes().trim());
        VehicleMaintenance saved = maintenanceRepository.save(maintenance);
        if (actualCost > 0) {
            fundService.recordTransactionIfAbsent(
                    fundType,
                    "EXPENSE",
                    actualCost,
                    "Chi phí bảo trì xe " + bus.getLicensePlate() + " - " + maintenance.getMaintenanceType(),
                    "MAINTENANCE-" + saved.getId(),
                    performedBy == null || performedBy.isBlank() ? "SYSTEM" : performedBy);
        }
        return saved;
    }

    @Transactional
    public VehicleMaintenance cancel(Long id) {
        VehicleMaintenance maintenance = getMaintenance(id);
        if ("COMPLETED".equals(maintenance.getStatus())) throw new IllegalArgumentException("Không thể hủy phiếu đã hoàn thành.");
        if ("IN_PROGRESS".equals(maintenance.getStatus())) {
            maintenance.getBus().setStatus("ĐANG NGHỈ");
            busRepository.save(maintenance.getBus());
        }
        maintenance.setStatus("CANCELLED");
        return maintenanceRepository.save(maintenance);
    }

    private Bus getBus(Long id) {
        return busRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe."));
    }
    private VehicleMaintenance getMaintenance(Long id) {
        return maintenanceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu bảo trì."));
    }

    private String normalizeFundType(String rawFundType, double actualCost) {
        if (actualCost <= 0) return "CASH";
        if (rawFundType == null || rawFundType.isBlank())
            throw new IllegalArgumentException("Vui lòng chọn quỹ chi bảo trì.");
        String fundType = rawFundType.trim().toUpperCase();
        if (!List.of("CASH", "BANK_TRANSFER", "WALLET").contains(fundType))
            throw new IllegalArgumentException("Quỹ chi bảo trì không hợp lệ.");
        return fundType;
    }
}
