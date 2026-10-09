package com.cephcoding.tyde.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tyde.firebase")
data class FirebaseProperties(
    val credentialsPath: String = ""
)
