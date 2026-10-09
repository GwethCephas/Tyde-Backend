package com.cephcoding.tyde.resumes

import com.cephcoding.tyde.config.ResumeProperties
import com.cephcoding.tyde.users.UserRepository
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.UUID

@Service
class ResumeService(
    private val resumeRepository: ResumeRepository,
    private val userRepository: UserRepository,
    private val resumeStorage: ResumeStorage,
    private val resumeProperties: ResumeProperties
) {

    fun upload(userId: UUID, file: MultipartFile): Resume {
        if (file.isEmpty) {
            throw EmptyFileException()
        }
        if (file.size > resumeProperties.maxSizeBytes) {
            throw FileTooLargeException(resumeProperties.maxSizeBytes)
        }

        val declaredContentType = try {
            file.contentType?.let { MediaType.parseMediaType(it) }
        } catch (ex: InvalidMediaTypeException) {
            null
        }
        if (declaredContentType == null || !declaredContentType.equalsTypeAndSubtype(MediaType.APPLICATION_PDF)) {
            throw UnsupportedResumeMediaTypeException(file.contentType)
        }

        val content = file.bytes
        if (!isPdf(content)) {
            throw InvalidPdfContentException()
        }

        val storedFileName = "${UUID.randomUUID()}.pdf"
        try {
            resumeStorage.store(storedFileName, content)
        } catch (ex: IOException) {
            throw ResumeStorageException(ex)
        }

        val resume = Resume(
            user = userRepository.getReferenceById(userId),
            originalFileName = file.originalFilename ?: "resume.pdf",
            storedFileName = storedFileName,
            contentType = MediaType.APPLICATION_PDF_VALUE,
            fileSize = file.size,
            status = ResumeStatus.UPLOADED
        )
        return resumeRepository.save(resume)
    }

    fun listForUser(userId: UUID): List<Resume> =
        resumeRepository.findByUserIdOrderByCreatedAtDesc(userId)

    private fun isPdf(content: ByteArray): Boolean =
        content.size >= PDF_MAGIC_BYTES.size &&
            content.sliceArray(PDF_MAGIC_BYTES.indices).contentEquals(PDF_MAGIC_BYTES)

    companion object {
        private val PDF_MAGIC_BYTES = byteArrayOf(
            '%'.code.toByte(), 'P'.code.toByte(), 'D'.code.toByte(), 'F'.code.toByte(), '-'.code.toByte()
        )
    }
}
