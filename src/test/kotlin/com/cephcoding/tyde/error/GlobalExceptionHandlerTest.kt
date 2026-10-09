package com.cephcoding.tyde.error

import com.cephcoding.tyde.resumes.EmptyFileException
import com.cephcoding.tyde.resumes.FileTooLargeException
import com.cephcoding.tyde.resumes.InvalidPdfContentException
import com.cephcoding.tyde.resumes.ResumeStorageException
import com.cephcoding.tyde.resumes.UnsupportedResumeMediaTypeException
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MaxUploadSizeExceededException
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `maps empty file and invalid pdf content to 400`() {
        assertEquals(HttpStatus.BAD_REQUEST.value(), handler.handleBadRequest(EmptyFileException()).status)
        assertEquals(HttpStatus.BAD_REQUEST.value(), handler.handleBadRequest(InvalidPdfContentException()).status)
    }

    @Test
    fun `maps file too large to 413`() {
        assertEquals(
            HttpStatus.CONTENT_TOO_LARGE.value(),
            handler.handlePayloadTooLarge(FileTooLargeException(10485760)).status
        )
    }

    @Test
    fun `maps servlet container upload size exceeded to 413`() {
        val servletException = MaxUploadSizeExceededException(12 * 1024 * 1024L)

        assertEquals(HttpStatus.CONTENT_TOO_LARGE.value(), handler.handlePayloadTooLarge(servletException).status)
    }

    @Test
    fun `maps unsupported media type to 415`() {
        assertEquals(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            handler.handleUnsupportedMediaType(UnsupportedResumeMediaTypeException("image/png")).status
        )
    }

    @Test
    fun `maps resume storage failure to 500 without leaking the cause message`() {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.method).thenReturn("POST")
        `when`(request.requestURI).thenReturn("/api/resumes")

        val problem = handler.handleStorageFailure(ResumeStorageException(RuntimeException("/var/data/secret-path")), request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.status)
        assertEquals("Failed to store uploaded file", problem.detail)
    }
}
