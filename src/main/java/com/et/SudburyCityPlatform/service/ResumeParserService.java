package com.et.SudburyCityPlatform.service;

import com.et.SudburyCityPlatform.models.jobs.Education;
import com.et.SudburyCityPlatform.models.jobs.ResumeResponse;
import com.et.SudburyCityPlatform.models.jobs.WorkExperience;
import com.et.SudburyCityPlatform.service.ai.ResumeAiParserService;
import com.et.SudburyCityPlatform.service.ai.ResumeNormalization;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private final Tika tika = new Tika();
    private final ResumeAiParserService ai;

    private static final Map<String, List<String>> SECTION_ALIASES = Map.of(
            "education", List.of("education", "academic background"),
            "experience", List.of("experience", "work experience", "professional experience"),
            "skills", List.of("skills", "technical skills"),
            "projects", List.of("projects"),
            "achievements", List.of("achievements", "awards"),
            "certifications", List.of("certifications", "certificates"),
            "preferences", List.of("preferences"),
            "accessibility", List.of("accessibility", "disability")
    );

    public ResumeParserService(ResumeAiParserService ai) {
        this.ai = ai;
    }

    /* ======================= ENTRY POINT ======================= */

    public ResumeResponse parseResume(MultipartFile file) throws Exception {

        String text = normalize(tika.parseToString(file.getInputStream()));

        ResumeResponse response = new ResumeResponse();
        response.setOtherDetails(text);
        response.setReviewAgree(true);

        // 1) Try AI-first for better structured autofill.
        // 2) If AI fails, fall back to regex parsing (legacy behavior).
        boolean parsed = applyAiParsing(text, response);
        if (!parsed) {
            response.setBasicInfo(extractBasicInfo(text));
            response.setEducation(extractEducation(text));
            response.setWorkExperience(extractWorkExperience(text));
            response.setSkills(extractSkills(text));
            response.setProjects(extractList(text, "projects"));
            response.setAchievements(extractList(text, "achievements"));
            response.setCertification(extractList(text, "certifications"));
            response.setPreference(extractList(text, "preferences"));
            response.setAccessibilityNeeds(extractAccessibility(text));
        }

        return response;
    }

    private boolean applyAiParsing(String resumeText, ResumeResponse out) {
        try {

            JsonNode resumeJson = ai.extractStrictJson(resumeText);

            if (resumeJson == null || !resumeJson.isObject()) {
                return false;
            }

            // personalInfo
            JsonNode p = resumeJson.get("personalInfo");
            Map<String, String> basic = new HashMap<>();

            if (p != null && p.isObject()) {
                basic.put("name", textOrNull(p.get("name")));
                basic.put("email", textOrNull(p.get("email")));
                basic.put("phone", textOrNull(p.get("phone")));
                basic.put("linkedin", textOrNull(p.get("linkedin")));
            }

            out.setBasicInfo(basic);

            // skills
            out.setSkills(stringArray(resumeJson.get("skills")));

            // education
            out.setEducation(mapEducation(resumeJson.get("education")));

            // experience
            out.setWorkExperience(mapExperience(resumeJson.get("experience")));

            // projects
            out.setProjects(mapProjects(resumeJson.get("projects")));

            // certifications / awards
            out.setCertification(stringArray(resumeJson.get("certifications")));
            out.setAchievements(stringArray(resumeJson.get("awards")));

            // normalize
            out.setSkills(ResumeNormalization.normalizeStringList(out.getSkills(), true));
            out.setCertification(ResumeNormalization.normalizeStringList(out.getCertification(), false));
            out.setAchievements(ResumeNormalization.normalizeStringList(out.getAchievements(), false));
            out.setProjects(ResumeNormalization.normalizeStringList(out.getProjects(), false));
            out.setEducation(ResumeNormalization.normalizeEducation(out.getEducation()));
            out.setWorkExperience(ResumeNormalization.normalizeWorkExperience(out.getWorkExperience()));

            out.setPreference(List.of());
            out.setAccessibilityNeeds(List.of());

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String textOrNull(JsonNode n) {
        if (n == null || n.isNull()) return null;
        String s = n.asText();
        s = com.et.SudburyCityPlatform.service.ai.ResumeNormalization.clean(s);
        return s;
    }

    private static List<String> stringArray(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonNode n : arr) {
            String v = textOrNull(n);
            if (v != null) out.add(v);
        }
        return out;
    }

    private static List<Education> mapEducation(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        List<Education> out = new ArrayList<>();
        for (JsonNode n : arr) {
            if (n == null || !n.isObject()) continue;
            Education e = new Education();
            e.setDegree(textOrNull(n.get("degree")));
            e.setFieldOfStudy(textOrNull(n.get("fieldOfStudy")));
            e.setInstitution(textOrNull(n.get("institution")));
            e.setStartDate(textOrNull(n.get("startDate")));
            e.setEndDate(textOrNull(n.get("endDate")));
            e.setGrade(textOrNull(n.get("grade")));
            e.setLocation(textOrNull(n.get("location")));
            out.add(e);
        }
        return out;
    }

    private static List<WorkExperience> mapExperience(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        List<WorkExperience> out = new ArrayList<>();
        for (JsonNode n : arr) {
            if (n == null || !n.isObject()) continue;
            WorkExperience we = new WorkExperience();
            we.setJobTitle(textOrNull(n.get("jobTitle")));
            we.setCompany(textOrNull(n.get("company")));
            we.setLocation(textOrNull(n.get("location")));
            we.setStartDate(textOrNull(n.get("startDate")));
            we.setEndDate(textOrNull(n.get("endDate")));
            if (n.hasNonNull("currentlyWorking")) {
                we.setCurrentlyWorking(n.get("currentlyWorking").asBoolean());
            }
            we.setResponsibilities(stringArray(n.get("responsibilities")));
            we.setTechnologies(stringArray(n.get("technologies")));
            we.setDescription(null);
            out.add(we);
        }
        return out;
    }

    private static List<String> mapProjects(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonNode n : arr) {
            if (n == null || !n.isObject()) continue;
            String name = textOrNull(n.get("name"));
            String desc = textOrNull(n.get("description"));
            if (name == null && desc == null) continue;
            if (name != null && desc != null) out.add(name + " - " + desc);
            else out.add(name != null ? name : desc);
        }
        return out;
    }

    /* ======================= NORMALIZATION ======================= */

    private String normalize(String text) {
        return text.replaceAll("\\r", "")
                .replaceAll("[•●▪]", "-")
                .replaceAll("\\t", " ")
                .replaceAll(" +", " ")
                .trim();
    }

    /* ======================= BASIC INFO ======================= */

    private Map<String, String> extractBasicInfo(String text) {

        Map<String, String> info = new HashMap<>();
        info.put("name", extractName(text));
        info.put("email", extractByRegex(text, "[a-zA-Z0-9._%+-]+@[a-zA-Z.-]+\\.[a-z]{2,}"));
        info.put("phone", extractByRegex(text, "(\\+?\\d{1,3})?[\\s.-]?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}"));
        info.put("linkedin", extractByRegex(text, "linkedin\\.com/in/\\S+"));

        return info;
    }

    private String extractName(String text) {
        return Arrays.stream(text.split("\n"))
                .limit(6)
                .map(String::trim)
                .filter(l ->
                        l.matches("[A-Z][a-z]+\\s[A-Z]{1,3}") ||     // Anurag MS
                                l.matches("[A-Z][a-z]+\\s[A-Z][a-z]+")       // John Doe
                )
                .findFirst()
                .orElse("Not Found");
    }

    private String extractByRegex(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        return m.find() ? m.group() : "Not Found";
    }

    /* ======================= SECTION EXTRACTION ======================= */

    private String extractSection(String text, String key) {

        for (String alias : SECTION_ALIASES.getOrDefault(key, List.of(key))) {

            Pattern pattern = Pattern.compile(
                    "(?i)\\b" + Pattern.quote(alias) +
                            "\\b\\s*[:\\n](.*?)(?=\\n[A-Z][A-Z \\t]{3,}|$)",
                    Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }

    /* ======================= GENERIC LIST ======================= */

    private List<String> extractList(String text, String key) {

        String block = extractSection(text, key);
        if (block == null) return List.of();

        return Arrays.stream(block.split("\n|-"))
                .map(String::trim)
                .filter(s -> s.length() > 2)
                .distinct()
                .toList();
    }

    /* ======================= SKILLS ======================= */

    private List<String> extractSkills(String text) {

        String block = extractSection(text, "skills");
        if (block == null) return List.of();

        return Arrays.stream(block.split(",|\n|-"))
                .map(s -> s.replaceAll("(?i)programming:", "").trim())
                .filter(s -> s.length() > 1)
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    /* ======================= EDUCATION ======================= */

    private List<Education> extractEducation(String text) {

        String block = extractSection(text, "education");
        if (block == null) return List.of();

        List<Education> list = new ArrayList<>();
        Education current = null;

        for (String raw : block.split("\n")) {

            String line = raw.trim();
            if (line.isEmpty()) continue;

            // DEGREE LINE
            if (containsDegree(line)) {
                if (current != null) list.add(current);

                current = new Education();
                current.setDegree(extractDegree(line));
                current.setFieldOfStudy(extractField(line));

                if (containsDate(line)) {
                    current.setStartDate(extractYear(line, true));
                    current.setEndDate(extractYear(line, false));
                }
                continue;
            }

            // INSTITUTION (IMMEDIATELY AFTER DEGREE)
            if (current != null && current.getInstitution() == null && isInstitution(line)) {
                current.setInstitution(line);
            }
        }

        if (current != null) list.add(current);
        return list;
    }

    /* ======================= WORK EXPERIENCE ======================= */

    private List<WorkExperience> extractWorkExperience(String text) {

        String block = extractSection(text, "experience");
        if (block == null) return List.of();

        List<WorkExperience> experiences = new ArrayList<>();
        WorkExperience current = null;

        for (String raw : block.split("\n")) {

            String line = raw.trim();
            if (line.isEmpty()) continue;

            // NEW ROLE
            if (isJobTitle(line)) {
                if (current != null) experiences.add(current);
                current = new WorkExperience();
                current.setJobTitle(cleanJobTitle(line));
                current.setResponsibilities(new ArrayList<>());

                if (containsDate(line)) {
                    current.setStartDate(extractYear(line, true));
                    current.setEndDate(extractYear(line, false));
                }
                continue;
            }

            if (current == null) continue;

            // COMPANY
            if (current.getCompany() == null && isCompany(line)) {
                current.setCompany(line);
                continue;
            }

            // DATE LINE
            if (containsDate(line)) {
                current.setStartDate(extractYear(line, true));
                current.setEndDate(extractYear(line, false));
                current.setCurrentlyWorking(line.toLowerCase().contains("present"));
                continue;
            }

            // RESPONSIBILITIES
            if (line.startsWith("-") || line.length() > 12) {
                current.getResponsibilities().add(cleanBullet(line));
            }
        }

        if (current != null) experiences.add(current);
        return experiences;
    }

    /* ======================= HELPERS ======================= */

    private boolean isJobTitle(String line) {
        return line.matches("(?i).*(engineer|developer|analyst|manager|consultant|architect|lead).*");
    }

    private String cleanJobTitle(String line) {
        return line
                .replaceAll("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*", "")
                .replaceAll("(19|20)\\d{2}.*", "")
                .replaceAll("–|-", "")
                .trim();
    }


    private boolean isCompany(String line) {
        return line.matches("(?i)^([A-Z][A-Za-z0-9.& ]+),\\s*[A-Za-z ]+.*")
                || line.matches("(?i).*(inc|ltd|llc|corp|technologies|solutions|systems).*");
    }

    private boolean containsDegree(String line) {
        return line.matches("(?i).*(bachelor|master|phd|mba|b\\.tech|m\\.tech|bsc|msc).*");
    }

    private boolean isInstitution(String line) {
        return line.matches("(?i).*(university|college|institute|school).*");
    }

    private boolean containsDate(String line) {
        return line.matches(".*(19|20)\\d{2}.*")
                || line.matches("(?i).*(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec).*")
                || line.toLowerCase().contains("present");
    }

    private String extractDegree(String text) {
        Matcher m = Pattern.compile(
                "(Bachelor|Master|PhD|MBA|B\\.Tech|M\\.Tech|BSc|MSc)[^,\\n]*",
                Pattern.CASE_INSENSITIVE
        ).matcher(text);
        return m.find() ? m.group() : null;
    }

    private String extractField(String text) {
        Matcher m = Pattern.compile(
                "(Computer Science|Engineering|Information Technology|Business|Finance|Data Science)",
                Pattern.CASE_INSENSITIVE
        ).matcher(text);
        return m.find() ? m.group() : null;
    }

    private String extractYear(String text, boolean start) {
        Matcher m = Pattern.compile("(19|20)\\d{2}").matcher(text);
        List<String> years = new ArrayList<>();
        while (m.find()) years.add(m.group());
        if (years.isEmpty()) return null;
        return start ? years.get(0) : years.get(years.size() - 1);
    }

    private String cleanBullet(String line) {
        return line.replaceAll("^[•\\-]+", "").trim();
    }

    private List<String> extractAccessibility(String text) {
        String block = extractSection(text, "accessibility");
        return block == null ? List.of() : List.of(block);
    }
}
