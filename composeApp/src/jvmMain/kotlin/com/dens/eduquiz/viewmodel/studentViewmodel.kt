package com.dens.eduquiz.viewmodel

import androidx.compose.runtime.*
import com.dens.eduquiz.model.CreateStudentRequest
import com.dens.eduquiz.model.Student
import com.dens.eduquiz.model.UpdateStudentRequest
import com.dens.eduquiz.utils.calculateAge
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class StudentScreenViewModel(private val baseUrl: String = "http://localhost:9090") {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    private val _studentList = mutableStateListOf<Student>()
    val studentList: List<Student> get() = _studentList

    var uiState by mutableStateOf(StudentScreenState())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    init { fetchStudents() }


    // --- EVENT HANDLING ---
    fun onEvent(event: StudentScreenEvent) {
        when (event) {
            is StudentScreenEvent.OnFirstNameChanged -> uiState = uiState.copy(firstname = event.value)
            is StudentScreenEvent.OnLastNameChanged -> uiState = uiState.copy(lastname = event.value)
            is StudentScreenEvent.OnDepartmentChanged -> uiState = uiState.copy(department = event.value)
            is StudentScreenEvent.OnCourseChanged -> uiState = uiState.copy(course = event.value)
            is StudentScreenEvent.OnYearLevelChanged -> uiState = uiState.copy(yearlevel = event.value)
            is StudentScreenEvent.OnBirthdayChanged -> uiState = uiState.copy(birthday = event.value)
            is StudentScreenEvent.OnQRCodeChanged -> uiState = uiState.copy(qrcode = event.value)
            is StudentScreenEvent.OnSearchQueryChanged -> uiState = uiState.copy(searchQuery = event.value)
            is StudentScreenEvent.OnShowAllToggled -> uiState = uiState.copy(showAllStudents = event.value)
            is StudentScreenEvent.OnPageChanged -> uiState = uiState.copy(currentPage = event.value)
            is StudentScreenEvent.OnSubmitStudent -> submitStudent()
            is StudentScreenEvent.OnDeleteStudent -> deleteStudent(event.id)
            is StudentScreenEvent.OnEditStudent -> {
                val s = event.student
                uiState = uiState.copy(
                    isEditing = true,
                    editingId = s.id,
                    firstname = s.firstname,
                    lastname = s.lastname,
                    department = s.department,
                    course = s.course,
                    yearlevel = s.yearlevel.toString(),
                    birthday = s.birthday,
                    qrcode = s.qrcode
                )
            }
            is StudentScreenEvent.OnCancelEdit -> {
                uiState = StudentScreenState()
                errorMessage = null
            }
            else -> {}
        }
    }

    // --- API OPERATIONS ---
    private fun fetchStudents() {
        scope.launch {
            try {
                val response: List<Student> = client.get("$baseUrl/students").body()
                val studentsWithAge = response.map { it.copy(age = calculateAge(it.birthday)) }
                withContext(Dispatchers.Main) {
                    _studentList.clear()
                    _studentList.addAll(studentsWithAge)
                    errorMessage = null
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { errorMessage = "Fetch Error: ${e.message}" }
            }
        }
    }

    private fun submitStudent() {
        scope.launch {
            try {
                val yLevel = uiState.yearlevel.toLongOrNull() ?: 1L
                val response: HttpResponse

                if (uiState.isEditing && uiState.editingId != null) {
                    val updateRequest = UpdateStudentRequest(
                        firstname = uiState.firstname,
                        lastname = uiState.lastname,
                        department = uiState.department,
                        course = uiState.course,
                        yearlevel = yLevel,
                        birthday = uiState.birthday,
                        qrcode = uiState.qrcode
                    )

                    response = client.put("$baseUrl/students/${uiState.editingId}") {
                        contentType(ContentType.Application.Json)
                        setBody(updateRequest)
                    }
                } else {
                    val createRequest = CreateStudentRequest(
                        firstname = uiState.firstname,
                        lastname = uiState.lastname,
                        department = uiState.department,
                        course = uiState.course,
                        yearlevel = yLevel,
                        birthday = uiState.birthday,
                        qrcode = uiState.qrcode
                    )

                    response = client.post("$baseUrl/students/insert") {
                        contentType(ContentType.Application.Json)
                        setBody(createRequest)
                    }
                }

                if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                    fetchStudents() // Recalculate age automatically
                    withContext(Dispatchers.Main) {
                        uiState = StudentScreenState()
                        errorMessage = null
                    }
                } else {
                    val errorBody = response.bodyAsText()
                    withContext(Dispatchers.Main) {
                        errorMessage = "Server Error (${response.status}): $errorBody"
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) { errorMessage = "Submit Failed: ${e.message}" }
            }
        }
    }

    private fun deleteStudent(id: Long?) {
        scope.launch {
            try {
                val response = client.delete("$baseUrl/students/$id")
                if (response.status == HttpStatusCode.OK) fetchStudents()
                else withContext(Dispatchers.Main) { errorMessage = "Delete Failed: ${response.status}" }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { errorMessage = "Delete Error: ${e.message}" }
            }
        }
    }

    fun dispose() { client.close() }
}


data class StudentScreenState(
    val firstname: String = "",
    val lastname: String = "",
    val department: String = "",
    val course: String = "",
    val yearlevel: String = "",
    val birthday: String = "",
    val editingId: Long? = null,
    val isEditing: Boolean = false,
    val searchQuery: String = "",
    val showAllStudents: Boolean = false,
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val yearLevelMenuExpanded: Boolean = false,
    val editId: Long? = null,
    val yearLevelOptions: List<String> = listOf("1st", "2nd", "3rd", "4th"),
    val qrcode: String = ""
)

sealed class StudentScreenEvent {
    data class OnFirstNameChanged(val value: String) : StudentScreenEvent()
    data class OnLastNameChanged(val value: String) : StudentScreenEvent()
    data class OnDepartmentChanged(val value: String) : StudentScreenEvent()
    data class OnCourseChanged(val value: String) : StudentScreenEvent()
    data class OnYearLevelChanged(val value: String) : StudentScreenEvent()
    data class OnBirthdayChanged(val value: String) : StudentScreenEvent()
    data class OnSearchQueryChanged(val value: String) : StudentScreenEvent()
    data class OnShowAllToggled(val value: Boolean) : StudentScreenEvent()
    data class OnPageChanged(val value: Int) : StudentScreenEvent()
    object OnSubmitStudent : StudentScreenEvent()
    data class OnEditStudent(val student: Student) : StudentScreenEvent()
    data class OnQRCodeChanged(val value: String) : StudentScreenEvent()
    data class OnDeleteStudent(val id: Long?) : StudentScreenEvent()
    object OnCancelEdit : StudentScreenEvent()
    data class OnFirstnameChange(val value: String) : StudentScreenEvent()
    data class OnLastnameChange(val value: String) : StudentScreenEvent()
    data class OnCourseChange(val value: String) : StudentScreenEvent()
    data class OnYearChange(val value: Long) : StudentScreenEvent()
    data class OnDepartmentChange(val value: String) : StudentScreenEvent()
}
