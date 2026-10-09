package com.cephcoding.tyde.resumes

class EmptyFileException : RuntimeException("Uploaded file is empty")

class FileTooLargeException(maxSizeBytes: Long) :
    RuntimeException("Uploaded file exceeds the maximum allowed size of $maxSizeBytes bytes")

class UnsupportedResumeMediaTypeException(declaredContentType: String?) :
    RuntimeException("Unsupported content type: $declaredContentType")

class InvalidPdfContentException : RuntimeException("Uploaded file content is not a valid PDF")

class ResumeStorageException(cause: Throwable) : RuntimeException("Failed to store resume file", cause)
