package com.example.recipefinder.ui.createrecipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recipefinder.data.Ingredient
import com.example.recipefinder.data.trimmedString
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.authFieldColors

@Composable
private fun UnitSelector(
    unit: String,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White, shape = MaterialTheme.shapes.small)
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = unit.ifBlank { "Select unit ▼" },
                color = Secondary,
                fontSize = 16.sp,
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                com.example.recipefinder.data.Unit.entries.forEach { enumUnit ->
                    DropdownMenuItem(
                        text = { Text(enumUnit.name.lowercase().replaceFirstChar(Char::titlecase)) },
                        onClick = {
                            onUnitSelected(enumUnit.name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientDialogButtons(
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Button(
        enabled = confirmEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Secondary,
            contentColor = Color.White
        ),
        onClick = onConfirm
    ) {
        Text("Confirm")
    }
}

@Composable
fun IngredientInputDialog(
    onConfirm: (Ingredient) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableDoubleStateOf(0.0) }
    var unit by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text(color = Secondary, text = "Add Ingredient", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        colors = authFieldColors(),
                        value = if (quantity == 0.0) "" else quantity.trimmedString(),
                        onValueChange = { value ->
                            value.toDoubleOrNull()?.let { quantity = it }
                        },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(.4f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    UnitSelector(
                        unit = unit,
                        onUnitSelected = { unit = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    shape = MaterialTheme.shapes.medium,
                )
            }
        },
        confirmButton = {
            IngredientDialogButtons(
                confirmEnabled = name.isNotBlank() && quantity > 0 && unit.isNotBlank(),
                onConfirm = {
                    onConfirm(Ingredient(0, name.lowercase().replaceFirstChar(Char::titlecase), quantity, com.example.recipefinder.data.Unit.valueOf(unit.uppercase()), note))
                },
                onDismiss = onDismiss,
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}

@Composable
fun EditIngredientInputDialog(
    ingredient: Ingredient,
    onConfirm: (Ingredient) -> Unit,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    val newName = remember { mutableStateOf(ingredient.name) }
    val newQuantity = remember { mutableDoubleStateOf(ingredient.quantity) }
    val newUnit = remember { mutableStateOf(ingredient.unit.name) }
    val newNote = remember { mutableStateOf(ingredient.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Edit Ingredient", color = Secondary, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = newName.value,
                    onValueChange = { newName.value = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        colors = authFieldColors(),
                        value = newQuantity.doubleValue.trimmedString(),
                        onValueChange = { value ->
                            value.toDoubleOrNull()?.let { newQuantity.doubleValue = it }
                        },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(.4f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    UnitSelector(
                        unit = newUnit.value,
                        onUnitSelected = { newUnit.value = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OutlinedTextField(
                    colors = authFieldColors(),
                    value = newNote.value,
                    onValueChange = { newNote.value = it },
                    label = { Text("Note") },
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
                        text = "Remove Ingredient",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            IngredientDialogButtons(
                confirmEnabled = newName.value.isNotBlank() && newQuantity.doubleValue > 0 && newUnit.value.isNotBlank(),
                onConfirm = {
                    onConfirm(Ingredient(ingredient.id, newName.value.lowercase().replaceFirstChar(Char::titlecase), newQuantity.doubleValue, com.example.recipefinder.data.Unit.valueOf(newUnit.value.uppercase()), newNote.value))
                },
                onDismiss = onDismiss,
            )
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}
