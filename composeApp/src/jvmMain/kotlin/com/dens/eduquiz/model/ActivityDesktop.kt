// jvmMain
package com.dens.eduquiz.model

import com.dens.eduquiz.database.Activity

data class ActivityDesktop(
    val activity: Activity,
    var timeLeftMillis: Long = 0L,
    var isRunning: Boolean = false
)

// Extension to convert shared model → JVM model
fun Activity.toDesktopModel(): ActivityDesktop {
    val millisLeft = this.deadline?.let { deadlineStr ->
        try {
            val deadlineTime = java.time.Instant.parse(deadlineStr).toEpochMilli()
            (deadlineTime - System.currentTimeMillis()).coerceAtLeast(0L)
        } catch (e: Exception) {
            0L
        }
    } ?: 0L

    return ActivityDesktop(
        activity = this,
        timeLeftMillis = millisLeft,
        isRunning = millisLeft > 0
    )
}
