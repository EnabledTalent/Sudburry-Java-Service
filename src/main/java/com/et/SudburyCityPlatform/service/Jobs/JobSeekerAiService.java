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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class JobSeekerAiService {

    private static final String OUT_OF_SCOPE_REPLY = "Sorry i have no information about wat ur asking";

    private static final Set<String> JOB_KEYWORDS = Set.of(
            "job", "jobs", "role", "position", "opening", "openings", "available",
            "apply", "application", "applied", "status", "interview", "offer", "hired", "rejected",
            "salary", "location", "remote", "hybrid", "onsite", "requirements", "experience", "skills", "resume", "cv"
    );

    private static final Pattern JOB_ID_PATTERN = Pattern.compile(
            "(?i)\\b(job\\s*id|jobid|id)\\s*[:=#]?\\s*\\d+\\b"
    );

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

        if (!isJobQuestion(req.getMessage())) {
            return OUT_OF_SCOPE_REPLY;
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
        String out = answer != null ? answer : raw;
        return sanitizeOutput(out);
    }

    private String systemPrompt() {
        return """
You are a job search assistant for a job seeker.
You will be given two contexts: AVAILABLE_JOBS and APPLIED_JOBS (with statuses).
Answer ONLY questions about jobs, available jobs, and the user's applied jobs (application statuses) you can answer about what the user needs to do in order to get a particular job and how to apply for it.
If the user asks anything outside jobs, reply exactly: "Sorry i have no information about wat ur asking"
Do not include job IDs, application IDs, or any identifiers in your answer.
Use plain text only (no special characters, no bullet symbols, no emojis).
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
                sb.append("- role=").append(safe(j.getRole()))
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
                sb.append("- status=").append(a.getStatus() != null ? a.getStatus().name() : "N/A")
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

    private static boolean isJobQuestion(String message) {
        if (message == null) return false;
        String m = message.trim().toLowerCase();
        if (m.isBlank()) return false;
        for (String k : JOB_KEYWORDS) {
            if (m.contains(k)) return true;
        }
        return false;
    }

    private static String sanitizeOutput(String input) {
        if (input == null) return "";
        // Remove explicit job id mentions first.
        String s = JOB_ID_PATTERN.matcher(input).replaceAll("");

        // Normalize to ASCII and drop non-ASCII characters (emojis, bullets, etc.).
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);
        s = s.replaceAll("[^\\x00-\\x7F]", "");

        // Keep only letters/digits/space and very basic punctuation.
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)
                    || c == '.' || c == ',' || c == '!' || c == '?' || c == '-' ) {
                out.append(c);
            }
        }

        // Collapse whitespace and trim.
        String cleaned = out.toString().replaceAll("\\s+", " ").trim();
        return cleaned.isBlank() ? OUT_OF_SCOPE_REPLY : cleaned;
    }
}

