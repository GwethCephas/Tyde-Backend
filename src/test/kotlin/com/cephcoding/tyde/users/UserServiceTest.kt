package com.cephcoding.tyde.users

import com.cephcoding.tyde.AbstractIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
class UserServiceTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `creates a new user on first resolution`() {
        val firebaseUid = "firebase-uid-${System.nanoTime()}"

        val user = userService.resolveOrCreate(firebaseUid)

        assertNotNull(user.id)
        assertEquals(firebaseUid, user.firebaseUid)
        assertNotNull(userRepository.findByFirebaseUid(firebaseUid))
    }

    @Test
    fun `resolving the same firebase uid twice returns the same user and creates no duplicate`() {
        val firebaseUid = "firebase-uid-${System.nanoTime()}"

        val first = userService.resolveOrCreate(firebaseUid)
        val second = userService.resolveOrCreate(firebaseUid)

        assertEquals(first.id, second.id)
        assertEquals(1, userRepository.findAll().count { it.firebaseUid == firebaseUid })
    }
}
