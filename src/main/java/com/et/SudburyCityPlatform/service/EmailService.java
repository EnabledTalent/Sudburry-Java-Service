package com.et.SudburyCityPlatform.service;

import com.et.SudburyCityPlatform.models.jobs.ApplicationStatus;
import com.et.SudburyCityPlatform.models.jobs.Job;
import com.et.SudburyCityPlatform.models.jobs.JobApplicationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Sends transactional emails for application status updates.
 *
 * Uses Brevo Transactional Email API (HTTPS) to avoid SMTP restrictions in some hosts.
 */
@Service
public class EmailService {

    private static final String BREVO_SEND_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Value("${email.brevo.api-key:}")
    private String brevoApiKey;

    @Value("${email.brevo.from-email:}")
    private String brevoFromEmail;

    @Value("${email.brevo.from-name:SudburyCityPlatform}")
    private String brevoFromName;

    public void sendApplicationStatusEmail(JobApplicationRequest application, ApplicationStatus status) {
        if (application == null || application.getEmail() == null || application.getEmail().isBlank()) {
            return;
        }
        if (status != ApplicationStatus.INTERVIEW && status != ApplicationStatus.OFFERED && status != ApplicationStatus.REJECTED) {
            return;
        }

        if (!isConfigured()) {
            System.out.println("[EmailService] Brevo not configured (missing BREVO_API_KEY or BREVO_FROM_EMAIL); skipping email to " + application.getEmail());
            return;
        }

        Job job = application.getJob();
        String role = job != null && job.getRole() != null ? job.getRole() : "the role you applied for";
        String company =
                (job != null && job.getEmployer() != null && job.getEmployer().getCompanyName() != null && !job.getEmployer().getCompanyName().isBlank())
                        ? job.getEmployer().getCompanyName()
                        : (job != null && job.getCompanyName() != null && !job.getCompanyName().isBlank() ? job.getCompanyName() : "our team");

        String candidateName = ((application.getFirstName() != null ? application.getFirstName().trim() : "") +
                (application.getLastName() != null && !application.getLastName().isBlank() ? " " + application.getLastName().trim() : "")).trim();
        if (candidateName.isBlank()) candidateName = "Hello";

        String subject;
        String body;

        switch (status) {
            case INTERVIEW -> {
                subject = "Congratulations! Interview update — " + role + " at " + company;
                body = """
                        %s,

                        Congratulations — we reviewed your application and we’re excited to move you forward to the interview process for the %s position at %s.

                        What happens next:
                        - Our team will contact you shortly with available interview slots.
                        - Please keep an eye on your inbox (and spam/junk folder).
                        - If you need to update your availability, reply to this email with your preferred times.

                        We appreciate your interest in %s and look forward to speaking with you soon.

                        Best regards,
                        %s Hiring Team
                        """.formatted(candidateName, role, company, company, company);
            }
            case OFFERED -> {
                subject = "Job offer — " + role + " at " + company;
                body = """
                        %s,

                        Great news — congratulations! We’re happy to inform you that you have been offered the %s position at %s.

                        Next steps:
                        - You will receive additional details about compensation, start date, and onboarding.
                        - If you have any questions, reply to this email and our team will assist you.

                        Thank you for your time and effort throughout the application process. We’re excited about the possibility of you joining %s.

                        Warm regards,
                        %s Hiring Team
                        """.formatted(candidateName, role, company, company, company);
            }
            case REJECTED -> {
                subject = "Update on your application — " + role + " at " + company;
                body = """
                        %s,

                        Thank you for taking the time to apply for the %s position at %s.

                        After careful review, we won’t be moving forward with your application at this time. We know this is disappointing news, and we truly appreciate the effort you put into applying.

                        Please don’t be discouraged — we encourage you to apply again in the future as new roles open up that may be a better match.

                        We wish you the very best in your job search.

                        Sincerely,
                        %s Hiring Team
                        """.formatted(candidateName, role, company, company);
            }
            default -> {
                return;
            }
        }

        try {
            sendViaBrevo(application.getEmail(), subject, body);
        } catch (Exception e) {
            // Do not break the API flow if email fails; just log.
            System.out.println("[EmailService] Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Send an invite-to-apply email to a job seeker for a specific job.
     */
    public void sendInviteToApplyEmail(Job job, String inviteeEmail) {
        if (job == null || inviteeEmail == null || inviteeEmail.isBlank()) return;
        if (!isConfigured()) {
            System.out.println("[EmailService] Brevo not configured (missing BREVO_API_KEY or BREVO_FROM_EMAIL); skipping invite email to " + inviteeEmail);
            return;
        }

        String role = job.getRole() != null && !job.getRole().isBlank() ? job.getRole() : "a position";
        String company = (job.getEmployer() != null && job.getEmployer().getCompanyName() != null && !job.getEmployer().getCompanyName().isBlank())
                ? job.getEmployer().getCompanyName()
                : (job.getCompanyName() != null && !job.getCompanyName().isBlank() ? job.getCompanyName() : "our team");

        String subject = "You're invited to apply — " + role + " at " + company;
        String body = """
                Hello,

                You have been invited by %s to apply for the following role:

                Position: %s
                Company: %s

                We think your profile could be a great match. Please log in to the job platform and submit your application for this job when you're ready.

                We look forward to hearing from you.

                Best regards,
                %s Team
                """.formatted(company, role, company, company);

        try {
            sendViaBrevo(inviteeEmail, subject, body);
        } catch (Exception e) {
            System.out.println("[EmailService] Failed to send invite email: " + e.getMessage());
        }
    }

    private boolean isConfigured() {
        return brevoApiKey != null && !brevoApiKey.isBlank()
                && brevoFromEmail != null && !brevoFromEmail.isBlank();
    }

    private void sendViaBrevo(String toEmail, String subject, String text) {
        try {
            String fromName = (brevoFromName != null && !brevoFromName.isBlank()) ? brevoFromName : "SudburyCityPlatform";

            String body = "{"
                    + "\"sender\":{\"name\":\"" + jsonEscape(fromName) + "\",\"email\":\"" + jsonEscape(brevoFromEmail) + "\"},"
                    + "\"to\":[{\"email\":\"" + jsonEscape(toEmail) + "\"}],"
                    + "\"subject\":\"" + jsonEscape(subject) + "\","
                    + "\"textContent\":\"" + jsonEscape(text) + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_SEND_EMAIL_URL))
                    .header("api-key", brevoApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Brevo API failed with status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            // Don't break API flow; surface enough info for logs
            throw new IllegalStateException("Failed to send email via Brevo: " + e.getMessage(), e);
        }
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}

