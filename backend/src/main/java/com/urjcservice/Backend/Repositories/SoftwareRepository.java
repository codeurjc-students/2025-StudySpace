package com.urjcservice.Backend.Repositories;

import com.urjcservice.Backend.Entities.Software;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoftwareRepository extends JpaRepository<Software, Long> {
}
