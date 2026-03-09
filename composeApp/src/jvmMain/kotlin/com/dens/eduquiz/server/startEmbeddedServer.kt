package com.dens.eduquiz.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.dens.eduquiz.database.*
import com.dens.eduquiz.repository.ActivityAttemptRepository
import com.dens.eduquiz.repository.ActivityQuestionRepository
import com.dens.eduquiz.repository.ActivityRepository
import com.dens.eduquiz.repository.AdminRepository
import com.dens.eduquiz.repository.QuestionRepository
import com.dens.eduquiz.repository.StudentActivityRepository
import com.dens.eduquiz.repository.StudentAnswerRepository
import com.dens.eduquiz.repository.StudentRepository
import com.dens.eduquiz.repository.insertDefaultAdmin
import com.dens.eduquiz.routes.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun startEmbeddedServer(
    database: AppDatabase,
    port: Int = 2020,
    jwtSecret: String,
    jwtIssuer: String,
    jwtAudience: String,
    jwtRealm: String,
    testMode: Boolean = true
): EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration> {  // <-- return type
    val adminRepo = AdminRepository(database.adminQueries)
    //runBlocking { adminRepo.ensureDefaultAdmin() }
    insertDefaultAdmin(adminRepo)

    val server = embeddedServer(Netty, port) {
        configureServer(database, jwtSecret, jwtIssuer, jwtAudience, jwtRealm, testMode)
    }
    server.start(wait = false)
    println("Server running at: http://localhost:$port")
    return server  // <-- return engine instance
}


fun Application.module() {
    throw IllegalStateException("Use startEmbeddedServer() instead of module()")
}

private fun Application.configureServer(
    database: AppDatabase,
    jwtSecret: String,
    jwtIssuer: String,
    jwtAudience: String,
    jwtRealm: String,
    testMode: Boolean
) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            isLenient = true
        })
    }

    // CORS
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    // Logging
    install(CallLogging)

    // Error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Server error")
        }
    }

    // JWT Auth
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credentials ->
                val username = credentials.payload.getClaim("username").asString()
                if (username.isNullOrBlank()) null else JWTPrincipal(credentials.payload)
            }
        }
    }
// Initialize repositories
    val studentRepo = StudentRepository(database.studentQueries)
    val activityRepo = ActivityRepository(database.activityQueries)
    val questionRepo = QuestionRepository(database.questionQueries)
    val activityQuestionRepo = ActivityQuestionRepository(database.activityQuestionQueries)
    val attemptRepo = ActivityAttemptRepository(database.studentActivityAttemptQueries)
    val answerRepo = StudentAnswerRepository(database.studentAnswerQueries)
    val adminRepo = AdminRepository(database.adminQueries)
    val studentActivityRepo = StudentActivityRepository(database.studentActivityQueries)

    // Routing
    routing {
        get("/") { call.respondText("EduQuiz API is running!") }

        // Public admin routes
        adminRoutes(adminRepo, jwtSecret, jwtIssuer, jwtAudience)

        if (testMode) {
            // 🔓 Test Mode: no JWT required
            studentRoutes(studentRepo)
            questionRoutes(questionRepo)
            activityRoutes(activityRepo)
            activityQuestionRoutes(activityQuestionRepo, questionRepo)
            activityAttemptRoutes(attemptRepo)
            studentAnswerRoutes(answerRepo)
            studentActivityRoutes(studentActivityRepo)
        } else {
            // 🔐 Production: JWT protected
            authenticate("auth-jwt") {
                studentRoutes(studentRepo)
                questionRoutes(questionRepo)
                activityRoutes(activityRepo)
                activityQuestionRoutes(activityQuestionRepo,questionRepo)
                activityAttemptRoutes(attemptRepo)
                studentAnswerRoutes(answerRepo)
                studentActivityRoutes(studentActivityRepo)
            }
        }

    }
}
