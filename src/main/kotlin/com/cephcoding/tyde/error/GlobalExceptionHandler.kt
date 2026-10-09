package com.cephcoding.tyde.error

import com.cephcoding.tyde.resumes.EmptyFileException
import com.cephcoding.tyde.resumes.FileTooLargeException
import com.cephcoding.tyde.resumes.InvalidPdfContentException
import com.cephcoding.tyde.resumes.ResumeStorageException
import com.cephcoding.tyde.resumes.UnsupportedResumeMediaTypeException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(EmptyFileException::class, InvalidPdfContentException::class)
    fun handleBadRequest(ex: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request")

    @ExceptionHandler(FileTooLargeException::class, MaxUploadSizeExceededException::class)
    fun handlePayloadTooLarge(ex: Exception): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONTENT_TOO_LARGE, "Uploaded file exceeds the maximum allowed size")

    @ExceptionHandler(UnsupportedResumeMediaTypeException::class)
    fun handleUnsupportedMediaType(ex: UnsupportedResumeMediaTypeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only application/pdf uploads are supported")

    @ExceptionHandler(ResumeStorageException::class)
    fun handleStorageFailure(ex: ResumeStorageException, request: HttpServletRequest): ProblemDetail {
        log.error(
            "Resume storage failure on {} {}: {}",
            request.method,
            request.requestURI,
            ex.cause?.javaClass?.simpleName ?: ex.javaClass.simpleName
        )
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded file")
    }
}
