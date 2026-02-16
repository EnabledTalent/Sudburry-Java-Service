package com.et.SudburyCityPlatform.repository.events;

import com.et.SudburyCityPlatform.models.events.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}