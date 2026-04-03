package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CampusRepository extends JpaRepository<Campus, Long> {
    Optional<Campus> findByName(String name);
}