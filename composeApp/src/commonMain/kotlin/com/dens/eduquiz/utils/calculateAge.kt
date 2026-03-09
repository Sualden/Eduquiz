package com.dens.eduquiz.utils

import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

fun calculateAge(birthday: String): Int {
    // 1. Safety check: Return 0 immediately if empty
    if (birthday.isBlank()) return 0

    return try {
        // 2. Wrap parsing in try-catch to prevent server crash on bad dates
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val birthDate = sdf.parse(birthday) ?: return 0

        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply { time = birthDate }

        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        age
    } catch (_: Exception) {
        // Return 0 instead of crashing the whole request
        0
    }
}