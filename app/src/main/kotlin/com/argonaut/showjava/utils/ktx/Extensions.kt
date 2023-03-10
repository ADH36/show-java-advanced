

package com.argonaut.showjava.utils.ktx

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import java.io.File
import java.io.InputStream
import java.io.Serializable

/**
 * Convert an [InputStream] to a file given a path as a [String]
 */
fun InputStream.toFile(path: String) {
    toFile(File(path))
}

/**
 * Convert an [InputStream] to a file given the destination [File]
 */
fun InputStream.toFile(file: File) {
    file.outputStream().use { this.copyTo(it) }
}

/**
 * Convert a [Map] to a bundle by iterating through the map entries
 */
fun <V> Map<String, V>.toBundle(bundle: Bundle = Bundle()): Bundle = bundle.apply {
    forEach {
        val k = it.key
        val v = it.value
        when (v) {
            is IBinder -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    putBinder(k, v)
                }
            }
            is Bundle -> putBundle(k, v)
            is Byte -> putByte(k, v)
            is ByteArray -> putByteArray(k, v)
            is String -> putString(k, v)
            is Char -> putChar(k, v)
            is CharArray -> putCharArray(k, v)
            is CharSequence -> putCharSequence(k, v)
            is Float -> putFloat(k, v)
            is FloatArray -> putFloatArray(k, v)
            is Parcelable -> putParcelable(k, v)
            is Serializable -> putSerializable(k, v)
            is Short -> putShort(k, v)
            is ShortArray -> putShortArray(k, v)
            is Boolean -> putBoolean(k, v)
            is Int -> putInt(k, v)
            is IntArray -> putIntArray(k, v)
        }
    }
}

/**
 * Get a circular [Bitmap] from the original
 *
 * Borrowed from: https://stackoverflow.com/a/46613094/1562480 (Yuriy Seredyuk)
 */
fun Bitmap.getCircularBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    // circle configuration
    val circlePaint = Paint().apply { isAntiAlias = true }
    val circleRadius = Math.max(width, height) / 2f

    // output bitmap
    val outputBitmapPaint = Paint(circlePaint).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN) }
    val outputBounds = Rect(0, 0, width, height)
    val output = Bitmap.createBitmap(width, height, config)

    return Canvas(output).run {
        drawCircle(circleRadius, circleRadius, circleRadius, circlePaint)
        drawBitmap(this@getCircularBitmap, outputBounds, outputBounds, outputBitmapPaint)
        output
    }
}

