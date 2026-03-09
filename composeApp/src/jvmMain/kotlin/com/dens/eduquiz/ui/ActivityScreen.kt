package com.dens.eduquiz.ui

import ActivityItem
import CreateActivityRequest
import UpdateActivityRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dens.eduquiz.ui.theme.*
import com.dens.eduquiz.viewmodel.ActivityViewModel
import com.dens.eduquiz.viewmodel.QuestionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

data class ActivityMember(val activityId: Long, val studentId: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    activityViewModel: ActivityViewModel,
    questionViewModel: QuestionViewModel,
    activityMembers: List<ActivityMember>,
    onStartQuiz: (ActivityItem) -> Unit,
    onManageMembers: (ActivityItem) -> Unit
) {
    val activities = activityViewModel.activityList
    val questions = questionViewModel.questions

    var view by remember { mutableStateOf("list") }
    var filter by remember { mutableStateOf("all") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingActivity by remember { mutableStateOf<ActivityItem?>(null) }
    var activityForQr by remember { mutableStateOf<ActivityItem?>(null) }
    var selectedActivityForQuestions by remember { mutableStateOf<ActivityItem?>(null) }

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    fun openDialog(act: ActivityItem?) {
        editingActivity = act
        title = act?.title ?: ""
        desc = act?.description ?: ""
        deadline = act?.deadline ?: ""
        showAddDialog = true
    }

    fun saveActivity() {
        val finalDeadline = deadline.ifBlank { LocalDateTime.now().plusDays(7).toString() }
        if (editingActivity != null) {
            activityViewModel.updateActivity(
                id = editingActivity!!.id,
                request = UpdateActivityRequest(title, desc, finalDeadline, editingActivity!!.status),
                onSuccess = { showAddDialog = false }
            )
        } else {
            activityViewModel.addActivity(
                request = CreateActivityRequest(title, desc, finalDeadline),
                onSuccess = { showAddDialog = false }
            )
        }
    }

    if (view == "manage_questions" && selectedActivityForQuestions != null) {
        QuestionManagerScreen(
            activity = selectedActivityForQuestions!!,
            viewModel = questionViewModel,
            onBack = { view = "list" }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Activities", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate800)
                        Text("Manage quizzes and deadlines", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    }
                    Button(
                        onClick = { openDialog(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Activity", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Filter Tabs
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("all", "pending", "completed").forEach { f ->
                        val isSelected = filter == f
                        val bgColor = if (isSelected) SurfaceColor else Color.Transparent
                        val txtColor = if (isSelected) PrimaryColor else Slate500
                        val border = if (isSelected) null else BorderStroke(1.dp, Slate200)

                        Surface(
                            onClick = { filter = f },
                            shape = RoundedCornerShape(8.dp),
                            color = bgColor,
                            border = border,
                            modifier = Modifier.height(32.dp),
                            shadowElevation = if (isSelected) 1.dp else 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Text(f.replaceFirstChar { it.uppercase() }, color = txtColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Grid Content
                val filteredList = activities.filter { filter == "all" || it.status == filter }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(filteredList) { act ->
                        ActivityCardItem(
                            activity = act,
                            qCount = questions.count { it.activityId == act.id },
                            memberCount = activityMembers.count { it.activityId == act.id },
                            onEdit = { openDialog(act) },
                            onDelete = { activityViewModel.deleteActivity(act.id) },
                            onManageQ = {
                                selectedActivityForQuestions = act
                                questionViewModel.loadQuestions()
                                view = "manage_questions"
                            },
                            onPreview = { onStartQuiz(act) },
                            onManageMembers = { onManageMembers(act) },
                            onShowQr = { activityForQr = act }
                        )
                    }
                }
            }

            // QR Code Dialog
            activityForQr?.let {
                QRCodeDialog(activity = it, onDismiss = { activityForQr = null })
            }

            // Add/Edit Dialog
            if (showAddDialog) {
                var showDatePicker by remember { mutableStateOf(false) }
                var showTimePicker by remember { mutableStateOf(false) }
                val initialMillis = try { if (deadline.isNotBlank()) LocalDateTime.parse(deadline).toInstant(ZoneOffset.UTC).toEpochMilli() else null } catch (e: Exception) { null }
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
                val timePickerState = rememberTimePickerState(is24Hour = true)

                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text(if (editingActivity != null) "Edit Activity" else "Add Activity") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                            OutlinedTextField(
                                value = deadline.replace("T", " "),
                                onValueChange = { },
                                label = { Text("Deadline") },
                                placeholder = { Text("Select Date & Time") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, "Select") } }
                            )
                        }
                    },
                    confirmButton = { Button(onClick = { saveActivity() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Save") } },
                    dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = { TextButton(onClick = { showDatePicker = false; showTimePicker = true }) { Text("Next") } },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
                    ) { DatePicker(state = datePickerState) }
                }

                if (showTimePicker) {
                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = { TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                                val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                                deadline = LocalDateTime.of(date, time).toString()
                            }
                            showTimePicker = false
                        }) { Text("OK") } },
                        dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Select Time")
                                Spacer(Modifier.height(16.dp))
                                TimePicker(state = timePickerState)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityCardItem(
    activity: ActivityItem,
    qCount: Int,
    memberCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManageQ: () -> Unit,
    onPreview: () -> Unit,
    onManageMembers: () -> Unit,
    onShowQr: () -> Unit
) {
    var timeLeft by remember { mutableStateOf("") }
    LaunchedEffect(activity.deadline) {
        while (true) {
            try {
                val end = LocalDateTime.parse(activity.deadline)
                val now = LocalDateTime.now()
                val diff = ChronoUnit.SECONDS.between(now, end)
                if (diff <= 0) { timeLeft = "Expired"; break }
                val days = diff / 86400
                val hours = (diff % 86400) / 3600
                val mins = (diff % 3600) / 60
                timeLeft = if (days > 0) "${days}d ${hours}h left" else "${hours}h ${mins}m left"
            } catch (e: Exception) { timeLeft = "Invalid Date" }
            delay(60000)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val isCompleted = activity.status == "completed"
                Surface(color = if (isCompleted) Emerald50 else Amber50, shape = RoundedCornerShape(50)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Schedule, null, tint = if (isCompleted) Emerald600 else Amber600, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(activity.status.replaceFirstChar { it.uppercase() }, color = if (isCompleted) Emerald600 else Amber600, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode, "QR Code", tint = Slate400, modifier = Modifier.size(20.dp).clickable { onShowQr() })
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Default.Edit, "Edit", tint = Slate400, modifier = Modifier.size(20.dp).clickable { onEdit() })
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Default.Delete, "Delete", tint = Slate400, modifier = Modifier.size(20.dp).clickable { onDelete() })
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(activity.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Slate800)
            Spacer(Modifier.height(8.dp))
            Text(activity.description, color = Slate500, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(Modifier.height(24.dp))
            Divider(color = Slate100)
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Slate500)
                    Text(activity.deadline.take(10), fontSize = 12.sp, color = Slate500, modifier = Modifier.padding(start = 4.dp, end = 12.dp))
                    Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(14.dp), tint = Slate500)
                    Text("$qCount", fontSize = 12.sp, color = Slate500, modifier = Modifier.padding(start = 4.dp, end = 12.dp))
                    Icon(Icons.Default.Groups, null, modifier = Modifier.size(14.dp), tint = Slate500)
                    Text("$memberCount", fontSize = 12.sp, color = Slate500, modifier = Modifier.padding(start = 4.dp))
                }

                if (activity.status != "completed") {
                    Surface(color = if (timeLeft == "Expired") Red50 else PrimaryLight, shape = RoundedCornerShape(6.dp)) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = if (timeLeft == "Expired") Red500 else PrimaryColor, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(timeLeft, fontSize = 11.sp, color = if (timeLeft == "Expired") Red500 else PrimaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = onManageMembers, modifier = Modifier.weight(0.2f).height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryLight, contentColor = PrimaryColor), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.PersonAdd, "Members", modifier = Modifier.size(20.dp))
                }
                FilledTonalButton(onClick = onManageQ, modifier = Modifier.weight(0.6f).height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = PrimaryLight, contentColor = PrimaryColor)) {
                    Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Questions", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onPreview, modifier = Modifier.weight(0.2f).height(44.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Slate200), colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate500), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Default.PlayArrow, "Preview", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun QRCodeDialog(activity: ActivityItem, onDismiss: () -> Unit) {
    val qrData = "eduquiz://join/${activity.id}"

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Join Activity", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate800)
                Spacer(Modifier.height(8.dp))
                Text(activity.title, style = MaterialTheme.typography.titleMedium, color = PrimaryColor, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.size(220.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, Slate200, RoundedCornerShape(16.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
                    RealQRCodeImage(content = qrData, sizeDp = 188.dp)
                }
                Spacer(Modifier.height(24.dp))
                Text("Scan to join directly", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RealQRCodeImage(content: String, sizeDp: Dp) {
    val density = LocalDensity.current
    val sizePx = with(density) { sizeDp.toPx().toInt() }
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(content) {
        scope.launch {
            val generated = withContext(Dispatchers.IO) { generateQRBitmap(content, sizePx) }
            bitmap = generated
        }
    }

    if (bitmap != null) {
        Image(bitmap = bitmap!!, contentDescription = "QR Code for $content", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = PrimaryColor)
        }
    }
}

fun generateQRBitmap(content: String, sizePx: Int): ImageBitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until h) for (x in 0 until w) image.setRGB(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
        image.toComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
