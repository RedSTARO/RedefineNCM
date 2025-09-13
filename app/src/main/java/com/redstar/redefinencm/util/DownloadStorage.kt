package com.redstar.redefinencm.util

import android.net.Uri
import android.os.Environment
import java.io.File

object DownloadStorage {
    private fun downloadDir(): File {
        return Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/RedefineNCM"
        )
    }

    fun fileAlreadyExists(fileName: String): Boolean {
        val file = File(downloadDir(), fileName)
        return file.exists()
    }

    fun fileAlreadyExistsByBaseName(baseName: String): Boolean {
        val dir = downloadDir()
        if (!dir.exists() || !dir.isDirectory) return false
        return dir.listFiles()?.any { it.nameWithoutExtension == baseName } ?: false
    }

    fun getExistingFileUriByBaseName(baseName: String): Uri? {
        val dir = downloadDir()
        if (!dir.exists() || !dir.isDirectory) return null
        val matchedFile = dir.listFiles()?.firstOrNull { it.nameWithoutExtension == baseName }
        return matchedFile?.let { Uri.fromFile(it) }
    }

    fun listDownloadedFiles(): List<File> {
        val dir = downloadDir()
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        return dir.listFiles()?.toList() ?: emptyList()
    }

    fun createFile(fileName: String): File {
        val dir = downloadDir()
        dir.mkdirs()
        return File(dir, fileName)
    }
}
