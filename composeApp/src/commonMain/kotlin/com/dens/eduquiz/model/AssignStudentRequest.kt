package com.dens.eduquiz.model

import com.dens.eduquiz.database.Activity
import com.dens.eduquiz.database.Students
import kotlinx.serialization.Serializable

// ---------------------------------------------------------
// REQUEST SERIALIZERS
// ---------------------------------------------------------

@Serializable
data class AssignStudentRequest(
    val activityId: Long,
    val studentId: Long
)

@Serializable
data class RemoveStudentRequest(
    val activityId: Long,
    val studentId: Long
)


// ---------------------------------------------------------
// BASIC RELATION DTO
// ---------------------------------------------------------

@Serializable
data class ActivityStudentDTO(
    val id: Long,
    val activityId: Long,
    val studentId: Long
)


// ---------------------------------------------------------
// STUDENT DTO FOR "students in an activity"
// ---------------------------------------------------------

@Serializable
data class StudentInActivityDTO(
    val id: Long,
    val firstname: String,
    val lastname: String,
    val yearlevel: Long,
    val course: String,
    val birthday: String,
    val department: String,
    val qrcode: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

fun Students.toStudentInActivityDTO(): StudentInActivityDTO {
    return StudentInActivityDTO(
        id = id,
        firstname = firstname,
        lastname = lastname,
        yearlevel = yearlevel,
        course = course,
        birthday = birthday,
        department = department,
        qrcode = qrcode,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}


// ---------------------------------------------------------
// ACTIVITY DTO FOR "activities of a student"
// ---------------------------------------------------------

@Serializable
data class ActivityInStudentDTO(
    val id: Long,
    val title: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val deadline: String?,
    val status: String
)

fun Activity.toActivityInStudentDTO(): ActivityInStudentDTO {
    return ActivityInStudentDTO(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deadline = deadline,
        status = status
    )
}


// ---------------------------------------------------------
// UNIFIED RESPONSE: Students under an Activity
// ---------------------------------------------------------

@Serializable
data class ActivityStudentsResponse(
    val success: Boolean,
    val activityId: Long,
    val students: List<StudentInActivityDTO>
)


// ---------------------------------------------------------
// UNIFIED RESPONSE: Activities for a Student
// ---------------------------------------------------------

@Serializable
data class StudentActivitiesResponse(
    val success: Boolean,
    val studentId: Long,
    val activities: List<ActivityInStudentDTO>
)

@Serializable
data class ActionResponse(
    val success: Boolean,
    val message: String
)
