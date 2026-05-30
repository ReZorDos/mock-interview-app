package com.technokratos.agona.repository;

import com.technokratos.agona.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByName(String name);

    Optional<User> findByUsername(String username);
}
