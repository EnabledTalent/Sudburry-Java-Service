package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.models.places.LocalPlace;
import com.et.SudburyCityPlatform.models.places.LocalPlaceRequest;
import com.et.SudburyCityPlatform.service.place.LocalPlaceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/local-places")
public class LocalPlaceController {

    private final LocalPlaceService service;

    public LocalPlaceController(LocalPlaceService service) {
        this.service = service;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<LocalPlace> create(
            @Valid @RequestBody LocalPlaceRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<LocalPlace> update(
            @PathVariable Long id,
            @Valid @RequestBody LocalPlaceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<LocalPlace>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<LocalPlace> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

