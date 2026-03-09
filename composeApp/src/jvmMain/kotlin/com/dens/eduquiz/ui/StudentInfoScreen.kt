package com.dens.eduquiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.dens.eduquiz.model.Student
import com.dens.eduquiz.ui.theme.BgColor
import com.dens.eduquiz.ui.theme.Indigo50
import com.dens.eduquiz.ui.theme.Indigo600
import com.dens.eduquiz.ui.theme.Indigo700
import com.dens.eduquiz.ui.theme.Red500
import com.dens.eduquiz.ui.theme.Slate100
import com.dens.eduquiz.ui.theme.Slate200
import com.dens.eduquiz.ui.theme.Slate400
import com.dens.eduquiz.ui.theme.Slate50
import com.dens.eduquiz.ui.theme.Slate500
import com.dens.eduquiz.ui.theme.Slate800
import com.dens.eduquiz.ui.theme.SurfaceColor
import com.dens.eduquiz.viewmodel.StudentScreenEvent
import com.dens.eduquiz.viewmodel.StudentScreenState
import com.dens.eduquiz.viewmodel.StudentScreenViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentInfoScreen(
    viewModel: StudentScreenViewModel,
    onBack: () -> Unit,
    onDeleteClick: (Student) -> Unit
) {
    val students = viewModel.studentList
    val uiState = viewModel.uiState

    var currentView by remember { mutableStateOf("list") }
    var searchQuery by remember { mutableStateOf("") }
    var qrStudent by remember { mutableStateOf<Student?>(null) }

    fun startAdd() {
        viewModel.onEvent(StudentScreenEvent.OnCancelEdit)
        currentView = "form"
    }

    fun startEdit(student: Student) {
        viewModel.onEvent(StudentScreenEvent.OnEditStudent(student))
        currentView = "form"
    }

    Scaffold(containerColor = BgColor) { padding ->
        Row(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(32.dp)
            ) {
                // Top Header
                HeaderSection(onBackClick = onBack)

                Spacer(modifier = Modifier.height(32.dp))

                if (currentView == "list") {
                    val filteredStudents = students.filter {
                        it.firstname.contains(searchQuery, ignoreCase = true) ||
                                it.lastname.contains(searchQuery, ignoreCase = true) ||
                                it.course.contains(searchQuery, ignoreCase = true)
                    }

                    StudentListView(
                        students = filteredStudents,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onAddClick = { startAdd() },
                        onEditClick = { startEdit(it) },
                        onDeleteClick = { student ->
                            student.id?.let { id ->
                                viewModel.onEvent(StudentScreenEvent.OnDeleteStudent(id))
                            }
                        },
                        onQrClick = { qrStudent = it }
                    )

                } else {
                    StudentFormView(
                        state = uiState,
                        onEvent = viewModel::onEvent,
                        onSave = {
                            viewModel.onEvent(StudentScreenEvent.OnSubmitStudent)
                            currentView = "list"
                        },
                        onCancel = {
                            viewModel.onEvent(StudentScreenEvent.OnCancelEdit)
                            currentView = "list"
                        }
                    )
                }
            }
        }
    }

    if (qrStudent != null) {
        QrCodeDialog(student = qrStudent!!, onDismiss = { qrStudent = null })
    }
}

// ================= FORM VIEW (UPDATED) =================

@Composable
fun StudentFormView(
    state: StudentScreenState,
    onEvent: (StudentScreenEvent) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // --- Predefined Options (You can also move these to ViewModel) ---
    val departmentOptions = listOf("College of Technology", "College of Engineering", "College of Arts & Sciences", "College of Education")
    val courseOptions = listOf("BSIT", "BSCS", "BSCpE", "BSEd", "BSPsych")
    val yearLevelOptions = listOf("1", "2", "3", "4", "5")

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        TextButton(onClick = onCancel, contentPadding = PaddingValues(0.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Back to Student List", color = Slate500)
        }

        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.background(Slate50).padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (state.isEditing) "Edit Student" else "Add New Student",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Text("Enter the student's academic information below.", color = Slate500, fontSize = 14.sp)
                    }
                    Box(modifier = Modifier.size(40.dp).background(Indigo50, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Indigo600)
                    }
                }

                HorizontalDivider(color = Slate200)

                Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Name Fields (Standard Text Inputs)
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        InputGroup("First Name", state.firstname, { onEvent(StudentScreenEvent.OnFirstNameChanged(it)) }, "e.g. Juan", Modifier.weight(1f))
                        InputGroup("Last Name", state.lastname, { onEvent(StudentScreenEvent.OnLastNameChanged(it)) }, "e.g. Dela Cruz", Modifier.weight(1f))
                    }

                    // Department (Dropdown)
                    DropdownInputGroup(
                        label = "Department",
                        value = state.department,
                        options = departmentOptions,
                        onValueChange = { onEvent(StudentScreenEvent.OnDepartmentChanged(it)) },
                        icon = Icons.Default.Business
                    )

                    // Course & Year (Dropdowns)
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        DropdownInputGroup(
                            label = "Course",
                            value = state.course,
                            options = courseOptions,
                            onValueChange = { onEvent(StudentScreenEvent.OnCourseChanged(it)) },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownInputGroup(
                            label = "Year Level",
                            value = state.yearlevel,
                            options = yearLevelOptions,
                            onValueChange = { onEvent(StudentScreenEvent.OnYearLevelChanged(it)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Birthday (Calendar Picker)
                    DateInputGroup(
                        label = "Birthday",
                        value = state.birthday,
                        onDateSelected = { onEvent(StudentScreenEvent.OnBirthdayChanged(it)) }
                    )
                }

                HorizontalDivider(color = Slate200)

                Row(
                    modifier = Modifier.background(Slate50).padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate500)
                    ) { Text("Cancel") }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.isEditing) "Update Student" else "Save Student")
                    }
                }
            }
        }
    }
}

// ================= NEW HELPERS =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownInputGroup(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
        )
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select $label", color = Slate400) },
                leadingIcon = if(icon != null) { { Icon(icon, null, tint = Slate400) } } else null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo600,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor
                ),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceColor)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputGroup(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("YYYY-MM-DD", color = Slate400) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarToday, null, tint = Slate400)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Indigo600,
                unfocusedBorderColor = Slate200,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            ),
            singleLine = true
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        onDateSelected(date.toString())
                    }
                    showDatePicker = false
                }) { Text("OK", color = Indigo600) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Slate500) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ================= EXISTING HELPERS =================

@Composable
fun InputGroup(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Slate400) },
            trailingIcon = if (icon != null) { { Icon(icon, null, tint = Slate400) } } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Indigo600,
                unfocusedBorderColor = Slate200,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            ),
            singleLine = true
        )
    }
}
// ================= SUB-COMPOSABLES =================

@Composable
fun HeaderSection(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Back Button + Titles
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Slate800,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text("Manage Students", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate800)
                Text("Add, edit, or remove student records", color = Slate500)
            }
        }

        // Right Side: Admin Pill
        Surface(shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).background(Brush.linearGradient(listOf(Indigo600, Indigo700)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text("Admin", style = MaterialTheme.typography.labelLarge, color = Slate800)
            }
        }
    }
}
@Composable
fun StudentListView(
    students: List<Student>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Student) -> Unit,
    onDeleteClick: (Student) -> Unit,
    onQrClick: (Student) -> Unit
) {
    Column {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                textStyle = TextStyle(fontSize = 14.sp, color = Slate800),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .width(320.dp)
                            .background(SurfaceColor, RoundedCornerShape(12.dp))
                            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Slate400, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        if (searchQuery.isEmpty()) Text("Search...", color = Slate400, fontSize = 14.sp)
                        innerTextField()
                    }
                }
            )

            // Add Button
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Student")
            }
        }

        Spacer(Modifier.height(24.dp))

        // Table / List
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            LazyColumn {
                item {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .background(Slate50)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("STUDENT NAME", Modifier.weight(2.5f), color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("DEPARTMENT", Modifier.weight(2f), color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("COURSE & YEAR", Modifier.weight(1.5f), color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("ACTIONS", Modifier.weight(1.2f), color = Slate500, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                    HorizontalDivider(color = Slate200)
                }

                // FIX: Use elvis operator ?: 0 to handle nullable ID in key
                items(students, key = { it.id ?: 0 }) { student ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Name + Avatar
                        Row(Modifier.weight(2.5f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Indigo50, CircleShape)
                                    .border(1.dp, Indigo50, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${student.firstname.firstOrNull() ?: ""}${student.lastname.firstOrNull() ?: ""}",
                                    color = Indigo600,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${student.lastname}, ${student.firstname}", color = Slate800, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                // FIX: Handle nullable ID in math operation
                                Text("ID: 2023-${1000 + (student.id ?: 0)}", color = Slate400, fontSize = 12.sp)
                            }
                        }

                        // Department
                        Text(student.department, Modifier.weight(2f), color = Slate500, fontSize = 14.sp)

                        // Course
                        Column(Modifier.weight(1.5f)) {
                            Text(student.course, color = Slate800, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Year ${student.yearlevel} • Age ${student.age}", color = Slate400, fontSize = 12.sp)
                        }

                        // Actions
                        Row(Modifier.weight(1.2f), horizontalArrangement = Arrangement.End) {
                            ActionIcon(Icons.Default.QrCode, "QR") { onQrClick(student) }
                            ActionIcon(Icons.Default.Edit, "Edit") { onEditClick(student) }
                            ActionIcon(Icons.Default.Delete, "Delete", isDestructive = true) { onDeleteClick(student) }
                        }
                    }
                    HorizontalDivider(color = Slate50)
                }
            }
        }
    }
}

//@Composable
//fun StudentFormView(
//    state: StudentScreenState,
//    onEvent: (StudentScreenEvent) -> Unit,
//    onSave: () -> Unit,
//    onCancel: () -> Unit
//) {
//    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
//        // Back Button
//        TextButton(onClick = onCancel, contentPadding = PaddingValues(0.dp)) {
//            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
//            Spacer(Modifier.width(8.dp))
//            Text("Back to Student List", color = Slate500)
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        // Form Card
//        Card(
//            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
//            shape = RoundedCornerShape(16.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Column {
//                // Form Header
//                Row(
//                    modifier = Modifier.background(Slate50).padding(24.dp).fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Column {
//                        Text(
//                            text = if (state.isEditing) "Edit Student" else "Add New Student",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold,
//                            color = Slate800
//                        )
//                        Text("Enter the student's academic information below.", color = Slate500, fontSize = 14.sp)
//                    }
//                    Box(modifier = Modifier.size(40.dp).background(Indigo50, CircleShape), contentAlignment = Alignment.Center) {
//                        Icon(Icons.Default.Person, null, tint = Indigo600)
//                    }
//                }
//
//                HorizontalDivider(color = Slate200)
//
//                // Inputs
//                Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
//                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
//                        InputGroup("First Name", state.firstname, { onEvent(StudentScreenEvent.OnFirstNameChanged(it)) }, "e.g. Juan", Modifier.weight(1f))
//                        InputGroup("Last Name", state.lastname, { onEvent(StudentScreenEvent.OnLastNameChanged(it)) }, "e.g. Dela Cruz", Modifier.weight(1f))
//                    }
//
//                    InputGroup("Department", state.department, { onEvent(StudentScreenEvent.OnDepartmentChanged(it)) }, "e.g. College of Science", icon = Icons.Default.Business)
//
//                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
//                        InputGroup("Course", state.course, { onEvent(StudentScreenEvent.OnCourseChanged(it)) }, "e.g. BSCS", Modifier.weight(1f))
//                        InputGroup("Year Level", state.yearlevel, { onEvent(StudentScreenEvent.OnYearLevelChanged(it)) }, "e.g. 3", Modifier.weight(1f))
//                    }
//
//                    InputGroup("Birthday", state.birthday, { onEvent(StudentScreenEvent.OnBirthdayChanged(it)) }, "YYYY-MM-DD", icon = Icons.Default.CalendarToday)
//                }
//
//                HorizontalDivider(color = Slate200)
//
//                // Footer Actions
//                Row(
//                    modifier = Modifier.background(Slate50).padding(24.dp).fillMaxWidth(),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    OutlinedButton(
//                        onClick = onCancel,
//                        shape = RoundedCornerShape(12.dp),
//                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
//                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate500)
//                    ) {
//                        Text("Cancel")
//                    }
//                    Spacer(Modifier.width(12.dp))
//                    Button(
//                        onClick = onSave,
//                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
//                        shape = RoundedCornerShape(12.dp)
//                    ) {
//                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
//                        Spacer(Modifier.width(8.dp))
//                        Text(if (state.isEditing) "Update Student" else "Save Student")
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun QrCodeDialog(student: Student, onDismiss: () -> Unit) {
    val context = LocalPlatformContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.width(360.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Slate50).padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Student QR Code", fontWeight = FontWeight.Bold, color = Slate800)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Slate400) }
                }

                HorizontalDivider(color = Slate200)

                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(2.dp, Slate100, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data("https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=STUDENT-ID:${student.id}")
                                .crossfade(true)
                                .build(),
                            imageLoader = imageLoader,
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("${student.firstname} ${student.lastname}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate800)
                    Text("${student.course} • Year ${student.yearlevel}", color = Slate500, fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { /* Implement Download Logic Later */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Download PNG")
                    }
                }
            }
        }
    }
}

// ================= HELPERS (ADDED AS REQUESTED) =================
//
//@Composable
//fun InputGroup(
//    label: String,
//    value: String,
//    onValueChange: (String) -> Unit,
//    placeholder: String,
//    modifier: Modifier = Modifier,
//    icon: ImageVector? = null
//) {
//    Column(modifier = modifier) {
//        Text(
//            text = label.uppercase(),
//            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500, letterSpacing = 0.5.sp)
//        )
//        Spacer(Modifier.height(8.dp))
//        OutlinedTextField(
//            value = value,
//            onValueChange = onValueChange,
//            placeholder = { Text(placeholder, color = Slate400) },
//            trailingIcon = if (icon != null) { { Icon(icon, null, tint = Slate400) } } else null,
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Indigo600,
//                unfocusedBorderColor = Slate200,
//                focusedContainerColor = SurfaceColor,
//                unfocusedContainerColor = SurfaceColor
//            ),
//            singleLine = true
//        )
//    }
//}

@Composable
fun ActionIcon(
    icon: ImageVector,
    tooltip: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (isDestructive) Red500 else Slate400
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = tooltip, tint = tint, modifier = Modifier.size(20.dp))
    }
}