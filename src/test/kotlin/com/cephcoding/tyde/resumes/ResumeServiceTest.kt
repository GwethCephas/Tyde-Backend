package com.cephcoding.tyde.resumes

import com.cephcoding.tyde.config.ResumeProperties
import com.cephcoding.tyde.users.User
import com.cephcoding.tyde.users.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockMultipartFile
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private class FakeResumeStorage : ResumeStorage {
    var storedFileName: String? = null
    var storedContent: ByteArray? = null

    override fun store(storedFileName: String, content: ByteArray) {
        this.storedFileName = storedFileName
        this.storedContent = content
    }
}

class ResumeServiceTest {

    private val resumeRepository: ResumeRepository = mock(ResumeRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val resumeStorage = FakeResumeStorage()
    private val resumeProperties = ResumeProperties(maxSizeBytes = 10, storageDir = "/tmp/unused")

    private lateinit var resumeService: ResumeService

    @BeforeEach
    fun setUp() {
        resumeService = ResumeService(resumeRepository, userRepository, resumeStorage, resumeProperties)
    }

    @Test
    fun `rejects an empty file`() {
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", ByteArray(0))

        assertFailsWith<EmptyFileException> {
            resumeService.upload(UUID.randomUUID(), file)
        }
    }

    @Test
    fun `rejects a file larger than the configured maximum`() {
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", ByteArray(20) { '%'.code.toByte() })

        assertFailsWith<FileTooLargeException> {
            resumeService.upload(UUID.randomUUID(), file)
        }
    }

    @Test
    fun `rejects a declared content type other than application pdf`() {
        val file = MockMultipartFile("file", "resume.pdf", "image/png", "%PDF-1.4".toByteArray())

        assertFailsWith<UnsupportedResumeMediaTypeException> {
            resumeService.upload(UUID.randomUUID(), file)
        }
    }

    @Test
    fun `rejects content whose bytes are not actually a pdf`() {
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", "not a pdf".toByteArray())

        assertFailsWith<InvalidPdfContentException> {
            resumeService.upload(UUID.randomUUID(), file)
        }
    }

    @Test
    fun `stores and persists a valid pdf upload`() {
        val userId = UUID.randomUUID()
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-1.4".toByteArray())
        Mockito.`when`(userRepository.getReferenceById(userId)).thenReturn(User(firebaseUid = "firebase-uid"))
        Mockito.`when`(resumeRepository.save(any())).thenAnswer { it.arguments[0] }

        val resume = resumeService.upload(userId, file)

        assertEquals("resume.pdf", resume.originalFileName)
        assertEquals(ResumeStatus.UPLOADED, resume.status)
        assertEquals("%PDF-1.4", resumeStorage.storedContent?.toString(Charsets.US_ASCII))
    }
}
