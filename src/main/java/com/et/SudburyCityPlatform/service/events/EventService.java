package com.et.SudburyCityPlatform.service.events;

import com.et.SudburyCityPlatform.exception.ConflictException;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.events.*;
import com.et.SudburyCityPlatform.repository.events.EventRegistrationRepository;
import com.et.SudburyCityPlatform.repository.events.EventRepository;
import com.et.SudburyCityPlatform.repository.events.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository registrationRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository, EventRegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
    }

    public Event createEvent(EventRequest request) {
        Event event = new Event();
        mapRequestToEvent(request, event);
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        mapRequestToEvent(request, event);
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    private void mapRequestToEvent(EventRequest request, Event event) {
        event.setName(request.getName());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setEventTime(request.getEventTime());
        event.setFree(request.isFree());
        event.setPrice(request.isFree() ? BigDecimal.ZERO : request.getPrice());
        event.setDescription(request.getDescription());
    }
    public void registerUser(Long eventId, EventRegistrationRequest request) {

        // 1️⃣ Check if event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // 2️⃣ Find or create user
        UserEvent user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    UserEvent newUser = new UserEvent();
                    newUser.setName(request.getName());
                    newUser.setEmail(request.getEmail());
                    return userRepository.save(newUser);
                });

        // 3️⃣ Check if already registered
        if (registrationRepository.existsByEventAndUser(event, user)) {
            throw new ConflictException("User already registered for this event");
        }

        // 4️⃣ Create registration
        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setRegisteredAt(LocalDateTime.now());

        registrationRepository.save(registration);
    }
}

