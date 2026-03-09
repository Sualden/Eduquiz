package com.dens.eduquiz.repository

import com.dens.eduquiz.database.StudentQueries
import com.dens.eduquiz.model.CreateStudentRequest
import com.dens.eduquiz.model.Student
import com.dens.eduquiz.model.UpdateStudentRequest
import com.dens.eduquiz.model.toDTO
import com.dens.eduquiz.utils.calculateAge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


class StudentRepository(private val queries: StudentQueries) {

    // Insert student using CreateStudentRequest
    suspend fun insertStudent(request: CreateStudentRequest): Long = withContext(Dispatchers.IO) {
        queries.insertStudent(
            firstname = request.firstname,
            lastname = request.lastname,
            yearlevel = request.yearlevel,
            course = request.course,
            birthday = request.birthday,
            department = request.department,
            qrcode = request.qrcode

        )
        queries.lastInsertStudentId().executeAsOne()
    }

    // Update student using UpdateStudentRequest
    suspend fun updateStudent(id: Long, request: UpdateStudentRequest) = withContext(Dispatchers.IO) {
        queries.updateStudent(
            firstname = request.firstname,
            lastname = request.lastname,
            yearlevel = request.yearlevel,
            course = request.course,
            birthday = request.birthday,
            department = request.department,
            qrcode = request.qrcode, // or current timestamp if you prefer
            id = id
        )
    }

    // Delete student
    suspend fun deleteStudent(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteStudent(id)
    }

    // Select all students
    suspend fun getAllStudents(): List<Student> = withContext(Dispatchers.IO) {
        queries.selectAllStudents().executeAsList().map { it.toDTO() }
    }

    // Select student by ID
    suspend fun getStudentById(id: Long): Student? = withContext(Dispatchers.IO) {
        queries.selectStudentById(id).executeAsOneOrNull()?.toDTO()
    }

    // Select student by QR code
    suspend fun getStudentByQr(qrcode: String): Student? = withContext(Dispatchers.IO) {
        queries.selectStudentByQr(qrcode).executeAsOneOrNull()?.toDTO()
    }
}

