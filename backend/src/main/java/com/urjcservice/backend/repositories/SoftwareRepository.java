package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.Software;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SoftwareRepository extends JpaRepository<Software, Long> {


    Optional<Software> findByNameAndVersion(String name, Float version);

}
