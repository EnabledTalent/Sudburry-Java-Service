package com.et.SudburyCityPlatform.service.ai;

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

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ResumeAiParserService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode extractStrictJson(String resumeText) {

        if (resumeText == null) resumeText = "";
        String text = truncate(resumeText, 10000);

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
- No markdown
- JSON must start with { and end with }

Resume:
%s
""".formatted(text).trim();

        Map<String, Object> request = Map.of(
                "model", "llama-3.3-70b-versatile",   // 8K context
                "temperature", 0,
                "max_tokens", 2500,
                "messages", List.of(
                        Map.of("role", "system", "content", "Return strict JSON only."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        return parseGroqResponse(response.getBody());
    }

    private JsonNode parseGroqResponse(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);

            JsonNode contentNode = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content");

            String content = cleanJson(contentNode.asText());

            return objectMapper.readTree(content);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String cleanJson(String content) {
        if (content == null) return null;

        content = content.trim();
        content = content.replaceAll("(?s)```json", "");
        content = content.replaceAll("(?s)```", "");

        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");

        if (start >= 0 && end > start) {
            content = content.substring(start, end + 1);
        }

        return content.trim();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}