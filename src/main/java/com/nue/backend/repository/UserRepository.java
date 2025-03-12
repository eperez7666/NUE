package com.nue.backend.repository;

import com.nue.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
       Optional<User> findByEmailAddress(String emailAddress); // 🔹 Nuevo método
}
