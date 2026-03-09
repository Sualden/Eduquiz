package com.dens.eduquiz.ui

import ActivityItem
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dens.eduquiz.model.Student
import com.dens.eduquiz.viewmodel.AdminViewModel
import com.dens.eduquiz.viewmodel.StudentScreenViewModel
import com.dens.eduquiz.viewmodel.ActivityViewModel
import com.dens.eduquiz.ui.theme.*
import com.dens.eduquiz.viewmodel.QuestionViewModel
import com.dens.eduquiz.viewmodel.StudentActivityViewModel

// ... imports ...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    adminViewModel: AdminViewModel,
    studentViewModel: StudentScreenViewModel,
    activityViewModel: ActivityViewModel,
    questionViewModel: QuestionViewModel,
    studentActivityViewModel: StudentActivityViewModel,
    onLogout: () -> Unit,
    currentUser: Student?,
    onNavigateToStudentInfo: () -> Unit,
    onNavigateToManageActivity: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToQuestions: (ActivityItem) -> Unit
) {
    // --- APP STATE ---
    var activeTab by remember { mutableStateOf("dashboard") }
    var isSidebarOpen by remember { mutableStateOf(true) }
    var selectedActivityId by remember { mutableStateOf<Long?>(null) }
    var activityMembers by remember { mutableStateOf<List<ActivityMember>>(emptyList()) }
    var selectedActivity by remember { mutableStateOf<ActivityItem?>(null) }

    fun navigateTo(tab: String) {
        activeTab = tab
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            // ... (Your TopBar code remains exactly the same) ...
            // Keeping it brief here, use your existing TopBar code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(SurfaceColor.copy(alpha = 0.9f))
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isSidebarOpen = !isSidebarOpen }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Slate500)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Welcome back,", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text("Admin User", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate800)
                    }
                }

                Surface(shape = CircleShape, border = BorderStroke(1.dp, Slate100), color = SurfaceColor) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Brush.linearGradient(listOf(Indigo600, Color(0xFF818CF8))), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Admin", style = MaterialTheme.typography.labelMedium, color = Slate800)
                    }
                }
            }
        }
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Sidebar
            AnimatedVisibility(
                visible = isSidebarOpen,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Sidebar(activeTab, onNavigate = ::navigateTo, onLogout = onLogout)
            }

            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
                when (activeTab) {
                    "dashboard" -> DashboardScreen(
                        students = studentViewModel.studentList,
                        activities = activityViewModel.activityList
                    )

                    "students" -> StudentInfoScreen(
                        viewModel = studentViewModel,
                        onBack = { navigateTo("dashboard") },
                        onDeleteClick = { }
                    )

                    // FIX: Updated this section to pass ViewModels
                    "activities" -> {
                        // Ensure activities are loaded
                        LaunchedEffect(Unit) {
                            activityViewModel.loadActivities()
                            questionViewModel.loadQuestions()
                        }

                        ActivityScreen(
                            activityViewModel = activityViewModel,
                            questionViewModel = questionViewModel,
                            activityMembers = activityMembers,

                            // [NEW] Pass the logged-in user's ID
                            // Ensure 'currentUser' is the state variable holding your logged-in user
                        //    currentStudentId = currentUser?.id ?: 0L,

                            onManageMembers = { activity ->
                                selectedActivity = activity
                                activeTab = "activity-students"
                            },
                            onStartQuiz = { /* Handle quiz preview */ }
                        )
                    }

                    // --- UPDATED THIS SECTION ---
                    "activity-students" -> {
                        if (selectedActivity != null) {
                            ActivityStudentScreen(
                                activity = selectedActivity!!,
                                viewModel = studentActivityViewModel,
                                allStudents = studentViewModel.studentList,
                                onBack = {
                                    activeTab = "activities"
                                    selectedActivity = null
                                }
                            )
                        } else {
                            // Fallback
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No activity selected", color = Slate500)
                                Button(onClick = { activeTab = "activities" }) { Text("Go Back") }
                            }
                        }
                    }

                    "questions" -> {
                        LaunchedEffect(Unit) { questionViewModel.loadQuestions() }
                        GlobalQuestionBankScreen(
                            viewModel = questionViewModel,
                            activities = activityViewModel.activityList
                        )
                    }

                    "settings" -> SettingsScreen(
                        viewModel = adminViewModel,
                        onLogout = onLogout
                    )

                    else -> PlaceholderScreen("Coming Soon", Icons.Default.Build)
                }
            }
        }
    }
}
// --- SIDEBAR (Updated) ---
@Composable
fun Sidebar(activeTab: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(SurfaceColor)
            .border(BorderStroke(1.dp, Slate200))
            .padding(24.dp)
    ) {
        // Logo
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 40.dp)) {
            Box(
                modifier = Modifier.size(32.dp).background(Indigo600, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) { Text("E", color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Text("EduQuiz", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Slate800)
        }

        // Navigation Items
        SidebarItem("Home", Icons.Default.Home, activeTab == "dashboard") { onNavigate("dashboard") }
        SidebarItem("Manage Students", Icons.Default.Groups, activeTab == "students") { onNavigate("students") }
        SidebarItem("Manage Activities", Icons.Default.Description, activeTab == "activities") { onNavigate("activities") }

        // --- ADDED THIS ---
        SidebarItem("Manage Questions", Icons.AutoMirrored.Filled.List, activeTab == "questions") { onNavigate("questions") }

        Spacer(Modifier.weight(1f))
        Divider(color = Slate100, modifier = Modifier.padding(vertical = 16.dp))

        SidebarItem("Settings", Icons.Default.Settings, activeTab == "settings") { onNavigate("settings") }
        SidebarItem("Log out", Icons.Default.ExitToApp, false, isDestructive = true) { onLogout() }
    }
}

// --- HELPER COMPONENTS (Unchanged) ---

@Composable
fun SidebarItem(label: String, icon: ImageVector, isActive: Boolean, isDestructive: Boolean = false, onClick: () -> Unit) {
    val bgColor = if (isActive) Indigo50 else Color.Transparent
    val contentColor = if (isActive) Indigo600 else if (isDestructive)
        Color(0xFFEF4444) else Slate500

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = contentColor, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium)
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun DashboardScreen(students: List<com.dens.eduquiz.model.Student>, activities: List<ActivityItem>) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate800)
        Text("Overview of performance", style = MaterialTheme.typography.bodyMedium, color = Slate500)
        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            StatCard("Total Students", "${students.size}", Icons.Default.Groups, "Registered", Modifier.weight(1f))
            StatCard("Avg. Score", "N/A", Icons.Default.TrendingUp, "No Data", Modifier.weight(1f))
        }

        Spacer(Modifier.height(32.dp))
        Text("Recent Activities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate800)
        Spacer(Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor)) {
            Column(Modifier.padding(16.dp)) {
                if (activities.isEmpty()) {
                    Text("No recent activities", modifier = Modifier.padding(16.dp), color = Slate500)
                } else {
                    activities.take(3).forEach { activity ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(if (activity.status == "completed") Emerald600 else Amber600, CircleShape))
                                Spacer(Modifier.width(12.dp))
                                Text(activity.title, fontWeight = FontWeight.Medium, color = Slate800)
                            }
                            Text(activity.deadline.take(10), fontSize = 12.sp, color = Slate500)
                        }
                        Divider(color = Slate50)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, trend: String, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceColor)) {
        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.background(Indigo50, RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Icon(icon, null, tint = Indigo600)
                }
                Text(text = trend, color = Emerald600, fontSize = 12.sp, modifier = Modifier.background(Emerald50, CircleShape).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Slate800)
            Text(title, color = Slate500)
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, icon: ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = Slate200)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Slate500)
        Text("Coming Soon / Under Construction", style = MaterialTheme.typography.bodyMedium, color = Slate400)
    }
}