package com.cephcoding.tyde.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.File

@Configuration
class FirebaseConfig(private val firebaseProperties: FirebaseProperties) {

    private val log = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Bean
    fun firebaseApp(): FirebaseApp? {
        if (firebaseProperties.credentialsPath.isBlank()) {
            log.warn("FIREBASE_CREDENTIALS_PATH is not set; Firebase Admin SDK will not be initialized")
            return null
        }

        val credentialsFile = File(firebaseProperties.credentialsPath)
        if (!credentialsFile.exists()) {
            log.warn(
                "Firebase credentials file not found at {}; Firebase Admin SDK will not be initialized",
                firebaseProperties.credentialsPath
            )
            return null
        }

        if (FirebaseApp.getApps().isNotEmpty()) {
            return FirebaseApp.getInstance()
        }

        val options = FileInputStream(credentialsFile).use { stream ->
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()
        }

        return FirebaseApp.initializeApp(options)
    }
}
