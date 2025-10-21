package com.adas.application.debug;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class ConfigTestController {

    @Value("${GOOGLE_APPLICATION_CREDENTIALS:NOT_SET}")
    private String googleCredentials;
    
    @Value("${GOOGLE_PROJECT_ID:NOT_SET}")
    private String googleProjectId;
    
    @Value("${google.stt.credentials-path:NOT_SET}")
    private String googleSttCredentials;
    
    @Value("${google.stt.project-id:NOT_SET}")
    private String googleSttProjectId;

    @GetMapping("/env")
    public Map<String, String> getEnvironmentVariables() {
        return Map.of(
            "GOOGLE_APPLICATION_CREDENTIALS", googleCredentials,
            "GOOGLE_PROJECT_ID", googleProjectId,
            "google.stt.credentials-path", googleSttCredentials,
            "google.stt.project-id", googleSttProjectId,
            "env_GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null ? System.getenv("GOOGLE_APPLICATION_CREDENTIALS") : "NOT_SET",
            "env_GOOGLE_PROJECT_ID", System.getenv("GOOGLE_PROJECT_ID") != null ? System.getenv("GOOGLE_PROJECT_ID") : "NOT_SET",
            "credentials_file_exists", java.nio.file.Files.exists(java.nio.file.Paths.get("C:/google-credentials/adas-stt-key.json")) ? "YES" : "NO"
        );
    }
}