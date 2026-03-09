package com.dens.eduquiz.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.dens.eduquiz.model.CreateAdminRequest
import com.dens.eduquiz.model.LoginRequest
import com.dens.eduquiz.model.LoginResponse
import com.dens.eduquiz.model.toDTO
import com.dens.eduquiz.repository.AdminRepository
import com.dens.eduquiz.utils.PasswordHasher
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(
    adminRepo: AdminRepository,
    jwtSecret: String,
    jwtIssuer: String,
    jwtAudience: String,
) {
    route("/admin") {

        // GET ALL ADMINS (for testing)
        get {
            val admins = adminRepo.getAllAdmins()
            call.respond(HttpStatusCode.OK, admins.map { it.toDTO() })
        }

        // LOGIN
        post("/login") {
            val request = call.receive<LoginRequest>()
            val admin = adminRepo.getAdminByEmail(request.email)

            if (admin == null || !PasswordHasher.verify(request.password, admin.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
                return@post
            }

            val token = JWT.create()
                .withClaim("id", admin.id)
                .withIssuer(jwtIssuer)
                .withAudience(jwtAudience)
                .sign(Algorithm.HMAC256(jwtSecret))

            call.respond(LoginResponse(token = token, admin = admin.toDTO()))
        }

        // CREATE NEW ADMIN
        post("/create") {
            val body = call.receive<CreateAdminRequest>()
            val passwordHash = PasswordHasher.hash(body.passwordHash)

            val id = adminRepo.insertAdmin(body.fullname, body.email, passwordHash, body.role)
            val admin = adminRepo.getAdminById(id)
            call.respond(HttpStatusCode.OK, admin!!.toDTO())
        }

        // TEST ROUTE
        get("/test") {
            call.respondText("Admin route is reachable!")
        }
    }
}
