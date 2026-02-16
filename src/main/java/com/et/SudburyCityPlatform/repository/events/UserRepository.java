package com.et.SudburyCityPlatform.repository.events;


import com.et.SudburyCityPlatform.models.events.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<UserEvent, Long> {
    Optional<UserEvent> findByEmail(String email);
}

