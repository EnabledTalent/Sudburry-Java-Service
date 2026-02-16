package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.models.events.Event;
import com.et.SudburyCityPlatform.models.events.EventRegistrationRequest;
import com.et.SudburyCityPlatform.models.events.EventRequest;
import com.et.SudburyCityPlatform.service.events.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // 1️⃣ Create Event
    @PostMapping
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    // 2️⃣ Update Event
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    // 3️⃣ Get All Events
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    // 4️⃣ Get Event by ID
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    // 5️⃣ Delete Event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{eventId}/register")
    public ResponseEntity<String> registerForEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRegistrationRequest request) {

        eventService.registerUser(eventId, request);
        return ResponseEntity.ok("User registered successfully");
    }
}

