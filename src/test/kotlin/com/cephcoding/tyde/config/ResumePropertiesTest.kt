package com.cephcoding.tyde.config

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import kotlin.test.assertEquals

class ResumePropertiesTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(TestConfig::class.java)

    @Test
    fun `binds default resume properties when no environment overrides are present`() {
        contextRunner.run { context ->
            val properties = context.getBean(ResumeProperties::class.java)
            assertEquals(10485760, properties.maxSizeBytes)
            assertEquals("./var/resumes", properties.storageDir)
        }
    }

    @Test
    fun `binds overridden resume properties from configuration source`() {
        contextRunner
            .withPropertyValues(
                "tyde.resume.max-size-bytes=5242880",
                "tyde.resume.storage-dir=/tmp/resumes"
            )
            .run { context ->
                val properties = context.getBean(ResumeProperties::class.java)
                assertEquals(5242880, properties.maxSizeBytes)
                assertEquals("/tmp/resumes", properties.storageDir)
            }
    }

    @Configuration
    @EnableConfigurationProperties(ResumeProperties::class)
    class TestConfig
}
