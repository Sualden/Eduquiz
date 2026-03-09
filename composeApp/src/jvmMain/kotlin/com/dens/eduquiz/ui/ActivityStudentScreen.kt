package com.dens.eduquiz.ui

import ActivityItem
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistAdd // Optional icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dens.eduquiz.model.AssignStudentRequest
import com.dens.eduquiz.model.RemoveStudentRequest
import com.dens.eduquiz.model.Student
import com.dens.eduquiz.ui.theme.PrimaryColor
import com.dens.eduquiz.ui.theme.Red500
import com.dens.eduquiz.ui.theme.Slate100
import com.dens.eduquiz.ui.theme.Slate200
import com.dens.eduquiz.ui.theme.Slate500
import com.dens.eduquiz.ui.theme.Slate800
import com.dens.eduquiz.ui.theme.SurfaceColor
import com.dens.eduquiz.viewmodel.StudentActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityStudentScreen(
    activity: ActivityItem,
    viewModel: StudentActivityViewModel,
    allStudents: List<Student>,
    onBack: () -> Unit
) {
    // 1. Fetch data
    LaunchedEffect(activity.id) {
        viewModel.fetchStudentsByActivity(activity.id)
    }

    // 2. State
    val studentsInActivityDTO = viewModel.studentsInActivity
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // 3. Logic to separate Enrolled vs Available students
    val enrolledStudents = allStudents.filter { student ->
        studentsInActivityDTO.any { it.id == student.id }
    }

    val availableStudents = allStudents.filter { student ->
        studentsInActivityDTO.none { it.id == student.id }
    }

    fun handleAssign() {
        if (selectedStudent == null) {
            errorMessage = "Please select a student first."
            return
        }

        viewModel.assignStudent(
            AssignStudentRequest(activity.id, selectedStudent!!.id!!),
            onComplete = { success, msg ->
                if (success) {
                    selectedStudent = null
                    errorMessage = null
                } else {
                    errorMessage = msg
                }
            }
        )
    }

    // --- NEW HANDLER: Assign All ---
    fun handleAssignAll() {
        if (availableStudents.isEmpty()) return

        viewModel.assignAllStudents(activity.id, availableStudents) {
            // Optional: Show success message or reset state
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // --- Header ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate500)
            }
            Text("Back to Activities", color = Slate500, fontWeight = FontWeight.Medium)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(activity.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate800)
                Text("Manage Enrolled Students", color = Slate500)
            }
        }

        // --- Assign Section ---
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Assign Student", fontWeight = FontWeight.Bold, color = Slate800)

                    // --- ASSIGN ALL BUTTON ---
                    if (availableStudents.isNotEmpty()) {
                        TextButton(onClick = { handleAssignAll() }, enabled = !viewModel.isLoading) {
                            Text("Assign All (${availableStudents.size})")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Dropdown Menu
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        val displayText = selectedStudent?.let { "${it.firstname} ${it.lastname} (${it.course})" } ?: ""

                        OutlinedTextField(
                            value = displayText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Student") },
                            placeholder = { Text("Choose a student...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = Slate200
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            isError = errorMessage != null
                        )

                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            if (availableStudents.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No available students", color = Slate500) },
                                    onClick = { isDropdownExpanded = false }
                                )
                            } else {
                                availableStudents.forEach { student ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${student.firstname} ${student.lastname}", fontWeight = FontWeight.Bold)
                                                Text("${student.course} - Year ${student.yearlevel}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                            }
                                        },
                                        onClick = {
                                            selectedStudent = student
                                            isDropdownExpanded = false
                                            errorMessage = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { handleAssign() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp),
                        enabled = !viewModel.isLoading
                    ) {
                        Text(if (viewModel.isLoading) "..." else "Assign")
                    }
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = Red500, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // --- Enrolled Students List ---
        Text(
            "Students in Activity (${enrolledStudents.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Slate800
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            if (enrolledStudents.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(color = PrimaryColor)
                    } else {
                        Text("No students assigned yet.", color = Slate500)
                    }
                }
            } else {
                LazyColumn {
                    items(enrolledStudents) { student ->
                        ListItem(
                            headlineContent = { Text("${student.firstname} ${student.lastname}", fontWeight = FontWeight.Bold, color = Slate800) },
                            supportingContent = { Text("${student.course} - ${student.yearlevel}", color = Slate500) },
                            trailingContent = {
                                IconButton(onClick = {
                                    student.id?.let { id ->
                                        viewModel.removeStudent(RemoveStudentRequest(activity.id, id))
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, "Remove", tint = Slate500)
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                        )
                        Divider(color = Slate100)
                    }
                }
            }
        }
    }
}