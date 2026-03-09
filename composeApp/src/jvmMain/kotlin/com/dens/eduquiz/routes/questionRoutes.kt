package com.dens.eduquiz.routes

import CreateQuestionRequest
import UpdateQuestionRequest
import com.dens.eduquiz.repository.QuestionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.questionRoutes(questionRepo: QuestionRepository) {

    route("/questions") {


        post {
            try {
                val request = call.receive<CreateQuestionRequest>()
                val id = questionRepo.insertQuestion(request)

                call.respond(
                    HttpStatusCode.Created,
                    mapOf("message" to "Question created successfully", "id" to id)
                )

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        get {
            try {
                val questions = questionRepo.getAllQuestions()
                call.respond(HttpStatusCode.OK, questions)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        get("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid question ID"))

                val question = questionRepo.getQuestionById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Question not found"))

                call.respond(HttpStatusCode.OK, question)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        get("/activity/{activityId}") {
            try {
                val activityId = call.parameters["activityId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid activity ID"))

                val list = questionRepo.getQuestionsForActivity(activityId)
                call.respond(HttpStatusCode.OK, list)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        get("/status/{status}") {
            try {
                val status = call.parameters["status"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Status required"))

                val list = questionRepo.getQuestionsByStatus(status)
                call.respond(HttpStatusCode.OK, list)

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        put("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid question ID"))

                val request = call.receive<UpdateQuestionRequest>()
                questionRepo.updateQuestion(id, request)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Question updated successfully"))

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // -------------------------------------------------------------
        // DELETE QUESTION
        // -------------------------------------------------------------
        delete("{id}") {
            try {
                val id = call.parameters["id"]?.toLongOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid question ID"))

                questionRepo.deleteQuestion(id)

                call.respond(HttpStatusCode.OK, mapOf("message" to "Question deleted successfully"))

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // -------------------------------------------------------------
        // DELETE ALL QUESTIONS
        // -------------------------------------------------------------
        delete("/all") {
            try {
                questionRepo.deleteAllQuestions()

                call.respond(HttpStatusCode.OK, mapOf("message" to "All questions deleted"))

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}