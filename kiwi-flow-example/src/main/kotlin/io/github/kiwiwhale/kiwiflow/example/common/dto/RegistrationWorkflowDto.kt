package io.github.kiwiwhale.kiwiflow.example.common.dto

data class RegistrationRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class User(
    val id: Long,
    val email: String,
    val nickname: String
)