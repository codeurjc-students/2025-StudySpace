package com.urjcservice.backend.service;

import com.urjcservice.backend.dtos.DashboardStatsDTO;
import com.urjcservice.backend.repositories.RoomRepository;
import com.urjcservice.backend.repositories.ReservationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
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

    public DashboardStatsDTO getStats(LocalDate date) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        long totalRooms = roomRepo.countTotalRooms();
        stats.setTotalRooms(totalRooms);
        //total bookings
        long occupied = resRepo.countOccupiedRoomsByDate(date);

        if (totalRooms > 0) {
            //total ocupation 
            double occPct = ((double) occupied / totalRooms) * 100;
            stats.setOccupiedPercentage(Math.round(occPct * 100.0) / 100.0);
            stats.setFreePercentage(Math.round((100 - occPct) * 100.0) / 100.0);
        }

        // Software on book rooms
        if (occupied > 0) {
            long occupiedWithSoft = resRepo.countOccupiedWithSoftwareByDate(date);
            
            double softPct = ((double) occupiedWithSoft / occupied) * 100;
            stats.setRoomsWithSoftwarePercentage(Math.round(softPct * 100.0) / 100.0);
            stats.setRoomsWithoutSoftwarePercentage(Math.round((100 - softPct) * 100.0) / 100.0);
        } else {
            //no books, 0%
            stats.setRoomsWithSoftwarePercentage(0);
            stats.setRoomsWithoutSoftwarePercentage(0);
        }

        //Hour traffic
        List<Date> dates = resRepo.findStartDatesByDate(date);
        ZoneId madridZone = ZoneId.of("Europe/Madrid");

        Map<Integer, Long> hourlyCounts = dates.stream()
            .map(d -> d.toInstant().atZone(madridZone).getHour())
            .collect(Collectors.groupingBy(h -> h, Collectors.counting()));

        stats.setHourlyOccupancy(hourlyCounts);

        return stats;
    }
}