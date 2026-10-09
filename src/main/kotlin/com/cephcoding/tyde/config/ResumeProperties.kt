package com.cephcoding.tyde.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tyde.resume")
data class ResumeProperties(
    val maxSizeBytes: Long = 10485760,
    val storageDir: String = "./var/resumes"
)
