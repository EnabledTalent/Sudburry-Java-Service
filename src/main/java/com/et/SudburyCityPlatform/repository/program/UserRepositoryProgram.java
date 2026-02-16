package com.et.SudburyCityPlatform.repository.program;

import com.et.SudburyCityPlatform.models.program.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepositoryProgram extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
