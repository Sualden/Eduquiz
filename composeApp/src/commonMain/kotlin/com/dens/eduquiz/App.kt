//package com.dens.eduquiz
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.dens.eduquiz.database.AppDatabase
//import com.dens.eduquiz.database.Questions
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import com.dens.eduquiz.Platform
//
//sealed class Screen {
//    object SubjectList : Screen()
//    data class Quiz(val subjectId: Long) : Screen()
//    data class Result(val score: Int, val total: Int) : Screen()
//}
//
//object AppState {
//    private val _currentScreen = MutableStateFlow<Screen>(Screen.SubjectList)
//    val currentScreen: StateFlow<Screen> = _currentScreen
//
//    fun navigateTo(screen: Screen) {
//        _currentScreen.value = screen
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun App(database: AppDatabase) {
//    val screen by AppState.currentScreen.collectAsState()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text("EduQuiz") })
//        }
//    ) { paddingValues ->
//        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
//            when (val s = screen) {
//                is Screen.SubjectList -> SubjectListScreen(database)
//                is Screen.Quiz -> QuizScreen(database, s.subjectId)
//                is Screen.Result -> ResultScreen(s.score, s.total)
//            }
//        }
//    }
//}
//
//@Composable
//fun SubjectListScreen(database: AppDatabase) {
//    val coroutineScope = rememberCoroutineScope()
//    var activities by remember { mutableStateOf(emptyList<com.dens.eduquiz.database.Activity>()) }
//
//    coroutineScope. launch {
//        activities = database.activityQueries.selectAllActivities().executeAsList()
//    }
//
//    LazyColumn {
//        items(activities) { activity ->
//            Button(
//                onClick = { AppState.navigateTo(Screen.Quiz(activity.id)) },
//                modifier = Modifier.fillMaxWidth().padding(8.dp)
//            ) {
//                Text(activity.title)
//            }
//        }
//    }
//}
//
//@Composable
//fun QuizScreen(database: AppDatabase, subjectId: Long) {
//    val coroutineScope = rememberCoroutineScope()
//    var quizState by remember { mutableStateOf(QuizState()) }
//
//    coroutineScope.launch {
//        val questions = database.activityQuestionQueries.selectQuestionsForActivityFromJoin(subjectId).executeAsList()
//        quizState = quizState.copy(questions = questions)
//    }
//
//    if (quizState.questions.isNotEmpty()) {
//        val currentQuestion = quizState.questions[quizState.currentQuestionIndex]
//
//        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//            Text(currentQuestion.text, style = MaterialTheme.typography.headlineSmall)
//
//            val options = listOf(currentQuestion.a, currentQuestion.b, currentQuestion.c, currentQuestion.d)
//            options.forEachIndexed { index, option ->
//                Button(
//                    onClick = {
//                        val isCorrect = index.toString() == currentQuestion.correct
//                        val newScore = if (isCorrect) quizState.score + 1 else quizState.score
//                        val nextIndex = quizState.currentQuestionIndex + 1
//
//                        if (nextIndex < quizState.questions.size) {
//                            quizState = quizState.copy(
//                                score = newScore,
//                                currentQuestionIndex = nextIndex
//                            )
//                        } else {
//                            quizState = quizState.copy(score = newScore, isFinished = true)
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
//                ) {
//                    Text(option)
//                }
//            }
//        }
//    } else {
//        Text("Loading questions...")
//    }
//
//    if (quizState.isFinished) {
//        if (getPlatform().name == "Desktop") {
//            AppState.navigateTo(Screen.Result(quizState.score, quizState.questions.size))
//        } else {
//            AppState.navigateTo(Screen.SubjectList)
//        }
//    }
//}
//
//@Composable
//fun ResultScreen(score: Int, total: Int) {
//    Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text("Quiz Finished!", style = MaterialTheme.typography.headlineMedium)
//        Text("Your score: $score / $total", style = MaterialTheme.typography.bodyLarge)
//        Button(onClick = { AppState.navigateTo(Screen.SubjectList) }) {
//            Text("Back to Subjects")
//        }
//    }
//}
