package com.dens.eduquiz.model

import com.dens.eduquiz.database.Students
import com.dens.eduquiz.utils.calculateAge
import kotlinx.serialization.Serializable


@Serializable
data class Student(
    val id: Long? ,
    val firstname: String,
    val lastname: String,
    val department: String,
    val course: String,
    val yearlevel: Long,
    val birthday: String,
    val qrcode: String,
    val age: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateStudentRequest(
    val firstname: String,
    val lastname: String,
    val yearlevel: Long,
    val course: String,
    val birthday: String,
    val department: String,
    val qrcode: String
)

@Serializable
data class UpdateStudentRequest(
    val firstname: String,
    val lastname: String,
    val yearlevel: Long,
    val course: String,
    val birthday: String,
    val department: String,
    val qrcode: String
)


// Extension to convert SQLDelight Students entity to Student DTO
fun com.dens.eduquiz.database.Students.toDTO() = Student(
    id = id,
    firstname = firstname,
    lastname = lastname,
    yearlevel = yearlevel,
    course = course,
    birthday = birthday,
    department = department,
    qrcode = qrcode,
    age = calculateAge(birthday),
    createdAt = createdAt,
    updatedAt = updatedAt
)
