package com.urjcservice.backend.repositories;

import com.urjcservice.backend.entities.Software;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoftwareRepository extends JpaRepository<Software, Long> {
}
