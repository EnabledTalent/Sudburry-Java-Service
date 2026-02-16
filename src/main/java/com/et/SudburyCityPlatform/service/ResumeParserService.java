package com.et.SudburyCityPlatform.service;

import com.et.SudburyCityPlatform.models.jobs.Education;
import com.et.SudburyCityPlatform.models.jobs.ResumeResponse;
import com.et.SudburyCityPlatform.models.jobs.WorkExperience;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private final Tika tika = new Tika();

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

    /* ======================= ENTRY POINT ======================= */

    public ResumeResponse parseResume(MultipartFile file) throws Exception {

        String text = normalize(tika.parseToString(file.getInputStream()));

        ResumeResponse response = new ResumeResponse();
        response.setBasicInfo(extractBasicInfo(text));
        response.setEducation(extractEducation(text));
        response.setWorkExperience(extractWorkExperience(text));
        response.setSkills(extractSkills(text));
        response.setProjects(extractList(text, "projects"));
        response.setAchievements(extractList(text, "achievements"));
        response.setCertification(extractList(text, "certifications"));
        response.setPreference(extractList(text, "preferences"));
        response.setAccessibilityNeeds(extractAccessibility(text));
        response.setOtherDetails(text);
        response.setReviewAgree(true);

        return response;
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
