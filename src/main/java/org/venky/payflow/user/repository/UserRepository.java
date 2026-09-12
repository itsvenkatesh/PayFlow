package org.venky.payflow.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.venky.payflow.user.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);
}
