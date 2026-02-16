package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.jobs.JobSeekerProfile;
import com.et.SudburyCityPlatform.models.program.ApplyProgramRequest;
import com.et.SudburyCityPlatform.models.program.Program;
import com.et.SudburyCityPlatform.models.program.ProgramRequest;
import com.et.SudburyCityPlatform.repository.Jobs.JobSeekerProfileRepository;
import com.et.SudburyCityPlatform.service.program.ProgramService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    private final ProgramService service;
    private final JobSeekerProfileRepository profileRepository;

    public ProgramController(ProgramService service, JobSeekerProfileRepository profileRepository) {
        this.service = service;
        this.profileRepository = profileRepository;
    }

    @PostMapping
    public Program create(@RequestBody @Valid ProgramRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public Program update(@PathVariable Long id,
                          @RequestBody @Valid ProgramRequest request) {
        return service.update(id, request);
    }

    @GetMapping
    public List<Program> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Program get(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @PostMapping("/{programId}/apply")
    public ResponseEntity<String> apply(
            @PathVariable Long programId,
            @RequestBody @Valid ApplyProgramRequest request) {

        service.apply(programId, request);
        return ResponseEntity.ok("Application submitted successfully");
    }
    @GetMapping("/recommended")
    public List<Program> recommendedPrograms(
            @RequestParam String email) {

        JobSeekerProfile profile =
                profileRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Profile required"));

        return service.recommendProgramsForJobSeeker(profile);
    }

}

