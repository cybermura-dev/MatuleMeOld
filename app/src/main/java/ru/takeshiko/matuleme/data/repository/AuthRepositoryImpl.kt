package ru.takeshiko.matuleme.data.repository

import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.put
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.domain.models.result.AuthResult
import ru.takeshiko.matuleme.domain.repository.AuthRepository

class AuthRepositoryImpl(supabaseClientManager: SupabaseClientManager) : AuthRepository {

    private val auth = supabaseClientManager.auth
    private val postgrest = supabaseClientManager.postgrest

    override suspend fun register(email: String, password: String): AuthResult<Any> {
        return try {
            val response = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            AuthResult.Success(response as Any)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed!")
        }
    }


    override suspend fun login(email: String, password: String): AuthResult<Any> {
        return try {
            val response = auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            AuthResult.Success(response as Any)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed!")
        }
    }

    override suspend fun logout(): AuthResult<Any> {
        return try {
            val response = auth.signOut()
            AuthResult.Success(response as Any)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Logout failed!")
        }
    }

    override suspend fun sendVerificationEmail(email: String): AuthResult<Any> {
        return try {
            val response = auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = email
            )
            AuthResult.Success(response as Any)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send verification email!")
        }
    }

    override suspend fun verifyEmail(email: String, secret: String): AuthResult<Any> {
        return try {
            val response = auth.verifyEmailOtp(
                type = OtpType.Email.SIGNUP,
                email = email,
                token = secret
            )
            AuthResult.Success(response as Any)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Email verification failed!")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Any> {
        return try {
            val response = auth.resetPasswordForEmail(email)
            AuthResult.Success(response as Any)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send password reset email!")
        }
    }

    override suspend fun confirmPasswordReset(email: String, secret: String): AuthResult<Any> {
        return try {
            val response = auth.verifyEmailOtp(
                type = OtpType.Email.RECOVERY,
                email = email,
                token = secret
            )
            AuthResult.Success(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Password reset failed!")
        }
    }

    override suspend fun isUserAuthenticated(): AuthResult<Boolean> {
        return try {
            val session = auth.currentSessionOrNull()
            val isAuthenticated = session?.user != null
            AuthResult.Success(isAuthenticated)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to check authentication status!")
        }
    }

    override suspend fun updateUserData(
        email: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        avatarUrl: String?,
        phoneNumber: String?
    ): AuthResult<Any> {
        return try {
            val response = auth.updateUser {
                email?.let { this.email = it }
                password?.let { this.password = it }
                data {
                    firstName?.let { put("first_name", it) }
                    lastName?.let { put("last_name", it) }
                    avatarUrl?.let { put("avatar_url", it) }
                    phoneNumber?.let { put("phone_number", it) }
                }
            }
            AuthResult.Success(response as Any)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update user data!")
        }
    }
}