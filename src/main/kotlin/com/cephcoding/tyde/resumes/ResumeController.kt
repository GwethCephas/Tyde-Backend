package com.cephcoding.tyde.resumes

import com.cephcoding.tyde.authentication.AuthenticatedUser
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/resumes")
class ResumeController(
    private val resumeService: ResumeService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(
        @RequestParam("file") file: MultipartFile,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<ResumeResponse> {
        val resume = resumeService.upload(principal.id, file)
        return ResponseEntity.status(HttpStatus.CREATED).body(resume.toResponse())
    }

    @GetMapping
    fun list(@AuthenticationPrincipal principal: AuthenticatedUser): List<ResumeResponse> =
        resumeService.listForUser(principal.id).map { it.toResponse() }
}
