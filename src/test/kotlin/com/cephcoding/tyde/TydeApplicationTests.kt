package com.cephcoding.tyde

import com.cephcoding.tyde.config.ResumeProperties
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.servlet.autoconfigure.MultipartProperties
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class TydeApplicationTests : AbstractIntegrationTest() {

	@Test
	fun contextLoads() {
	}

	@Test
	fun `configured servlet multipart limits leave headroom above the configured resume size limit`(
		@Autowired multipartProperties: MultipartProperties,
		@Autowired resumeProperties: ResumeProperties
	) {
		val maxFileSizeBytes = multipartProperties.maxFileSize.toBytes()
		val maxRequestSizeBytes = multipartProperties.maxRequestSize.toBytes()

		assertTrue(
			maxFileSizeBytes > resumeProperties.maxSizeBytes,
			"spring.servlet.multipart.max-file-size must exceed tyde.resume.max-size-bytes so the " +
				"application's own validation handler produces the 413 response instead of the servlet container"
		)
		assertTrue(
			maxRequestSizeBytes > maxFileSizeBytes,
			"spring.servlet.multipart.max-request-size must exceed max-file-size to allow for multipart boundaries and form fields"
		)
	}

}
