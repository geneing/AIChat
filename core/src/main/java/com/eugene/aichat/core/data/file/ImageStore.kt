package com.eugene.aichat.core.data.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Downsamples images before they are sent to the model so a 12 MP
 * camera photo doesn't end up as a 40 MB base64 string.
 */
@Singleton
class ImageStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Reads the dimensions of the image at [uri] without loading the
     * full bitmap into memory.
     */
    fun readDimensions(uri: Uri): Pair<Int, Int>? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        }
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null
        return opts.outWidth to opts.outHeight
    }

    fun loadDownsampled(
        uri: Uri,
        maxDimension: Int = MAX_DIMENSION_PX
    ): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: return null
        val sample = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, max: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= max && h / 2 >= max) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    companion object {
        const val MAX_DIMENSION_PX = 2048
    }
}
