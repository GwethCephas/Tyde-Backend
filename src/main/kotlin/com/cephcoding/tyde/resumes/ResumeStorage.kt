package com.cephcoding.tyde.resumes

interface ResumeStorage {
    fun store(storedFileName: String, content: ByteArray)
}
