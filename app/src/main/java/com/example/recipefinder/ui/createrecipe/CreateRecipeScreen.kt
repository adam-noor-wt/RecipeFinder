package com.example.recipefinder.ui.createrecipe

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.recipefinder.data.ImageUploadService
import com.example.recipefinder.data.Ingredient
import com.example.recipefinder.ui.common.ImageCropDialog
import com.example.recipefinder.data.ParsedRecipe
import com.example.recipefinder.data.Recipe
import com.example.recipefinder.data.RecipeViewModel
import com.example.recipefinder.data.Step
import com.example.recipefinder.data.Tag
import com.example.recipefinder.data.abbreviateUnit
import com.example.recipefinder.data.parseRecipeText
import com.example.recipefinder.data.fractionString
import com.example.recipefinder.ui.recipes.SingleRecipeScreen
import com.example.recipefinder.ui.theme.Primary
import com.example.recipefinder.ui.theme.RecipeFinderTheme
import com.example.recipefinder.ui.theme.Secondary
import com.example.recipefinder.ui.theme.Tertiary
import com.example.recipefinder.ui.theme.authFieldColors
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

private fun reindexSteps(steps: List<Step>): List<Step> =
    steps.mapIndexed { i, step -> step.copy(index = i + 1) }

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleSmall,
    )
}

@Composable
private fun RemovableChip(text: String, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.padding(4.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Text(
                text = text,
                color = Secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = Secondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AddSectionButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
        Text(text)
    }
}

@Composable
private fun ImportChoiceDialog(
    onPasteText: () -> Unit,
    onPhoto: () -> Unit,
    onFromLink: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Import Recipe", color = Secondary, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Choose how you'd like to import a recipe. (note: this feature is experimental, results may vary)",
                    color = Secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = onFromLink,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("From Link")
                }
                Button(
                    onClick = onPasteText,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Paste Text")
                }
                Button(
                    onClick = onPhoto,
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("From Photo")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}

@Composable
private fun PasteTextImportDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    initialText: String = "",
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Paste Recipe Text", color = Secondary, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                colors = authFieldColors(),
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("Paste your recipe here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
            )
        },
        confirmButton = {
            Button(
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
                onClick = { onConfirm(text) },
            ) {
                Text("Parse")
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
private fun LinkImportDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Import from Link", color = Secondary, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter a recipe URL. Most recipe websites use a standard format that can be imported.",
                    color = Secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        errorMessage = null
                    },
                    colors = authFieldColors(),
                    shape = MaterialTheme.shapes.medium,
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Secondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = url.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
                onClick = { onConfirm(url) },
            ) {
                Text(if (isLoading) "Loading..." else "Import")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}

@Composable
private fun ConfirmOverwriteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Replace current entries?", color = Secondary, style = MaterialTheme.typography.titleLarge) },
        text = {
            Text(
                "Importing will replace your current title, description, prep time, ingredients, and steps.",
                color = Secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
                onClick = onConfirm,
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun CreateRecipeScreen(
    onCreate: (Recipe) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf(listOf<Ingredient>()) }
    var preparationTime by remember { mutableIntStateOf(0) }
    var steps by remember { mutableStateOf(listOf<Step>()) }
    var imageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var style by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<Tag>()) }
    var showPreview by remember { mutableStateOf(false) }

    val imageUploadService = remember { ImageUploadService() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        println("Image picker callback - URI: $uri")
        uri?.let {
            println("Setting selectedImageUri to: $it")
            selectedImageUri = it
            showCropDialog = true
        }
    }

    var showImportChoiceDialog by remember { mutableStateOf(false) }
    var showPasteTextDialog by remember { mutableStateOf(false) }
    var showPhotoImportDialog by remember { mutableStateOf(false) }
    var showLinkImportDialog by remember { mutableStateOf(false) }
    var pasteTextInitialText by remember { mutableStateOf("") }
    var pendingImport by remember { mutableStateOf<ParsedRecipe?>(null) }
    var showImportError by remember { mutableStateOf(false) }

    val recipeUrlFetcher = remember { com.example.recipefinder.data.RecipeUrlFetcher() }
    val coroutineScope = rememberCoroutineScope()

    fun applyImport(parsed: ParsedRecipe) {
        title = parsed.title
        description = parsed.description
        preparationTime = parsed.preparationTime
        ingredients = parsed.ingredients
        steps = parsed.steps
    }

    fun handleParsedText(text: String) {
        val parsed = parseRecipeText(text)
        val hasExistingContent = title.isNotBlank() || description.isNotBlank() ||
            ingredients.isNotEmpty() || steps.isNotEmpty() || style.isNotBlank() || preparationTime != 0
        showPasteTextDialog = false
        if (hasExistingContent) {
            pendingImport = parsed
        } else {
            applyImport(parsed)
        }
    }

    fun moveStep(step: Step, delta: Int) {
        val list = steps.toMutableList()
        val from = list.indexOf(step)
        val to = from + delta
        if (from < 0 || to < 0 || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        steps = reindexSteps(list)
    }

    Box(
        modifier = Modifier.fillMaxSize().zIndex(1f)
    )
    {
        Card(
            modifier = Modifier
                .fillMaxWidth(.98f)
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            )
            {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = { showImportChoiceDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Import")
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Create a Recipe",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Fill in the details below to create your recipe.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                if (showImportChoiceDialog) {
                    ImportChoiceDialog(
                        onFromLink = {
                            showImportChoiceDialog = false
                            showLinkImportDialog = true
                        },
                        onPasteText = {
                            showImportChoiceDialog = false
                            pasteTextInitialText = ""
                            showPasteTextDialog = true
                        },
                        onPhoto = {
                            showImportChoiceDialog = false
                            showPhotoImportDialog = true
                        },
                        onDismiss = { showImportChoiceDialog = false },
                    )
                }
                if (showLinkImportDialog) {
                    LinkImportDialog(
                        onConfirm = { url ->
                            coroutineScope.launch {
                                val result = recipeUrlFetcher.fetchRecipe(url)
                                result.onSuccess { parsed ->
                                    val hasExistingContent = title.isNotBlank() || description.isNotBlank() ||
                                        ingredients.isNotEmpty() || steps.isNotEmpty() || style.isNotBlank() || preparationTime != 0
                                    showLinkImportDialog = false
                                    if (hasExistingContent) {
                                        pendingImport = parsed
                                    } else {
                                        applyImport(parsed)
                                    }
                                }.onFailure {
                                    showLinkImportDialog = false
                                    showImportError = true
                                }
                            }
                        },
                        onDismiss = { showLinkImportDialog = false },
                    )
                }
                if (showPasteTextDialog) {
                    PasteTextImportDialog(
                        onConfirm = { text -> handleParsedText(text) },
                        onDismiss = { showPasteTextDialog = false },
                        initialText = pasteTextInitialText,
                    )
                }
                if (showPhotoImportDialog) {
                    PhotoImportDialog(
                        onConfirm = { text ->
                            showPhotoImportDialog = false
                            pasteTextInitialText = text
                            showPasteTextDialog = true
                        },
                        onDismiss = { showPhotoImportDialog = false },
                    )
                }
                if (showImportError) {
                    AlertDialog(
                        onDismissRequest = { showImportError = false },
                        shape = MaterialTheme.shapes.large,
                        title = { Text("Import Failed", color = Secondary, style = MaterialTheme.typography.titleLarge) },
                        text = {
                            Text(
                                "Sorry, couldn't import the recipe from that link. The website may not have recipe data in a standard format.",
                                color = Secondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        confirmButton = {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Secondary, contentColor = Color.White),
                                onClick = { showImportError = false },
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
                val currentPendingImport = pendingImport
                if (currentPendingImport != null) {
                    ConfirmOverwriteDialog(
                        onConfirm = {
                            applyImport(currentPendingImport)
                            pendingImport = null
                        },
                        onDismiss = { pendingImport = null },
                    )
                }

                // Image Crop Dialog
                if (showCropDialog && selectedImageUri != null) {
                    ImageCropDialog(
                        imageUri = selectedImageUri!!,
                        onConfirm = { croppedBitmap ->
                            showCropDialog = false
                            isUploadingImage = true
                            uploadProgress = 0f
                            println("Starting upload of cropped image...")

                            imageUploadService.uploadRecipeBitmap(
                                bitmap = croppedBitmap,
                                onSuccess = { downloadUrl ->
                                    println("Upload success! URL: $downloadUrl")
                                    imageUrl = downloadUrl
                                    isUploadingImage = false
                                    uploadProgress = 1f
                                },
                                onFailure = { exception ->
                                    println("Image upload failed: ${exception.message}")
                                    exception.printStackTrace()
                                    isUploadingImage = false
                                    uploadProgress = 0f
                                    selectedImageUri = null
                                },
                                onProgress = { progress ->
                                    println("Upload progress: $progress%")
                                    uploadProgress = progress / 100f
                                }
                            )
                        },
                        onDismiss = {
                            showCropDialog = false
                            selectedImageUri = null
                        }
                    )
                }

                // Basics
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Recipe Title*")
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        colors = authFieldColors(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Recipe Description*")
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        colors = authFieldColors(),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Image Upload Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Recipe Image")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(enabled = !isUploadingImage) {
                                println("Image box clicked, launching picker")
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        println("Box recomposing - isUploading: $isUploadingImage, imageUrl: $imageUrl, selectedUri: $selectedImageUri")
                        when {
                            isUploadingImage -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { uploadProgress },
                                        color = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "${(uploadProgress * 100).toInt()}%",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            imageUrl.isNotEmpty() -> {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Recipe image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    onError = { error ->
                                        println("Error loading uploaded image: ${error.result.throwable}")
                                    }
                                )
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.AddPhotoAlternate,
                                        contentDescription = "Add photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Tap to add image",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    if (imageUrl.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                imageUrl = ""
                                selectedImageUri = null
                                uploadProgress = 0f
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text("Remove Image")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(.5f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SectionLabel("Prep Time*")
                        OutlinedTextField(
                            value = if (preparationTime == 0) "" else preparationTime.toString(),
                            onValueChange = { value ->
                                preparationTime = value.toIntOrNull() ?: 0
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            colors = authFieldColors(),
                            shape = MaterialTheme.shapes.medium,
                            suffix = { Text("min") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SectionLabel("Recipe Style")
                        OutlinedTextField(
                            value = style,
                            onValueChange = { style = it },
                            colors = authFieldColors(),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Ingredients
                var showIngredientDialog by remember { mutableStateOf(false) }
                var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
                var showSelectedIngredientDialog by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel("Recipe Ingredients*")
                    FlowRow {
                        ingredients.forEach {
                            RemovableChip(
                                text = "${it.quantity.fractionString()}${abbreviateUnit(it.unit)} ${it.name}",
                                onClick = {
                                    selectedIngredient = it
                                    showSelectedIngredientDialog = true
                                },
                                onRemove = { ingredients = ingredients - it },
                            )
                        }
                    }
                    AddSectionButton("Add ingredient") { showIngredientDialog = true }
                }
                if (showIngredientDialog) {
                    IngredientInputDialog(
                        onConfirm = { ingredient ->
                            ingredients = ingredients + ingredient
                            showIngredientDialog = false
                        },
                        onDismiss = { showIngredientDialog = false }
                    )
                }
                if (showSelectedIngredientDialog && selectedIngredient != null) {
                    EditIngredientInputDialog(
                        selectedIngredient!!,
                        onConfirm = { ingredient ->
                            ingredients = ingredients - selectedIngredient!! + ingredient
                            showSelectedIngredientDialog = false
                            selectedIngredient = null
                        },
                        onDismiss = { showSelectedIngredientDialog = false },
                        onRemove = {
                            ingredients = ingredients - selectedIngredient!!
                            showSelectedIngredientDialog = false
                            selectedIngredient = null
                        }
                    )
                }

                // Steps
                var showStepDialog by remember { mutableStateOf(false) }
                var selectedStep by remember { mutableStateOf<Step?>(null) }
                var showSelectedStepDialog by remember { mutableStateOf(false) }
                if (showStepDialog) {
                    StepsInputDialog(
                        onConfirm = { newStep ->
                            steps = reindexSteps(steps + newStep)
                            showStepDialog = false
                        },
                        onDismiss = { showStepDialog = false },
                        assignedIndex = steps.size + 1,
                    )
                }
                if (showSelectedStepDialog && selectedStep != null) {
                    EditStepInputDialog(
                        selectedStep!!,
                        onConfirm = { updatedStep ->
                            steps = reindexSteps(steps.map { if (it == selectedStep) updatedStep else it })
                            showSelectedStepDialog = false
                            selectedStep = null
                        },
                        onDismiss = { showSelectedStepDialog = false },
                        onRemove = {
                            steps = reindexSteps(steps - selectedStep!!)
                            showSelectedStepDialog = false
                            selectedStep = null
                        }
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel("Recipe Steps*")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        steps.forEach { step ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                    ) {
                                        Text(
                                            text = "${step.index}. ${step.title}",
                                            color = Secondary,
                                            modifier = Modifier.padding(top = 8.dp),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = step.description,
                                            color = Secondary,
                                            modifier = Modifier.padding(bottom = 8.dp, top = 2.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                    Column {
                                        IconButton(
                                            onClick = { moveStep(step, -1) },
                                            enabled = steps.indexOf(step) > 0,
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up", tint = Secondary)
                                        }
                                        IconButton(
                                            onClick = { moveStep(step, 1) },
                                            enabled = steps.indexOf(step) < steps.size - 1,
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down", tint = Secondary)
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedStep = step
                                            showSelectedStepDialog = true
                                        },
                                        modifier = Modifier.size(32.dp).padding(end = 4.dp),
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit step", tint = Secondary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    AddSectionButton("Add step") { showStepDialog = true }
                }

                // Tags
                val showTagMenu = remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel("Recipe Tags")
                    FlowRow {
                        tags.forEach {
                            RemovableChip(
                                text = it.displayName,
                                onClick = { tags = tags - it },
                                onRemove = { tags = tags - it },
                            )
                        }
                    }
                    Box {
                        AddSectionButton("Add tag") { showTagMenu.value = true }
                        DropdownMenu(
                            expanded = showTagMenu.value,
                            onDismissRequest = { showTagMenu.value = false },
                            modifier = Modifier
                                .heightIn(max = 280.dp)
                                .fillMaxWidth(.7f)
                        ) {
                            var tagSearch by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = tagSearch,
                                onValueChange = { tagSearch = it },
                                placeholder = { Text("Search tags...") },
                                colors = authFieldColors(),
                                shape = MaterialTheme.shapes.medium,
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            )
                            Tag.entries
                                .filter { it !in tags && (it.name.contains(tagSearch, ignoreCase = true) || it.displayName.contains(tagSearch, ignoreCase = true)) }
                                .forEach { tag ->
                                    DropdownMenuItem(
                                        text = { Text(tag.displayName) },
                                        onClick = {
                                            tags = tags + tag
                                            tagSearch = ""
                                        }
                                    )
                                }
                        }
                    }
                }

                val isRecipeValid = title.isNotBlank() && description.isNotBlank() && ingredients.isNotEmpty() && steps.isNotEmpty() && preparationTime > 0

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { showPreview = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                        enabled = isRecipeValid,
                        modifier = Modifier.fillMaxWidth(.7f),
                    ) {
                        Text("Preview Recipe")
                    }
                    Button(
                        enabled = isRecipeValid && !isUploadingImage,
                        onClick = {
                            onCreate(
                                Recipe(
                                    id = 0,
                                    title = title,
                                    description = description,
                                    ingredients = ingredients,
                                    steps = steps,
                                    preparationTime = preparationTime,
                                    imageUrl = imageUrl,
                                    style = style,
                                    tags = tags,
                                    owner = Firebase.auth.currentUser?.uid ?: "Anonymous"
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Secondary,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(.7f),
                    ) {
                        Text("Create Recipe", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
    AnimatedVisibility(
        visible = showPreview,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier.zIndex(2f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .zIndex(2f)
        ) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxHeight(.9f)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large.copy(
                    bottomStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                    bottomEnd = androidx.compose.foundation.shape.CornerSize(0.dp),
                ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = { showPreview = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("Close Preview", color = Secondary)
                    }
                    SingleRecipeScreen(
                        recipe = Recipe(
                            id = 0,
                            title = title,
                            description = description,
                            ingredients = ingredients,
                            steps = steps,
                            preparationTime = preparationTime,
                            imageUrl = imageUrl,
                            style = style,
                            tags = tags,
                        ),
                        model = RecipeViewModel(),
                        saveable = false
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRecipeScreenPreview() {
    RecipeFinderTheme {
        CreateRecipeScreen(onCreate = {})
    }

}
