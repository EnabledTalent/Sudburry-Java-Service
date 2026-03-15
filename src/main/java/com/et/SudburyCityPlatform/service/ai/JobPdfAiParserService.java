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
public class JobPdfAiParserService {

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JobPdfAiParserService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode extractJobsFromText(String pdfText) {
        if (pdfText == null) pdfText = "";
        String text = truncate(pdfText, 12000);

        String prompt = """
Extract THE job posting from the text below. The content contains exactly ONE job. Return STRICT JSON only.

Return a JSON object with a single key "job" containing one job object (not an array).
The job object must have exactly these fields (use null if not found):
{
  "role": "Job title / role name",
  "companyName": "Company name or null",
  "jobLocation": "City, Province/State or null",
  "address": "Full address or null",
  "experienceRange": "e.g. 1-2, 2-3, 3-5, 5+ or null",
  "employmentType": "Full time / Part time / Internship / Contract / Hourly based or null",
  "typeOfWork": "Remote / Hybrid / Onsite or null",
  "preferredLanguage": "e.g. English, French or null",
  "urgentlyHiring": true or false,
  "jobDescription": "Full job description text",
  "requirements": "Skills, qualifications, requirements text",
  "salaryMin": number or null,
  "salaryMax": number or null,
  "externalApplyUrl": "URL to apply or null"
}

Rules:
- Return {"job": {...}} with exactly ONE job object (the content has one job only)
- If salary is given as a range like "$60K-$80K", convert to numbers: salaryMin=60000, salaryMax=80000
- If salary is hourly like "$25/hr", convert to annual: multiply by 2080
- No markdown, no explanation, JSON only
- JSON must start with { and end with }

Text:
%s
""".formatted(text).trim();

        Map<String, Object> request = Map.of(
                "model", "llama-3.3-70b-versatile",
                "temperature", 0,
                "max_tokens", 4000,
                "messages", List.of(
                        Map.of("role", "system", "content", "Return strict JSON only. Extract the single job posting. Return {\"job\": {...}} with one job object."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

        return parseResponse(response.getBody());
    }

    private JsonNode parseResponse(String raw) {
        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content");

            String cleaned = cleanJson(content.asText());
            return objectMapper.readTree(cleaned);
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
