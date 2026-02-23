package com.et.SudburyCityPlatform.service.ai;

import com.et.SudburyCityPlatform.models.HuggingFaceRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ResumeAiParserService {

    @Value("${huggingface.api.url}")
    private String apiUrl;

    @Value("${huggingface.api.token}")
    private String apiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ResumeAiParserService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode extractStrictJson(String resumeText) {
        if (resumeText == null) resumeText = "";
        String text = truncate(resumeText, 9000);

        String prompt = """
Extract resume details and return STRICT JSON only.

Return this structure:

{
  "personalInfo": {...},
  "skills": [],
  "education": [],
  "experience": [],
  "projects": [],
  "certifications": [],
  "awards": []
}

Rules:
- Maximum 15 skills
- Maximum 5 responsibilities per job
- Maximum 5 technologies per job
- Keep values concise
- No markdown
- JSON must start with { and end with }

Resume:
%s
""".formatted(text).trim();

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You extract structured resume data and output strict JSON only."),
                Map.of("role", "user", "content", prompt)
        );

        String raw = callHuggingFace(messages);
        String content = extractAssistantContent(raw);
        String cleaned = cleanJson(content);
        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            // last chance: parse raw
            try {
                return objectMapper.readTree(raw);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String cleanJson(String content) {

        if (content == null) return null;

        content = content.trim();

        // Remove markdown fences
        content = content.replaceAll("(?s)```json", "");
        content = content.replaceAll("(?s)```", "");

        // Remove leading text before first {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");

        if (start >= 0 && end > start) {
            content = content.substring(start, end + 1);
        }

        return content.trim();
    }

    private String callHuggingFace(List<Map<String, String>> messages) {
        HuggingFaceRequest request = new HuggingFaceRequest();
        request.model = "mistralai/Mistral-7B-Instruct-v0.2";
        request.max_tokens = 1400;
        request.temperature = 0;
        request.messages = messages;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiToken != null && !apiToken.isBlank()) {
            headers.setBearerAuth(apiToken);
        }

        HttpEntity<HuggingFaceRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response =
                restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        return response.getBody();
    }

    private String extractAssistantContent(String raw) {

        try {
            JsonNode root = objectMapper.readTree(raw);

            if (root.has("error")) {
                System.out.println("HF ERROR: " + root.get("error").asText());
                return null;
            }

            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.size() == 0) {
                System.out.println("⚠ Invalid HF response structure");
                return null;
            }

            return choices.get(0)
                    .path("message")
                    .path("content")
                    .asText();

        } catch (Exception e) {
            System.out.println("Parsing failure: " + e.getMessage());
            return null;
        }
    }

    private static String extractJsonObject(String s) {
        if (s == null) return "{}";
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) return s.substring(start, end + 1);
        return "{}";
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}

