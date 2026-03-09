package com.dens.eduquiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dens.eduquiz.database.Admin
import com.dens.eduquiz.ui.theme.BgColor
import com.dens.eduquiz.ui.theme.PrimaryColor
import com.dens.eduquiz.ui.theme.Red50
import com.dens.eduquiz.ui.theme.Red500
import com.dens.eduquiz.ui.theme.Slate100
import com.dens.eduquiz.ui.theme.Slate50
import com.dens.eduquiz.ui.theme.Slate500
import com.dens.eduquiz.ui.theme.Slate800
import com.dens.eduquiz.ui.theme.SurfaceColor
import com.dens.eduquiz.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    // Observe ViewModel State
    val uiState = viewModel.uiState
    val adminList = uiState.adminList

    // UI States
    var showAddModal by remember { mutableStateOf(false) }
    var adminToDelete by remember { mutableStateOf<Admin?>(null) }

    // Add Admin Form State
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Initial Load
    LaunchedEffect(Unit) {
        viewModel.refreshAdminList()
    }

    Scaffold(
        containerColor = BgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Manage administrator accounts", color = Slate500)

            Spacer(Modifier.height(24.dp))

            // --- ADMIN LIST CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Admin Accounts", fontWeight = FontWeight.Bold, color = Slate800)
                        TextButton(onClick = { showAddModal = true }) {
                            Text("+ Add New")
                        }
                    }
                    Divider(color = Slate100)

                    // List Items
                    LazyColumn {
                        items(adminList) { admin ->
                            AdminRowItem(
                                admin = admin,
                                onDelete = { adminToDelete = admin }
                            )
                            Divider(color = Slate50)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // --- LOGOUT BUTTON ---
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Red50, contentColor = Red500),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("Log Out")
            }
        }
    }

    // --- MODALS ---

    // 1. Add Admin Modal
    if (showAddModal) {
        AlertDialog(
            onDismissRequest = { showAddModal = false },
            title = { Text("Add New Admin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") }, // Required by backend
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addAdmin(name, email, password) { success, _ ->
                            if (success) {
                                showAddModal = false
                                name = ""; email = ""; password = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddModal = false }) { Text("Cancel") }
            }
        )
    }

    // 2. Delete Confirmation Modal
    if (adminToDelete != null) {
        AlertDialog(
            onDismissRequest = { adminToDelete = null },
            title = { Text("Delete Admin") },
            text = { Text("Are you sure you want to delete \"${adminToDelete?.fullname}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        adminToDelete?.let { viewModel.deleteAdmin(it.id) }
                        adminToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { adminToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminRowItem(admin: Admin, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Slate100, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = admin.fullname.firstOrNull()?.toString()?.uppercase() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(admin.fullname, fontWeight = FontWeight.Medium, color = Slate800)
                Text(admin.email, style = MaterialTheme.typography.bodySmall, color = Slate500)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, null, tint = Red500)
        }
    }
}