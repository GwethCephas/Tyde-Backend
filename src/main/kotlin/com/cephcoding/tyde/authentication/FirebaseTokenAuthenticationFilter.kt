package com.cephcoding.tyde.authentication

import com.cephcoding.tyde.users.UserService
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseTokenAuthenticationFilter(
    private val firebaseAppProvider: ObjectProvider<FirebaseApp>,
    private val userService: UserService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(FirebaseTokenAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION))
        val firebaseApp = firebaseAppProvider.getIfAvailable()

        if (token != null && firebaseApp != null) {
            try {
                val decodedToken = FirebaseAuth.getInstance(firebaseApp).verifyIdToken(token)
                val user = userService.resolveOrCreate(decodedToken.uid)
                val authenticatedUser = AuthenticatedUser(id = user.id!!, firebaseUid = user.firebaseUid)
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(authenticatedUser, null, emptyList())
            } catch (ex: FirebaseAuthException) {
                log.warn("Firebase ID token verification failed: {}", ex.message)
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractBearerToken(header: String?): String? {
        if (header == null || !header.startsWith("Bearer ")) {
            return null
        }
        return header.removePrefix("Bearer ").trim().ifBlank { null }
    }
}
