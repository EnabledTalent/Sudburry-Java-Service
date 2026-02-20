package com.et.SudburyCityPlatform.repository.auth;

import com.et.SudburyCityPlatform.models.auth.UserLoginState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLoginStateRepository extends JpaRepository<UserLoginState, Long> {
    Optional<UserLoginState> findByEmail(String email);
}

