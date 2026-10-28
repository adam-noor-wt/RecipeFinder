package com.example.recipefinder.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.recipefinder.ui.theme.Secondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/**
 * Dialog that displays an image with pan and zoom controls,
 * allowing the user to crop it to a square.
 */
@Composable
fun ImageCropDialog(
    imageUri: Uri,
    onConfirm: (Bitmap) -> kotlin.Unit,
    onDismiss: () -> kotlin.Unit
) {
    val context = LocalContext.current
    val bitmap = remember(imageUri) { loadBitmapFromUri(context, imageUri) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotatedBitmap by remember(bitmap, rotationDegrees) {
        mutableStateOf(bitmap?.let { rotateBitmap(it, rotationDegrees) })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = {
            Text("Adjust Image", color = Secondary, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Pan and zoom to frame your image \n Please note any inappropriate imagery will be flagged for deletion.",
                    color = Secondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                // Crop preview area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                ) {
                    rotatedBitmap?.let { bmp ->
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .pointerInput(kotlin.Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 3f)

                                        val canvasSize = min(this@pointerInput.size.width, this@pointerInput.size.height).toFloat()
                                        val scaledWidth = canvasSize * scale
                                        val scaledHeight = canvasSize * scale

                                        // Calculate max offset to prevent image from going beyond borders
                                        val maxOffsetX = (scaledWidth - canvasSize) / 2
                                        val maxOffsetY = (scaledHeight - canvasSize) / 2

                                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    }
                                }
                        ) {
                            val canvasSize = size.minDimension
                            val imageBitmap = bmp.asImageBitmap()

                            // Calculate scaled dimensions
                            val scaledWidth = canvasSize * scale
                            val scaledHeight = canvasSize * scale

                            // Draw the image
                            drawImage(
                                image = imageBitmap,
                                dstOffset = IntOffset(
                                    x = ((canvasSize - scaledWidth) / 2 + offsetX).toInt(),
                                    y = ((canvasSize - scaledHeight) / 2 + offsetY).toInt()
                                ),
                                dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
                            )

                            // Draw crop frame overlay
                            drawRect(
                                color = Color.White,
                                topLeft = Offset.Zero,
                                size = Size(canvasSize, canvasSize),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }

                // Rotation controls
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            rotationDegrees = (rotationDegrees - 90f) % 360f
                            // Reset pan and zoom on rotation
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(Icons.Filled.RotateLeft, contentDescription = "Rotate left", tint = Secondary)
                    }
                    Text(
                        text = "Rotate",
                        color = Secondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    IconButton(
                        onClick = {
                            rotationDegrees = (rotationDegrees + 90f) % 360f
                            // Reset pan and zoom on rotation
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                    ) {
                        Icon(Icons.Filled.RotateRight, contentDescription = "Rotate right", tint = Secondary)
                    }
                }

                // Zoom controls
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            val newScale = (scale - 0.1f).coerceIn(1f, 3f)
                            val canvasSize = 1f // Normalized size
                            val scaledWidth = canvasSize * newScale
                            val scaledHeight = canvasSize * newScale
                            val maxOffsetX = (scaledWidth - canvasSize) / 2
                            val maxOffsetY = (scaledHeight - canvasSize) / 2
                            offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                            scale = newScale
                        }
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Zoom out", tint = Secondary)
                    }
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        color = Secondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(
                        onClick = { scale = (scale + 0.1f).coerceIn(1f, 3f) }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Zoom in", tint = Secondary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    rotatedBitmap?.let { bmp ->
                        val croppedBitmap = cropBitmapToSquare(bmp, scale, offsetX, offsetY)
                        onConfirm(croppedBitmap)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = Color.White
                )
            ) {
                Text("Crop & Upload")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = Secondary)
            }
        }
    )
}

/**
 * Loads a bitmap from a URI, scaling it down if necessary to avoid memory issues.
 */
private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply {
                // First decode with inJustDecodeBounds=true to check dimensions
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, this)
            }

            // Calculate inSampleSize to scale down large images
            val maxSize = 2048
            var inSampleSize = 1
            while (options.outWidth / inSampleSize > maxSize ||
                   options.outHeight / inSampleSize > maxSize) {
                inSampleSize *= 2
            }

            // Decode with the calculated inSampleSize
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }.let { opts ->
                    BitmapFactory.decodeStream(stream, null, opts)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Crops the bitmap to a square based on the user's zoom and pan settings.
 */
private fun cropBitmapToSquare(
    bitmap: Bitmap,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): Bitmap {
    val outputSize = 1024 // Output square size

    // Calculate the visible area of the original bitmap
    val bitmapAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
    val smallerDim = min(bitmap.width, bitmap.height)

    // Calculate scale factor from display to bitmap coordinates
    val bitmapScale = smallerDim.toFloat() / outputSize.toFloat()

    // Convert display offsets to bitmap coordinates
    val scaledOffsetX = -offsetX * bitmapScale / scale
    val scaledOffsetY = -offsetY * bitmapScale / scale

    // Calculate crop dimensions in bitmap space
    val cropSize = (smallerDim / scale).toInt()

    // Calculate centered position with offset
    val centerX = bitmap.width / 2
    val centerY = bitmap.height / 2

    val cropX = (centerX - cropSize / 2 + scaledOffsetX).toInt()
        .coerceIn(0, bitmap.width - cropSize)
    val cropY = (centerY - cropSize / 2 + scaledOffsetY).toInt()
        .coerceIn(0, bitmap.height - cropSize)

    // Crop and scale to output size
    val croppedBitmap = Bitmap.createBitmap(
        bitmap,
        cropX,
        cropY,
        cropSize.coerceIn(1, bitmap.width - cropX),
        cropSize.coerceIn(1, bitmap.height - cropY)
    )

    return Bitmap.createScaledBitmap(croppedBitmap, outputSize, outputSize, true)
}

/**
 * Rotates a bitmap by the specified degrees (90, 180, 270, etc.)
 */
private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap

    val matrix = Matrix().apply {
        postRotate(degrees)
    }

    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}
