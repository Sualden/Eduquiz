package com.dens.eduquiz.routes

import com.dens.eduquiz.model.*
import com.dens.eduquiz.repository.StudentActivityRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.studentActivityRoutes(studentActivityRepo: StudentActivityRepository) {

    route("/activity-students") {

        // ----------------------------------------------------------
        // ASSIGN STUDENT TO ACTIVITY
        // ----------------------------------------------------------
        post("/assign") {
            val request = call.receive<AssignStudentRequest>()
            val result = studentActivityRepo.addStudentToActivity(
                request.activityId,
                request.studentId
            )

            val response = if (result) {
                ActionResponse(true, "Student assigned successfully.")
            } else {
                ActionResponse(false, "Failed to assign student. Possibly already assigned.")
            }

            call.respond(response)
        }

        // ----------------------------------------------------------
        // REMOVE STUDENT FROM ACTIVITY
        // ----------------------------------------------------------
        delete("/remove") {
            val request = call.receive<RemoveStudentRequest>()
            val result = studentActivityRepo.removeStudentFromActivity(
                request.activityId,
                request.studentId
            )

            val response = if (result) {
                ActionResponse(true, "Student removed successfully.")
            } else {
                ActionResponse(false, "Failed to remove student.")
            }

            call.respond(response)
        }

        // ----------------------------------------------------------
        // GET STUDENTS BY ACTIVITY ID
        // ----------------------------------------------------------
        get("/students/{activityId}") {
            val activityId = call.parameters["activityId"]?.toLongOrNull()

            if (activityId == null) {
                call.respond(ActionResponse(false, "Invalid or missing activityId."))
                return@get
            }

            val students = studentActivityRepo.getStudentsByActivity(activityId)
                .map { it.toStudentInActivityDTO() }

            call.respond(
                ActivityStudentsResponse(
                    success = true,
                    activityId = activityId,
                    students = students
                )
            )
        }

        // ----------------------------------------------------------
        // GET ACTIVITIES BY STUDENT ID
        // ----------------------------------------------------------
        get("/activities/{studentId}") {
            val studentId = call.parameters["studentId"]?.toLongOrNull()

            if (studentId == null) {
                call.respond(ActionResponse(false, "Invalid or missing studentId."))
                return@get
            }

            val activities = studentActivityRepo.getActivitiesByStudent(studentId)
                .map { it.toActivityInStudentDTO() }

            call.respond(
                StudentActivitiesResponse(
                    success = true,
                    studentId = studentId,
                    activities = activities
                )
            )
        }
    }
}
