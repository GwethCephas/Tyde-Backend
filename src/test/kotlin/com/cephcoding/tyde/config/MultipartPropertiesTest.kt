package com.cephcoding.tyde.config

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.servlet.autoconfigure.MultipartProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.util.unit.DataSize
import kotlin.test.assertEquals

class MultipartPropertiesTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(TestConfig::class.java)

    @Test
    fun `binds configured multipart size properties`() {
        contextRunner
            .withPropertyValues(
                "spring.servlet.multipart.max-file-size=7MB",
                "spring.servlet.multipart.max-request-size=9MB"
            )
            .run { context ->
                val multipartProperties = context.getBean(MultipartProperties::class.java)
                assertEquals(DataSize.ofMegabytes(7), multipartProperties.maxFileSize)
                assertEquals(DataSize.ofMegabytes(9), multipartProperties.maxRequestSize)
            }
    }

    @Configuration
    @EnableConfigurationProperties(MultipartProperties::class)
    class TestConfig
}
