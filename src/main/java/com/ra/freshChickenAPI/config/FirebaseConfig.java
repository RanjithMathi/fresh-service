package com.ra.freshChickenAPI.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Check if Firebase is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                // Load service account key from classpath
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

                if (resource.exists()) {
                    // Initialize with service account key file
                    try (InputStream serviceAccount = resource.getInputStream()) {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();

                        FirebaseApp.initializeApp(options);
                        System.out.println("✅ Firebase Admin SDK initialized with service account key");
                    }
                } else {
                    // Fallback: Use environment variables or default credentials
                    // This works when running on Google Cloud Platform
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();

                    FirebaseApp.initializeApp(options);
                    System.out.println("✅ Firebase Admin SDK initialized with default credentials");
                }
            } else {
                System.out.println("ℹ️ Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to initialize Firebase Admin SDK: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - allow app to start without Firebase
        } catch (Exception e) {
            System.err.println("❌ Unexpected error initializing Firebase Admin SDK: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - allow app to start without Firebase
        }
    }
}