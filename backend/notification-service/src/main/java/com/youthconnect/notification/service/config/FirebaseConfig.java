package com.youthconnect.notification.service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * FIREBASE CONFIGURATION - PUSH NOTIFICATIONS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Initializes Firebase Admin SDK for sending push notifications to Android and iOS devices.
 *
 * Prerequisites:
 * 1. Create Firebase project at https://console.firebase.google.com
 * 2. Add Android app with package name: com.youthconnect.mobile
 * 3. Add iOS app with bundle ID: com.youthconnect.mobile
 * 4. Generate service account key:
 *    - Go to Project Settings → Service Accounts
 *    - Click "Generate New Private Key"
 *    - Save as firebase-service-account.json
 * 5. Place firebase-service-account.json in src/main/resources/
 *
 * Features Enabled:
 * - Push notifications to Android devices via FCM
 * - Push notifications to iOS devices via APNs
 * - Topic-based messaging for broadcasts
 * - Device group messaging
 * - Rich notifications with images and actions
 * - Notification analytics
 *
 * Security:
 * - Service account credentials required (never commit to Git)
 * - Server-side authentication only
 * - No client credentials exposed
 * - Encrypted communication with FCM
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-10-20
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    /**
     * Path to Firebase service account JSON file.
     * Default: firebase-service-account.json in resources folder
     * Override: Set FIREBASE_SERVICE_ACCOUNT environment variable
     */
    @Value("${firebase.service-account-file:firebase-service-account.json}")
    private String serviceAccountFile;

    /**
     * Firebase project ID.
     * Must match the project ID in Firebase Console.
     */
    @Value("${firebase.project-id:Entrepreneurship-Booster-Platform}")
    private String projectId;

    /**
     * Firebase Realtime Database URL (optional).
     * Only required if using Firebase Realtime Database.
     */
    @Value("${firebase.database-url:}")
    private String databaseUrl;

    /**
     * Initialize Firebase Admin SDK on application startup.
     *
     * This method runs once during Spring context initialization.
     * If initialization fails, push notifications will be disabled but the service will still start.
     *
     * Initialization Steps:
     * 1. Check if Firebase is already initialized (prevents duplicate initialization)
     * 2. Load service account credentials from JSON file
     * 3. Build Firebase options with project configuration
     * 4. Initialize Firebase app instance
     * 5. Log success or handle failure gracefully
     */
    @PostConstruct
    public void initializeFirebase() {
        try {
            log.info("🔧 Initializing Firebase Admin SDK...");

            // Check if Firebase is already initialized (singleton pattern)
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("✅ Firebase already initialized - Skipping");
                return;
            }

            // Load service account credentials
            InputStream serviceAccount = loadServiceAccountFile();

            if (serviceAccount == null) {
                log.error("❌ Firebase service account file not found: {}", serviceAccountFile);
                log.error("⚠️ Push notifications will be DISABLED");
                log.error("💡 Place {} in src/main/resources/", serviceAccountFile);
                return;
            }

            // Build Firebase options
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId);

            // Add database URL if provided (optional)
            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                optionsBuilder.setDatabaseUrl(databaseUrl);
            }

            FirebaseOptions options = optionsBuilder.build();

            // Initialize Firebase app
            FirebaseApp.initializeApp(options);

            log.info("✅ Firebase Admin SDK initialized successfully");
            log.info("📱 Push notifications enabled for project: {}", projectId);
            log.info("🔗 Firebase Console: https://console.firebase.google.com/project/{}", projectId);

        } catch (IOException e) {
            log.error("❌ Failed to load Firebase service account credentials", e);
            log.error("⚠️ Push notifications will be DISABLED");
            log.error("💡 Ensure {} exists and is valid JSON", serviceAccountFile);
            // Don't throw exception - allow service to start without push notifications
        } catch (Exception e) {
            log.error("❌ Unexpected error initializing Firebase Admin SDK", e);
            log.error("⚠️ Push notifications will be DISABLED");
            // Don't throw exception - graceful degradation
        }
    }

    /**
     * Load service account file from classpath or filesystem.
     *
     * Priority:
     * 1. Try loading from classpath (src/main/resources/)
     * 2. Try loading from filesystem (absolute path)
     * 3. Return null if not found
     *
     * @return InputStream of service account JSON file, or null if not found
     */
    private InputStream loadServiceAccountFile() {
        try {
            // Try loading from classpath first
            ClassPathResource resource = new ClassPathResource(serviceAccountFile);
            if (resource.exists()) {
                log.debug("📂 Loading Firebase credentials from classpath: {}", serviceAccountFile);
                return resource.getInputStream();
            }
        } catch (IOException e) {
            log.debug("⚠️ Firebase credentials not found in classpath, trying filesystem...");
        }

        try {
            // Try loading from filesystem (absolute path)
            log.debug("📂 Loading Firebase credentials from filesystem: {}", serviceAccountFile);
            return new FileInputStream(serviceAccountFile);
        } catch (IOException e) {
            log.debug("⚠️ Firebase credentials not found in filesystem either");
            return null;
        }
    }

    /**
     * Provides FirebaseApp instance as a Spring bean for dependency injection.
     *
     * Usage in other components:
     * <pre>
     * {@code
     * @Autowired
     * private FirebaseApp firebaseApp;
     * }
     * </pre>
     *
     * @return FirebaseApp instance, or null if initialization failed
     */
    @Bean
    public FirebaseApp firebaseApp() {
        try {
            return FirebaseApp.getInstance();
        } catch (IllegalStateException e) {
            log.warn("⚠️ FirebaseApp not initialized - push notifications unavailable");
            log.warn("💡 This is not critical - SMS and Email notifications will still work");
            return null;
        }
    }

    /**
     * Check if Firebase is properly initialized.
     *
     * Used by:
     * - Health check endpoint (/actuator/health)
     * - PushNotificationService to determine availability
     * - Monitoring and alerting systems
     *
     * @return true if Firebase is initialized, false otherwise
     */
    public boolean isFirebaseInitialized() {
        boolean initialized = !FirebaseApp.getApps().isEmpty();
        log.debug("🔍 Firebase initialization status: {}", initialized ? "READY" : "NOT_INITIALIZED");
        return initialized;
    }

    /**
     * Get Firebase project configuration details for monitoring and debugging.
     *
     * Returns formatted string with:
     * - Project ID
     * - App Name
     * - Initialization status
     *
     * Used by:
     * - Actuator info endpoint
     * - Admin dashboard
     * - Log analysis
     *
     * @return Human-readable project information string
     */
    public String getProjectInfo() {
        if (!isFirebaseInitialized()) {
            return "Firebase Status: NOT INITIALIZED - Push notifications disabled";
        }

        try {
            FirebaseApp app = FirebaseApp.getInstance();
            return String.format(
                    "Firebase Status: READY | Project: %s | App: %s",
                    app.getOptions().getProjectId(),
                    app.getName()
            );
        } catch (Exception e) {
            return "Firebase Status: ERROR - " + e.getMessage();
        }
    }

    /**
     * Get Firebase project ID.
     *
     * @return Firebase project ID
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Manually reinitialize Firebase (for testing or recovery scenarios).
     *
     * WARNING: Use with caution in production.
     * This will delete the existing Firebase app and reinitialize.
     *
     * Use cases:
     * - Testing different configurations
     * - Recovering from initialization failures
     * - Switching between projects (dev/staging/prod)
     */
    public void reinitialize() {
        log.warn("⚠️ Manually reinitializing Firebase Admin SDK...");

        try {
            // Delete existing Firebase app if present
            if (!FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.getInstance().delete();
                log.info("🗑️ Existing Firebase app deleted");
            }

            // Reinitialize
            initializeFirebase();

        } catch (Exception e) {
            log.error("❌ Failed to reinitialize Firebase", e);
        }
    }
}