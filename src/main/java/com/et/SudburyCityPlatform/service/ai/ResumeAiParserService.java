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
        String text = truncate(resumeText, 12000);

        String prompt = """
Extract the following details from the resume and return STRICT JSON only (no markdown, no explanations).

JSON schema:
{
  "personalInfo": {
    "name": string|null,
    "email": string|null,
    "phone": string|null,
    "linkedin": string|null,
    "github": string|null,
    "portfolio": string|null,
    "location": string|null
  },
  "skills": string[],
  "education": [
    {
      "degree": string|null,
      "fieldOfStudy": string|null,
      "institution": string|null,
      "startDate": string|null,
      "endDate": string|null,
      "grade": string|null,
      "location": string|null
    }
  ],
  "experience": [
    {
      "jobTitle": string|null,
      "company": string|null,
      "location": string|null,
      "startDate": string|null,
      "endDate": string|null,
      "currentlyWorking": boolean|null,
      "responsibilities": string[],
      "technologies": string[]
    }
  ],
  "projects": [
    {
      "name": string|null,
      "description": string|null,
      "startDate": string|null,
      "endDate": string|null,
      "currentlyWorking": boolean|null
    }
  ],
  "certifications": string[],
  "awards": string[]
}

Rules:
- Output must be valid JSON and MUST start with '{' and end with '}'.
- Use null for unknown fields.
- Arrays must be present (empty array if none).

Resume Text:
%s
""".formatted(text).trim();

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You extract structured resume data and output strict JSON only."),
                Map.of("role", "user", "content", prompt)
        );

        String raw = callHuggingFace(messages);
        String content = extractAssistantContent(raw);
        String json = extractJsonObject(content != null ? content : raw);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            // last chance: parse raw
            try {
                return objectMapper.readTree(raw);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String callHuggingFace(List<Map<String, String>> messages) {
        HuggingFaceRequest request = new HuggingFaceRequest();
        request.model = "meta-llama/Llama-3.1-8B-Instruct";
        request.max_tokens = 900;
        request.temperature = 0.1;
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
        }
        return null;
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

