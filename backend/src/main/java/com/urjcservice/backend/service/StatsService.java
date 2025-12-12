package com.urjcservice.backend.service;

import com.urjcservice.backend.dtos.DashboardStatsDTO;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.ReservationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final RoomRepository roomRepo;
    private final ReservationRepository resRepo;

    public StatsService(RoomRepository roomRepo, ReservationRepository resRepo) {
        this.roomRepo = roomRepo;
        this.resRepo = resRepo;
    }

    public DashboardStatsDTO getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        long totalRooms = roomRepo.countTotalRooms();
        stats.setTotalRooms(totalRooms);

        if (totalRooms > 0) {
            // 1. Ocupación
            long occupied = resRepo.countOccupiedRoomsToday();
            double occPct = ((double) occupied / totalRooms) * 100;
            stats.setOccupiedPercentage(Math.round(occPct * 100.0) / 100.0);
            stats.setFreePercentage(Math.round((100 - occPct) * 100.0) / 100.0);

            // 3. Software
            long withSoft = roomRepo.countRoomsWithSoftware();
            double softPct = ((double) withSoft / totalRooms) * 100;
            stats.setRoomsWithSoftwarePercentage(Math.round(softPct * 100.0) / 100.0);
            stats.setRoomsWithoutSoftwarePercentage(Math.round((100 - softPct) * 100.0) / 100.0);
        }

        List<Date> dates = resRepo.findStartDatesToday();

        // Definimos la zona horaria de España
        ZoneId madridZone = ZoneId.of("Europe/Madrid");

        Map<Integer, Long> hourlyCounts = dates.stream()
            .map(date -> {
                // Convertimos UTC -> Madrid y sacamos la hora
                return date.toInstant().atZone(madridZone).getHour();
            })
            .collect(Collectors.groupingBy(h -> h, Collectors.counting()));

        // Usamos el nombre correcto del setter en el DTO
        stats.setHourlyOccupancy(hourlyCounts);

        return stats;
    }
}