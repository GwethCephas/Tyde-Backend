package com.cephcoding.tyde.authentication

import java.util.UUID

data class AuthenticatedUser(
    val id: UUID,
    val firebaseUid: String
)
