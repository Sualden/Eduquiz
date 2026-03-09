package com.dens.eduquiz

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.dens.eduquiz.ui.ScanActivityScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    ScanActivityScreen(
                        onJoinActivity = { activityId ->
                            // 1. Logic to handle the joined activity
                            // For now, let's just show a Toast message
                            Toast.makeText(
                                this@MainActivity,
                                "Joined Activity ID: $activityId",
                                Toast.LENGTH_LONG
                            ).show()

                            // 2. TODO: Navigate to your Quiz Screen here
                            // Example: navController.navigate("quiz_screen/$activityId")
                        }
                    )
                }
            }
        }
    }
}