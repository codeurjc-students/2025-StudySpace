package com.urjcservice.backend.service;

import com.urjcservice.backend.entities.Campus;
import com.urjcservice.backend.repositories.CampusRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CampusService {

    private final CampusRepository campusRepository;

    public CampusService(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    public List<Campus> findAll() {
        return campusRepository.findAll();
    }

    public Optional<Campus> findById(Long id) {
        return campusRepository.findById(id);
    }

    public Campus save(Campus campus) {
        if (campusRepository.findByName(campus.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A campus with this name already exists.");
        }
        return campusRepository.save(campus);
    }
}