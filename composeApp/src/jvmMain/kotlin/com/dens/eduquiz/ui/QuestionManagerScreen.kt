package com.dens.eduquiz.ui

import ActivityItem
import CreateQuestionRequest
import Question
import UpdateQuestionRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dens.eduquiz.ui.theme.*
import com.dens.eduquiz.viewmodel.QuestionViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuestionManagerScreen(
    activity: ActivityItem,
    viewModel: QuestionViewModel,
    onBack: () -> Unit
) {
    val allQuestions = viewModel.questions
    val activityQuestions = allQuestions.filter { it.activityId == activity.id }

    var isForm by remember { mutableStateOf(false) }
    var editingQ by remember { mutableStateOf<Question?>(null) }

    val totalTime = activityQuestions.sumOf { it.timer }

    // --- FORM STATE ---
    var qText by remember { mutableStateOf("") }
    var qTime by remember { mutableStateOf("") }

    val options = remember { mutableStateListOf("", "", "", "") }
    var correctIdx by remember { mutableStateOf(0) }

    fun openForm(q: Question?) {
        editingQ = q
        qText = q?.text ?: ""
        qTime = q?.timer?.toString() ?: ""

        options.clear()
        if (q != null) {
            options.add(q.a); options.add(q.b); options.add(q.c); options.add(q.d)
        } else {
            options.addAll(listOf("", "", "", ""))
        }

        val correctStr = q?.correct ?: ""
        correctIdx = if (q != null) {
            when (correctStr) { q.a -> 0; q.b -> 1; q.c -> 2; q.d -> 3; else -> 0 }
        } else 0
        isForm = true
    }

    fun handleSave() {
        if (options.any { it.isBlank() }) return

        val validCorrectIdx = if (correctIdx in 0..3) correctIdx else 0
        val correctAnswerStr = options[validCorrectIdx]

        val cleanTime = qTime.filter { it.isDigit() }
        val timeInt = cleanTime.toIntOrNull() ?: 0

        if (editingQ != null) {
            viewModel.updateQuestion(
                id = editingQ!!.id!!,
                request = UpdateQuestionRequest(qText, options[0], options[1], options[2], options[3], correctAnswerStr, activity.id, timeInt, editingQ?.status ?: "pending"),
                onSuccess = { isForm = false }
            )
        } else {
            viewModel.addQuestion(
                request = CreateQuestionRequest(qText, options[0], options[1], options[2], options[3], correctAnswerStr, activity.id, timeInt),
                onSuccess = { isForm = false }
            )
        }
    }

    // --- UI RENDER ---
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        if (isForm) {
            // Header (Fixed at top)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                IconButton(onClick = { isForm = false }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Slate500) }
                Spacer(Modifier.width(8.dp))
                Text(if (editingQ != null) "Edit Question" else "New Question", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate800)
            }

            // Scrollable Form Container
            // FIX: Added weight(1f) and verticalScroll to make this part scrollable
            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                QuestionForm(
                    qText, { qText = it },
                    qTime, { input ->
                        if (input.all { it.isDigit() }) qTime = input
                    },
                    options, correctIdx, { correctIdx = it }, null, null, { }, { handleSave() }, { isForm = false }
                )
            }
        } else {
            // Dashboard View (Unchanged)
            Row(modifier = Modifier.clickable { onBack() }.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Slate500, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Back to Activities", color = Slate500, fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(activity.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate800)
                    Spacer(Modifier.height(8.dp))
                    Text(activity.description.ifBlank { "Manage your questions." }, style = MaterialTheme.typography.bodyMedium, color = Slate500)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatsChip(icon = Icons.AutoMirrored.Filled.List, text = "${activityQuestions.size} Questions")
                        StatsChip(icon = Icons.Default.Schedule, text = "Total Time: ${totalTime}s")
                    }
                }
                Button(onClick = { openForm(null) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Add Question")
                }
            }
            Spacer(Modifier.height(32.dp))
            if (activityQuestions.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("No questions yet.", color = Slate500) }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp), contentPadding = PaddingValues(bottom = 40.dp)) {
                    itemsIndexed(activityQuestions, key = { _, q -> q.id ?: 0 }) { index, q ->
                        QuestionCard(index = index + 1, question = q, onEdit = { openForm(q) }, onDelete = { q.id?.let { viewModel.deleteQuestion(it) } })
                    }
                }
            }
        }
    }
}

// --- GLOBAL QUESTION BANK SCREEN ---
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GlobalQuestionBankScreen(
    viewModel: QuestionViewModel,
    activities: List<ActivityItem>
) {
    val allQuestions = viewModel.questions
    var searchQuery by remember { mutableStateOf("") }

    var isForm by remember { mutableStateOf(false) }
    var editingQ by remember { mutableStateOf<Question?>(null) }
    var qText by remember { mutableStateOf("") }

    var qTime by remember { mutableStateOf("") }

    val options = remember { mutableStateListOf("", "", "", "") }
    var correctIdx by remember { mutableStateOf(0) }
    var selectedActivityId by remember { mutableStateOf<Long?>(null) }

    fun openForm(q: Question?) {
        editingQ = q
        qText = q?.text ?: ""
        qTime = q?.timer?.toString() ?: ""

        selectedActivityId = q?.activityId ?: activities.firstOrNull()?.id
        options.clear()
        if (q != null) {
            options.add(q.a); options.add(q.b); options.add(q.c); options.add(q.d)
        } else {
            options.addAll(listOf("", "", "", ""))
        }
        val correctStr = q?.correct ?: ""
        correctIdx = if (q != null) {
            when (correctStr) { q.a -> 0; q.b -> 1; q.c -> 2; q.d -> 3; else -> 0 }
        } else 0
        isForm = true
    }

    fun handleSave() {
        if (options.any { it.isBlank() } || selectedActivityId == null) return
        val validCorrectIdx = if (correctIdx in 0..3) correctIdx else 0
        val correctAnswerStr = options[validCorrectIdx]

        val cleanTime = qTime.filter { it.isDigit() }
        val timeInt = cleanTime.toIntOrNull() ?: 0

        if (editingQ != null) {
            viewModel.updateQuestion(
                id = editingQ!!.id!!,
                request = UpdateQuestionRequest(qText, options[0], options[1], options[2], options[3], correctAnswerStr, selectedActivityId!!, timeInt, editingQ?.status ?: "pending"),
                onSuccess = { isForm = false }
            )
        } else {
            viewModel.addQuestion(
                request = CreateQuestionRequest(qText, options[0], options[1], options[2], options[3], correctAnswerStr, selectedActivityId!!, timeInt),
                onSuccess = { isForm = false }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        if (isForm) {
            // FIX: Ensure this Box has weight(1f) and verticalScroll
            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                QuestionForm(
                    qText, { qText = it },
                    qTime, { input -> if (input.all { it.isDigit() }) qTime = input },
                    options, correctIdx, { correctIdx = it },
                    activities, selectedActivityId, { selectedActivityId = it },
                    { handleSave() }, { isForm = false }
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Global Question Bank", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate800); Text("Manage all questions", style = MaterialTheme.typography.bodyMedium, color = Slate500) }
                Button(onClick = { openForm(null) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor), shape = RoundedCornerShape(8.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Add Question") }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Search...", color = Slate500) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = Slate500) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Slate200), singleLine = true)
            Spacer(Modifier.height(24.dp))
            val filteredList = allQuestions.filter { it.text.contains(searchQuery, true) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                itemsIndexed(filteredList, key = { _, q -> q.id ?: 0 }) { index, q ->
                    QuestionCard(index = index + 1, question = q, onEdit = { openForm(q) }, onDelete = { q.id?.let { viewModel.deleteQuestion(it) } }, showActivityLabel = true, activityLabel = activities.find { it.id == q.activityId }?.title ?: "Unknown")
                }
            }
        }
    }
}

// --- HELPER COMPONENTS (Unchanged) ---
@Composable
fun StatsChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(color = Slate50, shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Slate500, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun QuestionCard(index: Int, question: Question, onEdit: () -> Unit, onDelete: () -> Unit, showActivityLabel: Boolean = false, activityLabel: String = "") {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.size(32.dp).background(Slate50, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text(text = "$index", fontWeight = FontWeight.Bold, color = Slate500) }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(question.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate800)
                    if(showActivityLabel) { Spacer(Modifier.height(4.dp)); Text(text = "in $activityLabel", fontSize = 12.sp, color = PrimaryColor) }
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, "Edit", tint = Slate400, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "Delete", tint = Slate400, modifier = Modifier.size(18.dp)) }
                }
            }
            Spacer(Modifier.height(24.dp))
            val options = listOf(question.a, question.b, question.c, question.d)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { OptionDisplay(options[0], options[0] == question.correct, Modifier.weight(1f)); OptionDisplay(options[1], options[1] == question.correct, Modifier.weight(1f)) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { OptionDisplay(options[2], options[2] == question.correct, Modifier.weight(1f)); OptionDisplay(options[3], options[3] == question.correct, Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Schedule, null, tint = Slate400, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)); Text("${question.timer} seconds", fontSize = 12.sp, color = Slate500) }
        }
    }
}

@Composable
fun OptionDisplay(text: String, isCorrect: Boolean, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(48.dp), color = if (isCorrect) Emerald50 else Slate50, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, if (isCorrect) Emerald600 else Color.Transparent)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = text, style = MaterialTheme.typography.bodyMedium, color = if (isCorrect) Emerald600 else Slate800, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (isCorrect) { Icon(Icons.Default.Check, null, tint = Emerald600, modifier = Modifier.size(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionForm(
    qText: String, onTextChange: (String) -> Unit,
    qTime: String, onTimeChange: (String) -> Unit,
    options: List<String>, correctIdx: Int, onCorrectChange: (Int) -> Unit,
    allActivities: List<ActivityItem>? = null, selectedActivityId: Long? = null, onActivitySelected: ((Long) -> Unit)? = null,
    onSave: () -> Unit, onCancel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val actualTime = qTime.filter { it.isDigit() }.toIntOrNull() ?: 0

    Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), border = BorderStroke(1.dp, Slate200)) {
        Column(Modifier.padding(32.dp)) {
            if (allActivities != null && onActivitySelected != null && selectedActivityId != null) {
                val selectedTitle = allActivities.find { it.id == selectedActivityId }?.title ?: "Select Activity"
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = selectedTitle, onValueChange = {}, readOnly = true, label = { Text("Assign to Activity") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Slate200))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { allActivities.forEach { act -> DropdownMenuItem(text = { Text(act.title) }, onClick = { onActivitySelected(act.id); expanded = false }) } }
                }
                Spacer(Modifier.height(16.dp))
            }
            OutlinedTextField(value = qText, onValueChange = onTextChange, label = { Text("Question Text") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Slate200))
            Spacer(Modifier.height(16.dp))
            Text("Time Limit (seconds)", style = MaterialTheme.typography.bodySmall, color = Slate500, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(onClick = { val current = qTime.filter { it.isDigit() }.toIntOrNull() ?: 0; val newTime = (current - 5).coerceAtLeast(0); onTimeChange(newTime.toString()) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Slate100, contentColor = Slate800), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Remove, "Decrease") }
                OutlinedTextField(value = qTime, onValueChange = onTimeChange, modifier = Modifier.width(100.dp).padding(horizontal = 8.dp), placeholder = { Text("0") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Slate200), textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center))
                FilledIconButton(onClick = { val current = qTime.filter { it.isDigit() }.toIntOrNull() ?: 0; val newTime = current + 5; onTimeChange(newTime.toString()) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = PrimaryColor, contentColor = Color.White), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Add, "Increase") }
            }
            Spacer(Modifier.height(8.dp))
            Surface(color = if (actualTime > 0) Emerald50 else Slate50, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (actualTime > 0) Emerald200 else Slate200)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (actualTime > 0) Icons.Default.CheckCircle else Icons.Default.Info, null, tint = if (actualTime > 0) Emerald600 else Slate500, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(text = if (actualTime > 0) "Saving: $actualTime seconds for this question." else "No timer set (0 seconds).", style = MaterialTheme.typography.bodySmall, color = if (actualTime > 0) Emerald600 else Slate500)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate800)
            Spacer(Modifier.height(16.dp))
            options.forEachIndexed { idx, opt ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    RadioButton(selected = correctIdx == idx, onClick = { onCorrectChange(idx) }, colors = RadioButtonDefaults.colors(selectedColor = PrimaryColor))
                    OutlinedTextField(value = opt, onValueChange = { (options as MutableList<String>)[idx] = it }, modifier = Modifier.weight(1f), placeholder = { Text("Option ${if (idx == 0) "A" else if (idx == 1) "B" else if (idx == 2) "C" else "D"}") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor, unfocusedBorderColor = Slate200))
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onCancel, border = BorderStroke(1.dp, Slate200), colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate500)) { Text("Cancel") }
                Spacer(Modifier.width(12.dp))
                Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Save Question") }
            }
        }
    }
}