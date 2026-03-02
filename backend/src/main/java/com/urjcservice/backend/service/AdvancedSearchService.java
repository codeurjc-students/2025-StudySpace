package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Reservation;
import com.urjcservice.backend.entities.Room;
import com.urjcservice.backend.entities.User;
import com.urjcservice.backend.entities.Software;
import jakarta.persistence.EntityManager;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
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
                    applyTextSearch(f, b, text, "name", "place", "software.name");
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
                    applyTextSearch(f, b, text, "name", "email");
                    if (isBlocked != null)
                        b.must(f.match().field("blocked").matching(isBlocked));
                    if (role != null && !role.isBlank())
                        b.must(f.match().field("roles").matching(role));
                    applyTextSearch(f, b, roomName, "reservations.room.name");
                    applyDateFilter(f, b, date, "reservations.startDate", "reservations.endDate");
                })).fetch(page * size, size);

        return new PageImpl<>(result.hits(), PageRequest.of(page, size), result.total().hitCount());
    }

    @Transactional(readOnly = true)
    public Page<Software> searchSoftwares(String text, Float minVersion, int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<Software> result = searchSession.search(Software.class)
                .where(f -> f.bool(b -> {
                    applyTextSearch(f, b, text, "name", "description");
                    if (minVersion != null)
                        b.must(f.range().field("version").atLeast(minVersion));
                })).fetch(page * size, size);

        return new PageImpl<>(result.hits(), PageRequest.of(page, size), result.total().hitCount());
    }

    @Transactional(readOnly = true)
    public Page<Reservation> searchReservations(Long userId, String text, LocalDate date, int page, int size) {
        SearchSession searchSession = Search.session(entityManager);
        SearchResult<Reservation> result = searchSession.search(Reservation.class)
                .where(f -> f.bool(b -> {
                    b.must(f.match().field("user.id").matching(userId));
                    applyTextSearch(f, b, text, "room.name", "reason");
                    applyDateFilter(f, b, date, "startDate", "endDate");
                })).fetch(page * size, size);

        return new PageImpl<>(result.hits(), PageRequest.of(page, size), result.total().hitCount());
    }

    private void applyTextSearch(SearchPredicateFactory f, BooleanPredicateClausesStep<?> b, String text,
            String... fields) {
        if (text != null && !text.isBlank()) {
            String cleanText = text.toLowerCase().trim();
            b.must(f.bool(sub -> {
                sub.should(f.match().fields(fields).matching(cleanText).fuzzy(1));
                sub.should(f.wildcard().fields(fields).matching("*" + cleanText + "*"));
            }));
        }
    }

    private void applyDateFilter(SearchPredicateFactory f, BooleanPredicateClausesStep<?> b, LocalDate date,
            String startField, String endField) {
        if (date != null) {
            Date startOfDay = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endOfDay = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            b.must(f.bool(sub -> {
                sub.must(f.range().field(startField).lessThan(endOfDay));
                sub.must(f.range().field(endField).greaterThan(startOfDay));
            }));
        }
    }
}