package com.urjcservice.Backend.Repositories;

import com.urjcservice.Backend.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
