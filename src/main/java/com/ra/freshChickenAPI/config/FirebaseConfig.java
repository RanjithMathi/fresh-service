package com.ra.freshChickenAPI.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String firebaseServiceAccountJson;

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                
                // Method 1: Try environment variable first (for production)
                if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.trim().isEmpty()) {
                    try {
                        initializeWithEnvironmentVariable();
                        return;
                    } catch (Exception e) {
                        System.err.println("❌ Failed to initialize Firebase with environment variable: " + e.getMessage());
                        // Continue to try other methods
                    }
                }
                
                // Method 2: Try loading from classpath file (for local development)
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                if (resource.exists()) {
                    try (InputStream serviceAccount = resource.getInputStream()) {
                        initializeWithInputStream(serviceAccount);
                        return;
                    }
                }
                
                // Method 3: Fallback to default credentials (for cloud deployment)
                try {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();

                    FirebaseApp.initializeApp(options);
                    System.out.println("✅ Firebase Admin SDK initialized with default credentials");
                } catch (Exception e) {
                    System.err.println("❌ Failed to initialize Firebase with default credentials: " + e.getMessage());
                    System.out.println("⚠️ Firebase features will not be available");
                }
                
            } else {
                System.out.println("ℹ️ Firebase Admin SDK already initialized");
            }
        } catch (Exception e) {
            System.err.println("❌ Unexpected error initializing Firebase Admin SDK: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - allow app to start without Firebase
        }
    }
    
    private void initializeWithEnvironmentVariable() throws IOException {
        // Try base64 decoded JSON first
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseServiceAccountJson);
            try (InputStream serviceAccount = new ByteArrayInputStream(decodedBytes)) {
                initializeWithInputStream(serviceAccount);
                System.out.println("✅ Firebase Admin SDK initialized with base64 environment variable");
                return;
            }
        } catch (IllegalArgumentException e) {
            // Not base64, try as plain JSON
            try {
                byte[] jsonBytes = firebaseServiceAccountJson.getBytes(StandardCharsets.UTF_8);
                try (InputStream serviceAccount = new ByteArrayInputStream(jsonBytes)) {
                    initializeWithInputStream(serviceAccount);
                    System.out.println("✅ Firebase Admin SDK initialized with JSON environment variable");
                    return;
                }
            } catch (Exception ex) {
                throw new IOException("Failed to parse Firebase service account JSON from environment variable", ex);
            }
        }
    }
    
    private void initializeWithInputStream(InputStream serviceAccount) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
    }
}