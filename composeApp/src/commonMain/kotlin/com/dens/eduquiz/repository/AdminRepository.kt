package com.dens.eduquiz.repository

import com.dens.eduquiz.database.Admin
import com.dens.eduquiz.database.AdminQueries
import com.dens.eduquiz.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking

class AdminRepository(val queries: AdminQueries) {

    // Insert Admin
    suspend fun insertAdmin(
        fullname: String,
        email: String,
        passwordHash: String,
        role: String = "admin"
    ): Long = withContext(Dispatchers.IO) {
        queries.insertAdmin(fullname, email, passwordHash, role)
        queries.lastInsertRowId().executeAsOne()
    }

    // Get All Admins
    suspend fun getAllAdmins(): List<Admin> = withContext(Dispatchers.IO) {
        queries.selectAllAdmins().executeAsList()
    }

    // Find by Email
    suspend fun getAdminByEmail(email: String): Admin? = withContext(Dispatchers.IO) {
        queries.selectAdminByEmail(email).executeAsOneOrNull()
    }

    // Find by ID
    suspend fun getAdminById(id: Long): Admin? = withContext(Dispatchers.IO) {
        queries.selectAdminById(id).executeAsOneOrNull()
    }

    // Delete Admin
    suspend fun deleteAdmin(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteAdminById(id)
    }

    // Update Admin
    suspend fun updateAdmin(
        id: Long,
        fullname: String,
        email: String,
        passwordHash: String,
        role: String = "admin"
    ) = withContext(Dispatchers.IO) {
        queries.updateAdminById(fullname, email, passwordHash, role, id)
    }

    // LOGIN FUNCTION
    suspend fun login(email: String, password: String): Admin? = withContext(Dispatchers.IO) {
        val admin = queries.selectAdminByEmail(email).executeAsOneOrNull()
            ?: return@withContext null

        return@withContext if (PasswordHasher.verify(password, admin.passwordHash))
            admin
        else null
    }
}

// Insert default admin if none exists
fun insertDefaultAdmin(adminRepo: AdminRepository) = runBlocking {
    val existing = adminRepo.getAdminByEmail("admin")
    if (existing != null) return@runBlocking

    val passwordHash = PasswordHasher.hash("admin123")
    adminRepo.insertAdmin(
        fullname = "Default Admin",
        email = "admin",
        passwordHash = passwordHash,
        role = "admin"
    )
    println("Default admin inserted successfully.")
}
