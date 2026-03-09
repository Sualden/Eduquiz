package com.dens.eduquiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dens.eduquiz.model.Student
import com.dens.eduquiz.ui.theme.PrimaryColor
import com.dens.eduquiz.ui.theme.PrimaryLight
import com.dens.eduquiz.ui.theme.Red500
import com.dens.eduquiz.ui.theme.Slate500
import com.dens.eduquiz.ui.theme.Slate800

@Composable
fun StudentRow(
    student: Student,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${student.firstname.first()}${student.lastname.first()}",
                    color = PrimaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "${student.lastname}, ${student.firstname}",
                    fontWeight = FontWeight.SemiBold,
                    color = Slate800
                )
                Text(
                    text = "${student.course} • Year ${student.yearlevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Red500)
        }
    }
}