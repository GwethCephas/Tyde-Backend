package com.cephcoding.tyde.users

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

@Service
class UserService(
    private val userRepository: UserRepository,
    transactionManager: PlatformTransactionManager
) {
    private val newUserTransactionTemplate = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    fun resolveOrCreate(firebaseUid: String): User {
        userRepository.findByFirebaseUid(firebaseUid)?.let { return it }
        return try {
            newUserTransactionTemplate.execute {
                userRepository.save(User(firebaseUid = firebaseUid))
            }
        } catch (ex: DataIntegrityViolationException) {
            userRepository.findByFirebaseUid(firebaseUid) ?: throw ex
        }
    }
}
