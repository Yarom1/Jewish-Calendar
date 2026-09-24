package com.yarom.jewishcalendar.ui.screens.addevent

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yarom.jewishcalendar.ui.theme.BrassGold
import com.yarom.jewishcalendar.ui.theme.DeepTeal
import kotlin.math.min

private const val FRAME_SIZE_PX = 640f
private const val OUTPUT_SIZE_PX = 640

/** Builds the Matrix mapping [source]'s own pixels onto the [FRAME_SIZE_PX]-square frame, given
 * the current pinch-zoom [userScale] and drag [userOffset] - reused identically for the live
 * preview and the final crop, so what's shown is exactly what gets saved. */
private fun cropMatrix(source: android.graphics.Bitmap, userScale: Float, userOffset: Offset): android.graphics.Matrix {
    val baseScale = FRAME_SIZE_PX / min(source.width, source.height).toFloat()
    val effectiveScale = baseScale * userScale
    return android.graphics.Matrix().apply {
        postScale(effectiveScale, effectiveScale)
        postTranslate(
            FRAME_SIZE_PX / 2f - source.width * effectiveScale / 2f + userOffset.x,
            FRAME_SIZE_PX / 2f - source.height * effectiveScale / 2f + userOffset.y,
        )
    }
}

/** Pinch-zoom/drag crop to a fixed square, matching a printed contact photo (spec follow-up:
 * event photo attachment). [source] is the freshly captured/picked full-size photo. */
@Composable
fun ImageCropDialog(
    source: android.graphics.Bitmap,
    onCancel: () -> Unit,
    onCropped: (android.graphics.Bitmap) -> Unit,
) {
    var userScale by remember { mutableFloatStateOf(1f) }
    var userOffset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        userScale = (userScale * zoomChange).coerceIn(1f, 5f)
        userOffset += panChange
    }
    val paint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
    }

    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("חיתוך תמונה", style = MaterialTheme.typography.titleMedium, color = DeepTeal)
            Text(
                "צביטה להגדלה, גרירה למיקום",
                style = MaterialTheme.typography.bodySmall,
                color = DeepTeal.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            val frameSizeDp = with(LocalDensity.current) { FRAME_SIZE_PX.toDp() }
            Canvas(
                modifier = Modifier
                    .size(frameSizeDp)
                    .clip(RoundedCornerShape(8.dp))
                    .transformable(state = transformState),
            ) {
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawBitmap(source, cropMatrix(source, userScale, userOffset), paint)
                }
            }
            Row(modifier = Modifier.padding(top = 16.dp)) {
                TextButton(onClick = onCancel) {
                    Text("ביטול", color = DeepTeal)
                }
                Button(
                    onClick = {
                        val output = android.graphics.Bitmap.createBitmap(
                            OUTPUT_SIZE_PX,
                            OUTPUT_SIZE_PX,
                            android.graphics.Bitmap.Config.ARGB_8888,
                        )
                        android.graphics.Canvas(output).drawBitmap(source, cropMatrix(source, userScale, userOffset), paint)
                        onCropped(output)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Text("שמירה", color = BrassGold)
                }
            }
        }
    }
}
