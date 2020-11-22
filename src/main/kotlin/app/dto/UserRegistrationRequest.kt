package app.dto

import app.validation.requireNotBlank
import app.validation.requireValidEmail
import app.validation.requireValidPassword
import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
) {
    fun validate() {
        name.requireNotBlank { "Name must not be blank" }
        email.requireValidEmail { "Email is invalid" }
        password.requireValidPassword { "Password is invalid" }
    }
}