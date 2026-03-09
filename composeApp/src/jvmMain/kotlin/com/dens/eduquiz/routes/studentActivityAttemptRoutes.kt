package com.dens.eduquiz.routes

import com.dens.eduquiz.model.CreateActivityAttemptRequest
import com.dens.eduquiz.model.UpdateActivityAttemptRequest
import com.dens.eduquiz.repository.ActivityAttemptRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Route.activityAttemptRoutes(repository: ActivityAttemptRepository) {

    route("/activity-attempts") {

        // Create a new activity attempt
        post {
            try {
                val request = call.receive<CreateActivityAttemptRequest>()
                val attemptId = repository.insertActivityAttempt(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to attemptId))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Update an attempt (after completion)
        put("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val request = call.receive<UpdateActivityAttemptRequest>()
                repository.updateActivityAttempt(id, request)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Attempt updated successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Delete an attempt
        delete("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                repository.deleteActivityAttempt(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Attempt deleted successfully"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Get attempt by ID
        get("/{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

                val attempt = repository.getActivityAttemptById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, "Attempt not found")

                call.respond(attempt)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }

    // Get all attempts for a specific user
    get("/users/{userId}/activity-attempts") {
        try {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid userId")

            val attempts = repository.getAttemptsByUser(userId)
            call.respond(attempts)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
        }
    }

    // Get all attempts for a specific activity
    get("/activities/{activityId}/activity-attempts") {
        try {
            val activityId = call.parameters["activityId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid activityId")

            val attempts = repository.getAttemptsByActivity(activityId)
            call.respond(attempts)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
        }
    }
}
