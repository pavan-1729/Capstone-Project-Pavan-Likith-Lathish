package com.anootations.farmerseedadvisory.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Initializes the Firebase Admin SDK at application startup.
 * Sets IPv4 preference to avoid Windows socket connection timeouts when fetching Google certificates.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    static {
        // Force IPv4 on Windows to prevent IPv6 routing timeouts to googleapis.com
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");
    }

    @Value("${firebase.service-account.path:firebase-service-account.json}")
    private String serviceAccountPath;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase already initialized — skipping.");
                return;
            }

            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase Admin SDK initialized successfully.");

            // Pre-warm Google certificates in background so first incoming request doesn't wait
            prewarmGoogleCertificates();

        } catch (IOException e) {
            log.error("Firebase initialization failed: {}", e.getMessage());
        }
    }

    private void prewarmGoogleCertificates() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(8, TimeUnit.SECONDS)
                        .readTimeout(8, TimeUnit.SECONDS)
                        .build();

                Request req = new Request.Builder()
                        .url("https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com")
                        .get()
                        .build();

                try (Response res = client.newCall(req).execute()) {
                    if (res.isSuccessful()) {
                        log.info("Google certificate cache pre-warmed successfully (HTTP {})", res.code());
                    }
                }
            } catch (Exception e) {
                log.debug("Google certificate pre-warm note: {}", e.getMessage());
            }
        }, "firebase-cert-prewarm").start();
    }
}
