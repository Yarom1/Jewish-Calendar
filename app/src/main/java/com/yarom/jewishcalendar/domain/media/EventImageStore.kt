package com.yarom.jewishcalendar.domain.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Where a cropped event photo lives in app-private storage, and the temp file a camera capture
 * writes into before it's cropped (spec follow-up: event photo attachment).
 */
object EventImageStore {

    private const val CAPTURE_DIR = "event_photo_captures"
    private const val STORE_DIR = "event_images"

    /** A fresh content:// Uri (via FileProvider) for the camera app to write a full-size capture
     * into - must live under the cache dir declared in res/xml/file_paths.xml. */
    fun newCaptureUri(context: Context): Uri {
        val dir = File(context.cacheDir, CAPTURE_DIR).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Saves the already-cropped [bitmap] as a new file under app-private storage, deleting
     * [previousPath]'s file first if this replaces an existing photo. Returns the new path. */
    fun save(context: Context, bitmap: Bitmap, previousPath: String?): String {
        previousPath?.let { delete(it) }
        val dir = File(context.filesDir, STORE_DIR).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    /** Best-effort delete - a missing/already-gone file isn't an error. */
    fun delete(path: String) {
        runCatching { File(path).delete() }
    }

    /** Decodes [uri] (camera capture or gallery pick) and applies its EXIF rotation, since a
     * camera photo is often stored "sideways" with the real orientation only in EXIF metadata. */
    fun decodeOrientedBitmap(context: Context, uri: Uri): Bitmap? = runCatching {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return@runCatching null
        val rotationDegrees = context.contentResolver.openInputStream(uri)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        if (rotationDegrees == 0f) {
            bitmap
        } else {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }.getOrNull()
}
