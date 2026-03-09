package com.dens.eduquiz.routes

import CreateStudentAnswerRequest
import UpdateStudentAnswerRequest
import com.dens.eduquiz.repository.StudentAnswerRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Route.studentAnswerRoutes(repository: StudentAnswerRepository) {

    route("/student-answers") {

        // Create new student answer
        post {
            try {
                val request = call.receive<CreateStudentAnswerRequest>()
                val id = repository.insertStudentAnswer(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Update a student answer
        put("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val request = call.receive<UpdateStudentAnswerRequest>()
                repository.updateStudentAnswer(id, request)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Student answer updated"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Delete a student answer by ID
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                repository.deleteStudentAnswer(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Student answer deleted"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }

    // Delete all answers for a specific attempt
    delete("/attempts/{attemptId}/answers") {
        try {
            val attemptId = call.parameters["attemptId"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid attemptId")

            repository.deleteAnswersByAttempt(attemptId)
            call.respond(HttpStatusCode.OK, mapOf("message" to "Answers deleted for attempt"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
        }
    }

    // Get all answers for a specific attempt
    get("/attempts/{attemptId}/answers") {
        try {
            val attemptId = call.parameters["attemptId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid attemptId")

            val answers = repository.getAnswersByAttempt(attemptId)
            call.respond(answers)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
        }
    }

    // Get all answers for a specific student
    get("/students/{userId}/answers") {
        try {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid userId")

            val answers = repository.getAnswersByStudent(userId)
            call.respond(answers)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
        }
    }
}
