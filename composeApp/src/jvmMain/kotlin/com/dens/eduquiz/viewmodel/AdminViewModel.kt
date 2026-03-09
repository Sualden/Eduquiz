package com.dens.eduquiz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.dens.eduquiz.database.Admin
import com.dens.eduquiz.model.AdminDTO
import com.dens.eduquiz.model.LoginRequest
import com.dens.eduquiz.model.LoginResponse
import com.dens.eduquiz.repository.AdminRepository
import com.dens.eduquiz.utils.PasswordHasher
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class AdminUiState(
    val email: String = "admin",
    val password: String = "admin123",
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val loggedInAdmin: AdminDTO? = null,
    val token: String? = null,
    val adminList: List<Admin> = emptyList()
)
class AdminViewModel(
    private val client: HttpClient,
    val adminRepository: AdminRepository, // expose repo
    private val baseUrl: String = "http://10.0.2.2:2020"
) : ViewModel() {

    var uiState by mutableStateOf(AdminUiState())
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    // -------------------- LOGIN --------------------
    fun updateEmail(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun updatePassword(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun login(onResult: ((Boolean, String?) -> Unit)? = null) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        scope.launch {
            try {
                val response = client.post("$baseUrl/admin/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(uiState.email, uiState.password))
                }

                when (response.status) {
                    HttpStatusCode.OK -> {
                        val responseBody = response.body<LoginResponse>()
                        uiState = uiState.copy(
                            isLoginSuccessful = true,
                            loggedInAdmin = responseBody.admin,
                            token = responseBody.token,
                            isLoading = false
                        )
                        onResult?.invoke(true, null)
                        refreshAdminList()
                    }

                    HttpStatusCode.Unauthorized -> {
                        uiState = uiState.copy(
                            errorMessage = "Invalid credentials",
                            isLoading = false
                        )
                        onResult?.invoke(false, "Invalid credentials")
                    }

                    else -> {
                        uiState = uiState.copy(
                            errorMessage = "Error: ${response.status}",
                            isLoading = false
                        )
                        onResult?.invoke(false, "Error: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Network error: ${e.message}",
                    isLoading = false
                )
                e.printStackTrace()
                onResult?.invoke(false, e.message)
            }
        }
    }

    // -------------------- CRUD --------------------
    fun refreshAdminList() {
        scope.launch {
            val list = adminRepository.getAllAdmins()
            uiState = uiState.copy(adminList = list)
        }
    }

    fun deleteAdmin(adminId: Long, onResult: ((Boolean, String?) -> Unit)? = null) {
        scope.launch {
            try {
                val admin = adminRepository.getAdminById(adminId)
                if (admin != null) {
                    adminRepository.deleteAdmin(adminId)
                    refreshAdminList()
                    onResult?.invoke(true, null)
                } else {
                    onResult?.invoke(false, "Admin not found")
                }
            } catch (e: Exception) {
                onResult?.invoke(false, e.message)
            }
        }
    }

    fun addAdmin(fullname: String, email: String, password: String, role: String = "admin", onResult: ((Boolean, String?) -> Unit)? = null) {
        scope.launch {
            try {
                val hash = PasswordHasher.hash(password)
                adminRepository.insertAdmin(fullname, email, hash, role)
                refreshAdminList()
                onResult?.invoke(true, null)
            } catch (e: Exception) {
                onResult?.invoke(false, e.message)
            }
        }
    }

    fun updateAdmin(adminId: Long, fullname: String, email: String, password: String? = null, onResult: ((Boolean, String?) -> Unit)? = null) {
        scope.launch {
            try {
                val admin = adminRepository.getAdminById(adminId)
                if (admin != null) {
                    val hash = password?.let { PasswordHasher.hash(it) } ?: admin.passwordHash
                    adminRepository.updateAdmin(adminId, fullname, email, hash)
                    refreshAdminList()
                    onResult?.invoke(true, null)
                } else {
                    onResult?.invoke(false, "Admin not found")
                }
            } catch (e: Exception) {
                onResult?.invoke(false, e.message)
            }
        }
    }
}
