package com.dens.eduquiz.database

import app.cash.sqldelight.db.SqlDriver
import com.dens.eduquiz.repository.ActivityAttemptRepository
import com.dens.eduquiz.repository.ActivityRepository
import com.dens.eduquiz.repository.ActivityQuestionRepository
import com.dens.eduquiz.repository.AdminRepository
import com.dens.eduquiz.repository.QuestionRepository
import com.dens.eduquiz.repository.StudentAnswerRepository
import com.dens.eduquiz.repository.StudentRepository
import com.dens.eduquiz.repository.StudentActivityRepository

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): AppDatabase {
    val driver = driverFactory.createDriver()
    return AppDatabase(driver)
}

object DatabaseHelper {

    private var driver: SqlDriver? = null
    private var database: AppDatabase? = null

    /** Initialize using a driver instance */
    fun init(driver: SqlDriver) {
        if (this.driver == null) {
            this.driver = driver
            this.database = AppDatabase(driver)
        }
    }

    /** Initialize using the platform-specific DriverFactory */
    fun init(driverFactory: DriverFactory) {
        if (this.driver == null) {
            val driver = driverFactory.createDriver()
            this.driver = driver
            this.database = AppDatabase(driver)
        }
    }

    private fun db(): AppDatabase =
        database ?: error("Database not initialized. Call DatabaseHelper.init().")

    // ---------------------------------------------------
    // Repositories (MATCHING YOUR NEW SCHEMA COMPLETELY)
    // ---------------------------------------------------

    fun adminRepository() =
        AdminRepository(db().adminQueries)

    fun studentRepository() =
        StudentRepository(db().studentQueries)

    fun questionRepository() =
        QuestionRepository(db().questionQueries)

    fun activityRepository() =
        ActivityRepository(db().activityQueries)

    fun activityQuestionRepository() =
        ActivityQuestionRepository(db().activityQuestionQueries)

    fun activityAttemptRepository() =
        ActivityAttemptRepository(db().studentActivityAttemptQueries)

    fun studentAnswerRepository() =
        StudentAnswerRepository(db().studentAnswerQueries)

    fun StudentActivityRepository() =
        StudentActivityRepository(db().studentActivityQueries)
}
