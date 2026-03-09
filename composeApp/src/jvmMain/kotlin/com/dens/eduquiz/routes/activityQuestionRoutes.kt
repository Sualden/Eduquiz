package com.dens.eduquiz.routes

import com.dens.eduquiz.repository.ActivityQuestionRepository
import com.dens.eduquiz.repository.QuestionRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

data class AddQuestionToActivityRequest(
    val questionId: Long
)

fun Route.activityQuestionRoutes(
    activityQuestionRepo: ActivityQuestionRepository,
    questionRepo: QuestionRepository
) {

    route("/activities/{activityId}/questions") {

        // Add question to activity
        post {
            try {
                val activityId = call.parameters["activityId"]?.toLongOrNull()
                val body = call.receive<AddQuestionToActivityRequest>()

                if (activityId == null)
                    return@post call.respond(HttpStatusCode.BadRequest, "Invalid activityId")

                // Ensure the question exists
                val question = questionRepo.getQuestionById(body.questionId)
                    ?: return@post call.respond(HttpStatusCode.NotFound, "Question not found")

                activityQuestionRepo.insertActivityQuestion(activityId, body.questionId)

                call.respond(
                    HttpStatusCode.Created,
                    mapOf("message" to "Question added to activity")
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Get all questions for an activity
        get {
            try {
                val activityId = call.parameters["activityId"]?.toLongOrNull()
                if (activityId == null)
                    return@get call.respond(HttpStatusCode.BadRequest, "Invalid activityId")

                val questions = activityQuestionRepo.getQuestionsForActivity(activityId)
                call.respond(questions)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Remove a single question from an activity
        delete("/{questionId}") {
            try {
                val activityId = call.parameters["activityId"]?.toLongOrNull()
                val questionId = call.parameters["questionId"]?.toLongOrNull()

                if (activityId == null || questionId == null)
                    return@delete call.respond(HttpStatusCode.BadRequest, "Invalid IDs")

                activityQuestionRepo.deleteSingleQuestion(activityId, questionId)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Question removed from activity"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // Remove all questions from an activity
        delete {
            try {
                val activityId = call.parameters["activityId"]?.toLongOrNull()
                if (activityId == null)
                    return@delete call.respond(HttpStatusCode.BadRequest, "Invalid activityId")

                activityQuestionRepo.deleteQuestionsFromActivity(activityId)

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("message" to "All questions removed from activity")
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }
}
