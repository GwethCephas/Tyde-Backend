package com.cephcoding.tyde.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileInputStream
import java.io.IOException

@Configuration
class FirebaseConfig(private val firebaseProperties: FirebaseProperties) {

    private val log = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Bean
    fun firebaseApp(): FirebaseApp? {
        if (firebaseProperties.credentialsPath.isBlank()) {
            if (firebaseProperties.required) {
                throw IllegalStateException(
                    "tyde.firebase.required is true but FIREBASE_CREDENTIALS_PATH is not set"
                )
            }
            log.warn("FIREBASE_CREDENTIALS_PATH is not set; Firebase Admin SDK will not be initialized")
            return null
        }

        val credentialsFile = File(firebaseProperties.credentialsPath)
        if (!credentialsFile.exists()) {
            if (firebaseProperties.required) {
                throw IllegalStateException(
                    "tyde.firebase.required is true but no file exists at ${firebaseProperties.credentialsPath}"
                )
            }
            log.error(
                "FIREBASE_CREDENTIALS_PATH is set to {} but no file exists there; Firebase Admin SDK will not be initialized and all authenticated requests will be rejected",
                firebaseProperties.credentialsPath
            )
            return null
        }

        if (FirebaseApp.getApps().isNotEmpty()) {
            return FirebaseApp.getInstance()
        }

        return try {
            val options = FileInputStream(credentialsFile).use { stream ->
                FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build()
            }
            FirebaseApp.initializeApp(options)
        } catch (ex: IOException) {
            if (firebaseProperties.required) {
                throw IllegalStateException(
                    "tyde.firebase.required is true but credentials at ${firebaseProperties.credentialsPath} could not be read",
                    ex
                )
            }
            log.error(
                "Failed to read Firebase credentials from {}; Firebase Admin SDK will not be initialized and all authenticated requests will be rejected",
                firebaseProperties.credentialsPath,
                ex
            )
            null
        }
    }
}
