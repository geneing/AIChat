package com.github.geneing.aichat.core.data.file

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores user-attached files (images, audio) in app-private internal
 * storage so the URI remains valid for the lifetime of the chat even
 * after the source picker revokes its grant.
 */
@Singleton
class AttachmentStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val root: File by lazy {
        File(context.filesDir, "attachments").apply { mkdirs() }
    }

    fun imagesDir(): File = File(root, "images").apply { mkdirs() }
    fun audioDir(): File = File(root, "audio").apply { mkdirs() }

    /**
     * Copies the bytes behind [source] into app storage and returns a
     * content:// uri pointing at the new file. The content provider
     * needs to be defined if the URI is shared across processes, but
     * for same-app use a file:// uri via FileProvider is sufficient.
     * Here we just return the absolute path wrapped in a file Uri.
     */
    fun importImage(source: Uri, extension: String = "jpg"): Uri {
        val outFile = File(imagesDir(), "${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(source)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not open source: $source")
        return Uri.fromFile(outFile)
    }

    fun importAudio(source: Uri, extension: String = "m4a"): Uri {
        val outFile = File(audioDir(), "${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(source)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not open source: $source")
        return Uri.fromFile(outFile)
    }

    fun delete(uri: Uri) {
        runCatching {
            uri.path?.let { File(it).delete() }
        }
    }
}
