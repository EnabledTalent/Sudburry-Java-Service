package com.et.SudburyCityPlatform.service.Jobs;

import com.et.SudburyCityPlatform.dto.JobSeekerAiChatRequestDTO;
import com.et.SudburyCityPlatform.exception.BadRequestException;
import com.et.SudburyCityPlatform.models.jobs.Job;
import com.et.SudburyCityPlatform.models.jobs.JobApplicationRequest;
import com.et.SudburyCityPlatform.models.HuggingFaceRequest;
import com.et.SudburyCityPlatform.repository.Jobs.JobApplicationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class JobSeekerAiService {

    @Value("${huggingface.api.url}")
    private String apiUrl;

    @Value("${huggingface.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JobService jobService;
    private final JobApplicationRepository jobApplicationRepository;

    public JobSeekerAiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            JobService jobService,
            JobApplicationRepository jobApplicationRepository
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.jobService = jobService;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public String ask(String email, JobSeekerAiChatRequestDTO req) {
        if (req.getMessage() == null || req.getMessage().isBlank()) {
            throw new BadRequestException("message is required");
        }

        int maxAvailable = req.getMaxAvailableJobs() != null ? req.getMaxAvailableJobs() : 15;
        int maxApplied = req.getMaxAppliedJobs() != null ? req.getMaxAppliedJobs() : 15;

        // Pull available jobs from DB (same source as GET /api/v1/jobs/job)
        List<Job> availableJobs = new ArrayList<>(jobService.getAllJobs());
        availableJobs.sort(Comparator.comparing(Job::getPostedDate, Comparator.nullsLast(Comparator.reverseOrder())));
        if (availableJobs.size() > maxAvailable) {
            availableJobs = availableJobs.subList(0, maxAvailable);
        }

        List<JobApplicationRequest> applied = new ArrayList<>(jobApplicationRepository.findByEmail(email));
        if (applied.size() > maxApplied) {
            applied = applied.subList(0, maxApplied);
        }

        String context = buildContext(email, availableJobs, applied);
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt()),
                Map.of("role", "system", "content", context),
                Map.of("role", "user", "content", req.getMessage())
        );

        String raw = callHuggingFace(messages);
        String answer = extractAssistantContent(raw);
        return answer != null ? answer : raw;
    }

    private String systemPrompt() {
        return """
You are a job search assistant for a job seeker.
You will be given two contexts: AVAILABLE_JOBS and APPLIED_JOBS (with statuses).
Answer the user's questions ONLY using the provided job context when referencing specific jobs.
If the user asks for recommendations, propose up to 5 jobs and cite their job IDs.
If the user asks about application status, use APPLIED_JOBS.
Keep responses concise and actionable.
""".trim();
    }

    private String buildContext(String email, List<Job> available, List<JobApplicationRequest> applied) {
        StringBuilder sb = new StringBuilder();
        sb.append("USER_EMAIL: ").append(email).append("\n\n");

        sb.append("AVAILABLE_JOBS:\n");
        if (available.isEmpty()) {
            sb.append("- (none)\n");
        } else {
            for (Job j : available) {
                sb.append("- id=").append(j.getId())
                        .append(", role=").append(safe(j.getRole()))
                        .append(", location=").append(safe(j.getLocation()))
                        .append(", type=").append(safe(j.getEmploymentType()))
                        .append(", salary=").append(j.getSalary() != null ? j.getSalary() : "N/A")
                        .append(", requirements=").append(snippet(j.getRequirements(), 180))
                        .append("\n");
            }
        }

        sb.append("\nAPPLIED_JOBS (this user has applied to):\n");
        if (applied.isEmpty()) {
            sb.append("- (none)\n");
        } else {
            for (JobApplicationRequest a : applied) {
                Job j = a.getJob();
                sb.append("- applicationId=").append(a.getId())
                        .append(", jobId=").append(j != null ? j.getId() : "N/A")
                        .append(", status=").append(a.getStatus() != null ? a.getStatus().name() : "N/A")
                        .append(", role=").append(j != null ? safe(j.getRole()) : "N/A")
                        .append(", location=").append(j != null ? safe(j.getLocation()) : "N/A")
                        .append(", type=").append(j != null ? safe(j.getEmploymentType()) : "N/A")
                        .append("\n");
            }
        }

        return sb.toString();
    }

    private String callHuggingFace(List<Map<String, String>> messages) {
        HuggingFaceRequest request = new HuggingFaceRequest();
        request.model = "meta-llama/Llama-3.1-8B-Instruct";
        request.max_tokens = 350;
        request.temperature = 0.2;
        request.messages = messages;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        HttpEntity<HuggingFaceRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response =
                restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String extractAssistantContent(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && !choices.isEmpty()) {
                JsonNode msg = choices.get(0).get("message");
                if (msg != null) {
                    JsonNode content = msg.get("content");
                    if (content != null && !content.isNull()) {
                        return content.asText();
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to raw response
        }
        return null;
    }

    private static String safe(String s) {
        return s == null ? "N/A" : s.replace("\n", " ").trim();
    }

    private static String snippet(String s, int max) {
        if (s == null) return "N/A";
        String cleaned = s.replace("\n", " ").trim();
        return cleaned.length() <= max ? cleaned : cleaned.substring(0, max) + "...";
    }
}

