package com.example.recipefinder.ui.createrecipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.recipefinder.data.Step
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.authFieldColors

@Composable
fun StepsInputDialog(
    onConfirm: (Step) -> Unit,
    onDismiss: () -> Unit,
    assignedIndex: Int = 0
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Add Step", color = Secondary, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions") },
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image") },
                    placeholder = { Text("No function (Requires Premium Firestore)") },
                    shape = MaterialTheme.shapes.medium,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = Color.White
                ),
                onClick = {
                    onConfirm(Step(assignedIndex, title.lowercase().replaceFirstChar(Char::titlecase), description, imageUrl))
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}

@Composable
fun EditStepInputDialog(
    step: Step,
    onConfirm: (Step) -> Unit,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    val newTitle = remember { mutableStateOf(step.title) }
    val newDescription = remember { mutableStateOf(step.description) }
    val newImageUrl = remember { mutableStateOf(step.imageUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Edit Step", color = Secondary, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = newTitle.value,
                    onValueChange = { newTitle.value = it },
                    label = { Text("Title") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = newDescription.value,
                    onValueChange = { newDescription.value = it },
                    label = { Text("Description") },
                    shape = MaterialTheme.shapes.medium,
                )
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = newImageUrl.value,
                    onValueChange = { newImageUrl.value = it },
                    label = { Text("Image URL") },
                    shape = MaterialTheme.shapes.medium,
                )
                Button(
                    onClick = onRemove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                ) {
                    Text(
                        text = "Remove Step",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = newTitle.value.isNotBlank() && newDescription.value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = Color.White
                ),
                onClick = { onConfirm(Step(step.index, newTitle.value.lowercase().replaceFirstChar(Char::titlecase), newDescription.value, newImageUrl.value)) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}
