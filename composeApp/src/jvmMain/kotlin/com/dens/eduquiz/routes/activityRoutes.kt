package com.dens.eduquiz.routes

import CreateActivityRequest
import UpdateActivityRequest
import com.dens.eduquiz.repository.ActivityRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.activityRoutes(activityRepo: ActivityRepository) {

    route("/activities") {

        // CREATE ACTIVITY
        post {
            try {
                val request = call.receive<CreateActivityRequest>()
                val id = activityRepo.insertActivity(request)
                call.respond(
                    HttpStatusCode.Created,
                    mapOf("message" to "Activity created successfully", "id" to id)
                )
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to create activity", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET ALL ACTIVITIES
        get {
            try {
                val activities = activityRepo.getAllActivities()
                call.respond(HttpStatusCode.OK, activities)
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to fetch activities", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET ACTIVITY BY ID
        get("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid activity ID"))

                val activity = activityRepo.getActivityById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Activity not found"))

                call.respond(HttpStatusCode.OK, activity)
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to fetch activity", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET ACTIVITIES BY STATUS
        get("/status/{status}") {
            try {
                val status = call.parameters["status"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Status is required"))

                val list = activityRepo.getActivitiesByStatus(status)
                call.respond(HttpStatusCode.OK, list)
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to fetch activities by status", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // UPDATE ACTIVITY
        put("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

                val request = call.receive<UpdateActivityRequest>()
                activityRepo.updateActivity(id, request)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Activity updated successfully"))
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to update activity", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // DELETE ACTIVITY
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

                activityRepo.deleteActivity(id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Activity deleted successfully"))
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to delete activity", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GENERATE QR TOKEN (with studentId)
        post("{id}/qr/{studentId}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid activity ID"))
                val studentId = call.parameters["studentId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid student ID"))

                val token = java.util.UUID.randomUUID().toString()
                activityRepo.generateQrForActivity(id, token)

                val qrData = "eduquiz:/join?activityId=$id&studentId=$studentId&token=$token"

                call.respond(HttpStatusCode.OK, mapOf("message" to "QR generated", "qr_token" to token, "qr_data" to qrData))
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to generate QR", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET ACTIVITY BY QR (studentId included)
        get("{id}/qr/{token}/{studentId}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid activity ID"))
                val token = call.parameters["token"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Token is required"))
                val studentId = call.parameters["studentId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid student ID"))

                val activity = activityRepo.getActivityByQr(id, token)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Activity not found or QR expired"))

                // Clear QR after use
                activityRepo.clearQrForActivity(id)

                call.respond(HttpStatusCode.OK, mapOf(
                    "activity" to activity,
                    "studentId" to studentId
                ))
            } catch (e: Exception) {
                call.application.environment.log.error("Failed to fetch activity by QR", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
