package com.cephcoding.tyde.resumes

import java.util.UUID

data class ResumeResponse(
    val id: UUID,
    val originalFileName: String,
    val status: String,
    val uploadedAt: String
)

fun Resume.toResponse(): ResumeResponse = ResumeResponse(
    id = id!!,
    originalFileName = originalFileName,
    status = status.name,
    uploadedAt = createdAt!!.toString()
)
