package ru.takeshiko.matuleme.domain.repository

import ru.takeshiko.matuleme.domain.models.result.AuthResult

interface AuthRepository {
    suspend fun register(email: String, password: String): AuthResult<Any>
    suspend fun login(email: String, password: String): AuthResult<Any>
    suspend fun logout(): AuthResult<Any>

    suspend fun sendVerificationEmail(email: String): AuthResult<Any>
    suspend fun verifyEmail(email: String, secret: String): AuthResult<Any>

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Any>
    suspend fun confirmPasswordReset(email: String, secret: String): AuthResult<Any>

    suspend fun isUserAuthenticated(): AuthResult<Boolean>
    suspend fun updateUserData(
        email: String? = null,
        password: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        avatarUrl: String? = null,
        phoneNumber: String? = null
    ): AuthResult<Any>
}