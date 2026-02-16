package com.et.SudburyCityPlatform.service.program;

import com.et.SudburyCityPlatform.exception.ConflictException;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.jobs.JobSeekerProfile;
import com.et.SudburyCityPlatform.models.program.*;
import com.et.SudburyCityPlatform.repository.Jobs.JobSeekerProfileRepository;
import com.et.SudburyCityPlatform.repository.program.ProgramEnrollmentRepository;
import com.et.SudburyCityPlatform.repository.program.ProgramRepository;
import com.et.SudburyCityPlatform.repository.program.UserRepositoryProgram;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository repository;

    private final UserRepositoryProgram userRepository;
    private final ProgramEnrollmentRepository enrollmentRepository;
    private final JobSeekerProfileRepository profileRepository;

    public ProgramService(ProgramRepository repository, UserRepositoryProgram userRepository, ProgramEnrollmentRepository enrollmentRepository, JobSeekerProfileRepository profileRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.profileRepository = profileRepository;
    }

    public Program create(ProgramRequest request) {
        Program program = new Program();
        map(request, program);
        return repository.save(program);
    }

    public Program update(Long id, ProgramRequest request) {
        Program program = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));
        map(request, program);
        return repository.save(program);
    }

    public List<Program> getAll() {
        return repository.findAll();
    }

    public Program getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void map(ProgramRequest r, Program p) {
        p.setTitle(r.getTitle());
        p.setProvider(r.getProvider());
        p.setCost(r.getCost());
        p.setDuration(r.getDuration());
        p.setStartDate(r.getStartDate());
        p.setAvailableSeats(r.getAvailableSeats());
        p.setDescription(r.getDescription());
        p.setLearningPoints(r.getLearningPoints());
        p.setEligibility(r.getEligibility());
    }
    public void apply(Long programId, ApplyProgramRequest request) {

        // 1️⃣ Program exists?
        Program program = repository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

        if (program.getAvailableSeats() <= 0) {
            throw new ConflictException("No seats available");
        }

        // 2️⃣ Find or create user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User u = new User();
                    u.setFullName(request.getFullName());
                    u.setEmail(request.getEmail());
                    u.setPhone(request.getPhone());
                    return userRepository.save(u);
                });

        // 3️⃣ Prevent duplicate enrollment
        if (enrollmentRepository.existsByProgramAndUser(program, user)) {
            throw new ConflictException("Already enrolled in this program");
        }

        // 4️⃣ Save enrollment
        ProgramEnrollment enrollment = new ProgramEnrollment();
        enrollment.setProgram(program);
        enrollment.setUser(user);
        enrollment.setAppliedAt(LocalDateTime.now());

        enrollmentRepository.save(enrollment);

        // 5️⃣ Reduce seat count
        program.setAvailableSeats(program.getAvailableSeats() - 1);
        repository.save(program);
    }
    public List<Program> recommendProgramsForJobSeeker(
            JobSeekerProfile profile) {

        List<String> skills =
                profile.getSkills() == null
                        ? List.of()
                        : profile.getSkills()
                        .stream()
                        .map(String::toLowerCase)
                        .toList();

        return repository.findAll()
                .stream()
                .filter(program -> {

                    // 1️⃣ Match learning points
                    boolean matchesLearning =
                            program.getLearningPoints() != null &&
                                    program.getLearningPoints().stream()
                                            .anyMatch(lp ->
                                                    skills.stream()
                                                            .anyMatch(skill ->
                                                                    lp.toLowerCase()
                                                                            .contains(skill)));

                    // 2️⃣ Match eligibility gaps
                    boolean matchesEligibility =
                            program.getEligibility() != null &&
                                    program.getEligibility().stream()
                                            .anyMatch(e ->
                                                    skills.stream()
                                                            .anyMatch(skill ->
                                                                    e.toLowerCase()
                                                                            .contains(skill)));

                    return matchesLearning || matchesEligibility;
                })
                .toList();
    }

}

