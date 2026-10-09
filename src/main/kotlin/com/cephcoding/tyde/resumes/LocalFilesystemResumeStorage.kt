package com.cephcoding.tyde.resumes

import com.cephcoding.tyde.config.ResumeProperties
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class LocalFilesystemResumeStorage(
    private val resumeProperties: ResumeProperties
) : ResumeStorage {

    override fun store(storedFileName: String, content: ByteArray) {
        val storageDir = Paths.get(resumeProperties.storageDir)
        Files.createDirectories(storageDir)
        Files.write(storageDir.resolve(storedFileName), content)
    }
}
