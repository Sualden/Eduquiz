package com.dens.eduquiz.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        // Use current directory instead of home directory
        val databasePath = File("AppDatabase.db")

        // Check if the file exists BEFORE creating the connection
        val exists = databasePath.exists()

        // Create JDBC driver
        val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")

        // Only create tables if the file is NEW
        if (!exists) {
            AppDatabase.Schema.create(driver)
        }

        return driver
    }
}
