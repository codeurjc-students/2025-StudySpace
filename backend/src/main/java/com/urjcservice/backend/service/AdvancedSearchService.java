package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Software;
import jakarta.persistence.EntityManager;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneId;
import java.util.List;

@Service
public class AdvancedSearchService {

    private final EntityManager entityManager;

    public AdvancedSearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public Page<Room> searchRooms(String text, Integer minCapacity, Room.CampusType campus, Boolean active, int page,
            int size) {
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<Room> result = searchSession.search(Room.class)
                .where(f -> f.bool(b -> {
                    if (active != null)
                        b.must(f.match().field("active").matching(active));
                    if (text != null && !text.isBlank()) {
                        String cleanText = text.toLowerCase().trim();
                        b.must(f.bool(sub -> {
                            sub.should(f.match().fields("name", "place", "software.name").matching(cleanText).fuzzy(1));
                            sub.should(f.wildcard().fields("name", "place", "software.name")
                                    .matching("*" + cleanText + "*"));
                        }));
                    }
                    if (minCapacity != null)
                        b.must(f.range().field("capacity").atLeast(minCapacity));
                    if (campus != null)
                        b.must(f.match().field("Camp").matching(campus));
                })).fetch(page * size, size);

        return new PageImpl<>(result.hits(), PageRequest.of(page, size), result.total().hitCount());
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String text, Boolean isBlocked, String role, String roomName, LocalDate date,
            int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<User> result = searchSession.search(User.class)
                .where(f -> f.bool(b -> {
                    if (text != null && !text.isBlank()) {
                        String cleanText = text.toLowerCase().trim();
                        b.must(f.bool(sub -> {
                            sub.should(f.match().fields("name", "email").matching(cleanText).fuzzy(1));
                            sub.should(f.wildcard().fields("name", "email").matching("*" + cleanText + "*"));
                        }));
                    }
                    if (isBlocked != null)
                        b.must(f.match().field("blocked").matching(isBlocked));
                    if (role != null && !role.isBlank())
                        b.must(f.match().field("roles").matching(role));

                    if (roomName != null && !roomName.isBlank()) {
                        String cleanRoom = roomName.toLowerCase().trim();
                        b.must(f.bool(sub -> {
                            sub.should(f.match().field("reservations.room.name").matching(cleanRoom).fuzzy(1));
                            sub.should(f.wildcard().field("reservations.room.name").matching("*" + cleanRoom + "*"));
                        }));
                    }

                    if (date != null) {
                        Date startOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                        Date endOfDay = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

                        b.must(f.bool(sub -> {
                            sub.must(f.range().field("reservations.startDate").lessThan(endOfDay));
                            sub.must(f.range().field("reservations.endDate").greaterThan(startOfDay));
                        }));
                    }
                })).fetch(page * size, size);

        return new PageImpl<>(result.hits(), PageRequest.of(page, size), result.total().hitCount());
    }

    @Transactional(readOnly = true)
    public Page<Software> searchSoftwares(String text, Float minVersion, int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<Software> result = searchSession.search(Software.class)
                .where(f -> f.bool(b -> {
                    if (text != null && !text.isBlank()) {
                        String cleanText = text.toLowerCase().trim();
                        b.must(f.bool(sub -> {
                            sub.should(f.match().fields("name", "description").matching(cleanText).fuzzy(1));
                            sub.should(f.wildcard().fields("name", "description").matching("*" + cleanText + "*"));
                        }));
                    }
                    if (minVersion != null)
                        b.must(f.range().field("version").atLeast(minVersion));
                })).fetch(page * size, size);

        return new PageImpl<>(result.hits(), PageRequest.of(page, size), result.total().hitCount());
    }
}