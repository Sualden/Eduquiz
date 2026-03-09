package com.dens.eduquiz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dens.eduquiz.model.*
import com.dens.eduquiz.repository.StudentActivityRepository
import kotlinx.coroutines.launch

class StudentActivityViewModel(
    private val repository: StudentActivityRepository
) : ViewModel() {

    var studentsInActivity by mutableStateOf<List<StudentInActivityDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Fetch students assigned to an activity
    fun fetchStudentsByActivity(activityId: Long) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val dbStudents = repository.getStudentsByActivity(activityId)
                val dtos = dbStudents.map { it.toStudentInActivityDTO() }
                studentsInActivity = dtos
            } catch (e: Exception) {
                errorMessage = "Error loading students: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Assign a single student
    fun assignStudent(
        request: AssignStudentRequest,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val success = repository.addStudentToActivity(request.activityId, request.studentId)
                if (success) {
                    fetchStudentsByActivity(request.activityId)
                    onComplete(true, "Student assigned successfully")
                } else {
                    onComplete(false, "Failed to assign student")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }

    // --- NEW FUNCTION: Assign All Students ---
    fun assignAllStudents(
        activityId: Long,
        students: List<Student>,
        onComplete: () -> Unit
    ) {
        if (students.isEmpty()) return

        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                // Loop through all students and assign them
                students.forEach { student ->
                    student.id?.let { studentId ->
                        repository.addStudentToActivity(activityId, studentId)
                    }
                }
                // Refresh the list once finished
                fetchStudentsByActivity(activityId)
                onComplete()
            } catch (e: Exception) {
                errorMessage = "Error assigning all: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Remove a student
    fun removeStudent(
        request: RemoveStudentRequest,
        onComplete: (success: Boolean, message: String) -> Unit = { _, _ -> }
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val success = repository.removeStudentFromActivity(request.activityId, request.studentId)
                if (success) {
                    fetchStudentsByActivity(request.activityId)
                    onComplete(true, "Student removed successfully")
                } else {
                    onComplete(false, "Failed to remove student")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }
}