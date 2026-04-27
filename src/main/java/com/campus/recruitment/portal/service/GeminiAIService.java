package com.campus.recruitment.portal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String callGemini(String prompt) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_GEMINI_API_KEY")) {
            return null;
        }
        try {
            String requestBody = """
                    {
                      "contents": [{"parts": [{"text": %s}]}],
                      "generationConfig": {"temperature": 0.3, "maxOutputTokens": 2048}
                    }
                    """.formatted(objectMapper.writeValueAsString(prompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText();
        } catch (Exception e) {
            System.err.println("Gemini API error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse resume text and extract structured info as JSON string.
     */
    public String parseResume(String resumeText) {
        String prompt = """
                You are an expert resume parser. Extract the following fields from the resume text below and return ONLY a JSON object with these keys:
                - skills (comma-separated string)
                - technicalSkills (comma-separated string)
                - experience (brief summary string)
                - projects (brief summary string)
                - certifications (comma-separated string)
                - summary (2-sentence professional summary)

                Resume Text:
                """ + resumeText;

        String result = callGemini(prompt);
        return result != null ? result : "{\"skills\":\"\",\"technicalSkills\":\"\",\"experience\":\"\",\"projects\":\"\",\"certifications\":\"\",\"summary\":\"Profile parsed from uploaded resume.\"}";
    }

    /**
     * Compute a match score (0-100) and explanation between a student profile and a job description.
     */
    public String computeJobMatch(String studentSkills, String jobDescription, String jobSkillsRequired) {
        String prompt = """
                You are a recruitment AI assistant. Given the student's skills and a job description, return ONLY a JSON object with:
                - score (integer 0-100)
                - explanation (one sentence why)
                - strengths (comma-separated list of matching skills)
                - gaps (comma-separated list of missing skills)

                Student Skills: %s
                Job Description: %s
                Required Skills: %s
                """.formatted(studentSkills, jobDescription, jobSkillsRequired);

        String result = callGemini(prompt);
        return result != null ? result : "{\"score\":50,\"explanation\":\"Match computed.\",\"strengths\":\"\",\"gaps\":\"\"}";
    }

    /**
     * Recommend jobs to a student based on their profile.
     */
    public String recommendJobs(String studentProfile, String availableJobs) {
        String prompt = """
                You are a career advisor AI. Based on the student profile below, recommend which jobs best suit them.
                Return ONLY a JSON array of job IDs (e.g. [1,3,5]) ranked from best to worst match.

                Student Profile: %s
                Available Jobs (id:title:skills): %s
                """.formatted(studentProfile, availableJobs);

        String result = callGemini(prompt);
        return result != null ? result : "[]";
    }

    /**
     * Generate a cover letter for a student applying to a job.
     */
    public String generateCoverLetter(String studentName, String studentSkills,
                                       String jobTitle, String companyName, String jobDescription) {
        String prompt = """
                Write a professional cover letter for %s applying to the position of %s at %s.
                Keep it concise (3 paragraphs). Highlight these skills: %s.
                Job Description: %s
                Return ONLY the cover letter text, no markdown.
                """.formatted(studentName, jobTitle, companyName, studentSkills, jobDescription);

        String result = callGemini(prompt);
        return result != null ? result : "I am excited to apply for this position and believe my skills align well with your requirements.";
    }
}
