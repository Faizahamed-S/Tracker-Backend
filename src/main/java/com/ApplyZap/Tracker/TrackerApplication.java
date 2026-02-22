package com.ApplyZap.Tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class TrackerApplication {

    private static final Logger log = Logger.getLogger(TrackerApplication.class.getName());

    static {
        loadEnvFile();
    }

    /**
     * Find .env by: (1) ENV_FILE env var or env.file.path system property, or (2) walk up from user.dir.
     * Sets system properties so Spring can resolve ${SUPABASE_PROJECT_URL} etc.
     */
    private static void loadEnvFile() {
        String userDir = System.getProperty("user.dir");
        File envFile = null;
        String explicitPath = System.getenv("ENV_FILE");
        if (explicitPath == null || explicitPath.isEmpty()) explicitPath = System.getProperty("env.file.path");
        if (explicitPath != null && !explicitPath.isEmpty()) {
            File explicit = new File(explicitPath);
            if (explicit.isFile()) envFile = explicit;
        }
        if (envFile == null) {
            File dir = new File(userDir);
            for (int i = 0; i < 10 && dir != null; i++) {
                File candidate = new File(dir, ".env");
                if (candidate.isFile()) {
                    envFile = candidate;
                    break;
                }
                dir = dir.getParentFile();
            }
        }
        String primaryPath = new File(userDir, ".env").getAbsolutePath();
        if (envFile == null) {
            log.warning("No .env file found. Expected path: " + primaryPath + " (create it or set ENV_FILE / env.file.path). Set SUPABASE_PROJECT_URL etc. in env or system properties.");
            return;
        }
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(envFile.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2)
                    value = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                if (!key.isEmpty()) {
                    System.setProperty(key, value);
                    count++;
                }
            }
            log.log(Level.INFO, "Loaded .env from {0} ({1} properties set)", new Object[]{envFile.getAbsolutePath(), count});
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not load .env from " + envFile.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(TrackerApplication.class, args);
    }
}
