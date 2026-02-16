package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.dto.JobSeekerAiChatRequestDTO;
import com.et.SudburyCityPlatform.dto.JobSeekerAiChatResponseDTO;
import com.et.SudburyCityPlatform.exception.BadRequestException;
import com.et.SudburyCityPlatform.service.Jobs.JobSeekerAiService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobseeker/ai")
public class JobSeekerAiController {

    private final JobSeekerAiService jobSeekerAiService;

    public JobSeekerAiController(JobSeekerAiService jobSeekerAiService) {
        this.jobSeekerAiService = jobSeekerAiService;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasRole('STUDENT')")
    public JobSeekerAiChatResponseDTO chat(
            @Valid @RequestBody JobSeekerAiChatRequestDTO request,
            Authentication auth
    ) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            email = auth != null ? auth.getName() : null;
        }
        if (email == null || email.isBlank()) {
            throw new BadRequestException("email is required (or send JWT)");
        }

        String answer = jobSeekerAiService.ask(email, request);
        return new JobSeekerAiChatResponseDTO(answer, null);
    }
}

