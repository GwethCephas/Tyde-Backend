package com.cephcoding.tyde.resumes

import com.cephcoding.tyde.AbstractIntegrationTest
import com.cephcoding.tyde.authentication.AuthenticatedUser
import com.cephcoding.tyde.users.User
import com.cephcoding.tyde.users.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ResumeControllerTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    private fun authenticationFor(user: User): UsernamePasswordAuthenticationToken {
        val principal = AuthenticatedUser(id = user.id!!, firebaseUid = user.firebaseUid)
        return UsernamePasswordAuthenticationToken(principal, null, emptyList())
    }

    private fun newUser(): User = userRepository.save(User(firebaseUid = "firebase-uid-${System.nanoTime()}"))

    @Test
    fun `uploading a valid pdf returns 201 with resume metadata`() {
        val user = newUser()
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", "%PDF-1.4".toByteArray())

        mockMvc.perform(
            multipart("/api/resumes").file(file).with(authentication(authenticationFor(user)))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.originalFileName").value("resume.pdf"))
            .andExpect(jsonPath("$.status").value("UPLOADED"))
            .andExpect(jsonPath("$.uploadedAt").value(org.hamcrest.Matchers.matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?Z")))
    }

    @Test
    fun `uploading an empty file returns 400`() {
        val user = newUser()
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", ByteArray(0))

        mockMvc.perform(
            multipart("/api/resumes").file(file).with(authentication(authenticationFor(user)))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `uploading content that is not actually a pdf returns 400`() {
        val user = newUser()
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", "not a pdf".toByteArray())

        mockMvc.perform(
            multipart("/api/resumes").file(file).with(authentication(authenticationFor(user)))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `uploading a non-pdf declared content type returns 415`() {
        val user = newUser()
        val file = MockMultipartFile("file", "resume.png", "image/png", "%PDF-1.4".toByteArray())

        mockMvc.perform(
            multipart("/api/resumes").file(file).with(authentication(authenticationFor(user)))
        ).andExpect(status().isUnsupportedMediaType)
    }

    @Test
    fun `uploading a wildcard declared content type returns 415`() {
        val user = newUser()
        val file = MockMultipartFile("file", "resume.pdf", "*/*", "%PDF-1.4".toByteArray())

        mockMvc.perform(
            multipart("/api/resumes").file(file).with(authentication(authenticationFor(user)))
        ).andExpect(status().isUnsupportedMediaType)
    }

    @Test
    fun `uploading a file over the application size limit returns 413`() {
        val user = newUser()
        val oversized = ByteArray(11 * 1024 * 1024)
        val file = MockMultipartFile("file", "resume.pdf", "application/pdf", oversized)

        mockMvc.perform(
            multipart("/api/resumes").file(file).with(authentication(authenticationFor(user)))
        ).andExpect(status().isContentTooLarge)
    }

    @Test
    fun `requests without authentication are rejected with 401`() {
        mockMvc.perform(get("/api/resumes"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `listing resumes returns only the authenticated user's resumes ordered most recent first`() {
        val owner = newUser()
        val otherUser = newUser()

        uploadPdfAs(owner, "first.pdf")
        Thread.sleep(5)
        uploadPdfAs(owner, "second.pdf")
        uploadPdfAs(otherUser, "someone-elses-resume.pdf")

        mockMvc.perform(get("/api/resumes").with(authentication(authenticationFor(owner))))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].originalFileName").value("second.pdf"))
            .andExpect(jsonPath("$[1].originalFileName").value("first.pdf"))
    }

    private fun uploadPdfAs(user: User, fileName: String) {
        val file = MockMultipartFile("file", fileName, "application/pdf", "%PDF-1.4".toByteArray())
        mockMvc.perform(
            multipart("/api/resumes").file(file).with(authentication(authenticationFor(user)))
        ).andExpect(status().isCreated)
    }
}
