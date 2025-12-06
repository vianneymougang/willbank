package com.willbank.userservice.repository;

import com.willbank.userservice.model.User;
import com.willbank.userservice.model.UserRole;
import com.willbank.userservice.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}
