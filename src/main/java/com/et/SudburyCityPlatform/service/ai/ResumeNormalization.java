package com.et.SudburyCityPlatform.service.ai;

import com.et.SudburyCityPlatform.models.jobs.Education;
import com.et.SudburyCityPlatform.models.jobs.WorkExperience;

import java.util.*;

public final class ResumeNormalization {

    private ResumeNormalization() {}

    public static String clean(String s) {
        if (s == null) return null;
        String out = s.replace("\u0000", "").replace("\r", "").trim();
        return out.isBlank() ? null : out;
    }

    public static String cleanEmail(String s) {
        String e = clean(s);
        return e != null ? e.toLowerCase(Locale.ROOT) : null;
    }

    public static List<String> normalizeStringList(List<String> list, boolean lower) {
        if (list == null) return List.of();
        Map<String, String> seen = new LinkedHashMap<>();
        for (String raw : list) {
            String c = clean(raw);
            if (c == null) continue;
            String key = lower ? c.toLowerCase(Locale.ROOT) : c;
            seen.putIfAbsent(key, c);
        }
        return new ArrayList<>(seen.values());
    }

    public static List<Education> normalizeEducation(List<Education> list) {
        if (list == null) return List.of();
        List<Education> out = new ArrayList<>();
        for (Education e : list) {
            if (e == null) continue;
            e.setDegree(clean(e.getDegree()));
            e.setFieldOfStudy(clean(e.getFieldOfStudy()));
            e.setInstitution(clean(e.getInstitution()));
            e.setStartDate(clean(e.getStartDate()));
            e.setEndDate(clean(e.getEndDate()));
            e.setGrade(clean(e.getGrade()));
            e.setLocation(clean(e.getLocation()));
            boolean empty = e.getDegree() == null
                    && e.getInstitution() == null
                    && e.getFieldOfStudy() == null
                    && e.getStartDate() == null
                    && e.getEndDate() == null;
            if (!empty) out.add(e);
        }
        return out;
    }

    public static List<WorkExperience> normalizeWorkExperience(List<WorkExperience> list) {
        if (list == null) return List.of();
        List<WorkExperience> out = new ArrayList<>();
        for (WorkExperience we : list) {
            if (we == null) continue;
            we.setJobTitle(clean(we.getJobTitle()));
            we.setCompany(clean(we.getCompany()));
            we.setLocation(clean(we.getLocation()));
            we.setStartDate(clean(we.getStartDate()));
            we.setEndDate(clean(we.getEndDate()));
            we.setDescription(clean(we.getDescription()));
            we.setResponsibilities(normalizeStringList(we.getResponsibilities(), false));
            we.setTechnologies(normalizeStringList(we.getTechnologies(), true));

            boolean empty = we.getJobTitle() == null && we.getCompany() == null && (we.getResponsibilities() == null || we.getResponsibilities().isEmpty());
            if (!empty) out.add(we);
        }
        return out;
    }
}

