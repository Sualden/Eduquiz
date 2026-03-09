package com.dens.eduquiz.routes

import com.dens.eduquiz.model.CreateStudentRequest
import com.dens.eduquiz.model.UpdateStudentRequest
import com.dens.eduquiz.repository.StudentRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.studentRoutes(studentRepo: StudentRepository) {

    route("/students") {

        // -------------------------------------------------------------
        // CREATE STUDENT
        // -------------------------------------------------------------
        post("/insert") {
            try {
                val request = call.receive<CreateStudentRequest>()
                val id = studentRepo.insertStudent(request)

                call.respond(
                    HttpStatusCode.Created,
                    mapOf(
                        "message" to "Student inserted successfully",
                        "id" to id
                    )
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e.message)
                )
            }
        }
        // -------------------------------------------------------------
        // GET ALL STUDENTS
        // -------------------------------------------------------------
        get {
            try {
                val students = studentRepo.getAllStudents()
                call.respond(HttpStatusCode.OK, students)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e.message)
                )
            }
        }

        // -------------------------------------------------------------
        // GET STUDENT BY ID
        // -------------------------------------------------------------
        get("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid student ID")
                    )

                val student = studentRepo.getStudentById(id)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Student not found")
                    )

                call.respond(HttpStatusCode.OK, student)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // -------------------------------------------------------------
        // GET STUDENT BY QR CODE
        // -------------------------------------------------------------
        get("/qr/{qrcode}") {
            try {
                val qr = call.parameters["qrcode"]
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "QR code is missing")
                    )

                val student = studentRepo.getStudentByQr(qr)
                    ?: return@get call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Student with this QR not found")
                    )

                call.respond(HttpStatusCode.OK, student)

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e.message)
                )
            }
        }

        // -------------------------------------------------------------
        // UPDATE STUDENT
        // -------------------------------------------------------------
        put("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid student ID")
                    )

                val request = call.receive<UpdateStudentRequest>()

                studentRepo.updateStudent(id, request)

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Student updated successfully")
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e.message)
                )
            }
        }

        // -------------------------------------------------------------
        // DELETE STUDENT
        // -------------------------------------------------------------
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid student ID")
                    )

                studentRepo.deleteStudent(id)

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "Student deleted successfully")
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e.message)
                )
            }
        }
    }
}
