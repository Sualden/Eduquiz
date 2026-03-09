package com.dens.eduquiz.model

import com.dens.eduquiz.database.Admin
import kotlinx.serialization.Serializable

@Serializable
data class AdminDTO(
    val id: Long,
    val fullname: String,
    val email: String,
    val role: String
)

@Serializable
data class CreateAdminRequest(
    val fullname: String,
    val email: String,
    val passwordHash: String,
    val role: String = "admin"
)

fun Admin.toDTO() = AdminDTO(
    id = id,
    fullname = fullname,
    email = email,
    role = role
)
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
@Serializable
data class LoginResponse(
    val token: String,
    val admin: AdminDTO
)
