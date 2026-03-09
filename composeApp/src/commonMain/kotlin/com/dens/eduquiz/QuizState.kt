package com.dens.eduquiz

import com.dens.eduquiz.database.Questions

data class QuizState(
    val questions: List<Questions> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val isFinished: Boolean = false
)
