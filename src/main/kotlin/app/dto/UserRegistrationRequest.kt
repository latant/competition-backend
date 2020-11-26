package app.dto

import app.validation.requireNotBlank
import app.validation.requireValidEmail
import app.validation.requireValidPassword
import app.validation.validations
import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
) {
    fun validate() = validations {
        name.requireNotBlank { "Name must not be blank" }
        email.requireValidEmail { "Email is invalid" }
        password.requireValidPassword { "Password is invalid" }
    }
}