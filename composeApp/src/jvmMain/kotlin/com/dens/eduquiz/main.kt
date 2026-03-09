package com.dens.eduquiz

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.dens.eduquiz.database.DatabaseHelper
import com.dens.eduquiz.database.DriverFactory
import com.dens.eduquiz.database.createDatabase
import com.dens.eduquiz.model.Student
import com.dens.eduquiz.repository.AdminRepository
import com.dens.eduquiz.server.startEmbeddedServer
import com.dens.eduquiz.ui.HomeScreen
import com.dens.eduquiz.ui.LoginScreen
import com.dens.eduquiz.viewmodel.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.json.Json
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@OptIn(DelicateCoroutinesApi::class)
fun main() = application {

    // ---------------- DATABASE & SERVER ----------------
    val driverFactory = DriverFactory()
    DatabaseHelper.init(driverFactory)

    val database = createDatabase(driverFactory)
    val adminRepository = AdminRepository(database.adminQueries)

    val port = 2020
    val serverStarted = CountDownLatch(1)

    lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

    thread {
        server = startEmbeddedServer(
            database = database,
            port = port,
            jwtSecret = "super-secret",
            jwtIssuer = "eduquiz",
            jwtAudience = "eduquiz-client",
            jwtRealm = "eduquiz-realm"
        )
        serverStarted.countDown()
    }

    serverStarted.await()

    // ---------------- HTTP CLIENT ----------------
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    // ---------------- WINDOW ----------------
    Window(
        onCloseRequest = ::exitApplication,
        title = "EduQuiz",
        state = rememberWindowState(width = 1280.dp, height = 800.dp)
    ) {

        // GLOBAL AUTH STATE
        var isLoggedIn by remember { mutableStateOf(false) }
        var currentUser by remember { mutableStateOf<Student?>(null) }
        // ---------------- VIEWMODELS ----------------
        val adminViewModel = remember {
            AdminViewModel(
                client = httpClient,
                baseUrl = "http://localhost:$port",
                adminRepository = adminRepository
            )
        }

        val studentViewModel = remember {
            StudentScreenViewModel("http://localhost:$port")
        }

        val activityViewModel = remember {
            ActivityViewModel(DatabaseHelper.activityRepository())
        }

        val questionViewModel = remember {
            QuestionViewModel(DatabaseHelper.questionRepository())
        }

        val studentActivityViewModel = remember {
            StudentActivityViewModel(DatabaseHelper.StudentActivityRepository())
        }

        // ---------------- UI FLOW ----------------
        if (isLoggedIn) {
            HomeScreen(
                adminViewModel = adminViewModel,
                studentViewModel = studentViewModel,
                activityViewModel = activityViewModel,
                questionViewModel = questionViewModel,
                studentActivityViewModel = studentActivityViewModel,

                // [FIX] Pass the currentUser state here
                currentUser = currentUser,

                onLogout = {
                    isLoggedIn = false
                    currentUser = null
                },
                onNavigateToStudentInfo = { /* handled inside Home */ },
                onNavigateToManageActivity = { /* handled inside Home */ },
                onNavigateToSettings = { /* handled inside Home */ },
                onNavigateToQuestions = { /* handled inside Home */ }
            )
        } else {
            LoginScreen(
                viewModel = adminViewModel,
                onAdminLoginSuccess = { isLoggedIn = true }
            )
        }
    }
}
