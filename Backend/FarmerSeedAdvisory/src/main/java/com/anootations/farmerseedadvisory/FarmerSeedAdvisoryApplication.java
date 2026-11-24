package com.anootations.farmerseedadvisory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class FarmerSeedAdvisoryApplication {

    private static final Logger log = LoggerFactory.getLogger(FarmerSeedAdvisoryApplication.class);

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(FarmerSeedAdvisoryApplication.class, args);
    }

    /**
     * Automatically loads .env file properties into System properties
     * so Spring Boot can resolve them via ${PROPERTY_NAME} expressions.
     */
    private static void loadDotEnv() {
        File[] candidatePaths = new File[]{
                new File(".env"),
                new File("Backend/FarmerSeedAdvisory/.env"),
                new File("../.env")
        };

        for (File file : candidatePaths) {
            if (file.exists() && file.isFile()) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    int loadedCount = 0;
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                            continue;
                        }
                        int separatorIdx = trimmed.indexOf('=');
                        if (separatorIdx > 0) {
                            String key = trimmed.substring(0, separatorIdx).trim();
                            String value = trimmed.substring(separatorIdx + 1).trim();

                            // Strip surrounding single or double quotes
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                                value = value.substring(1, value.length() - 1);
                            }

                            // Don't overwrite existing system properties or OS environment variables
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                                loadedCount++;
                            }
                        }
                    }
                    log.info("[DotEnv] Successfully loaded {} properties from: {}", loadedCount, file.getAbsolutePath());
                    return;
                } catch (Exception e) {
                    log.warn("[DotEnv] Failed to read {}: {}", file.getAbsolutePath(), e.getMessage());
                }
            }
        }
        log.info("[DotEnv] No .env file found; relying on system environment variables.");
    }

}
