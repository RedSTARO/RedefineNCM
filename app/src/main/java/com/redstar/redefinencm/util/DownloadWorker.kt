package com.redstar.redefinencm.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.data.Repository
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.api.data.SongUrlV1Data
import com.redstar.redefinencm.data.api.safeApiCall
import com.redstar.redefinencm.data.db.DatabaseProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val downloadManager =
        appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)

    override suspend fun doWork(): Result {
        val idArray = inputData.getLongArray("listOfSongId") ?: return Result.failure()
        val ids = idArray.toList()

        return try {
            val urls = getUrlsForIds(ids)
            Log.d("DownloadSong", urls.toString())

            val batches = urls.chunked(5)
            for (batch in batches) {
                coroutineScope {
                    batch.map { eachSong ->
                        async {
                            Log.d("DownloadSong", eachSong.toString())
                            if (!DownloadUtil.fileAlreadyExistsByBaseName(eachSong.id.toString())){
                                enqueueDownload(eachSong.url, eachSong.id.toString())
                            }
                        }
                    }.awaitAll()
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun enqueueDownload(url: String, fileName: String) {
        val uri = url.toUri()
        val fileName = fileName + "." + uri.lastPathSegment?.substringAfterLast(".")

        if (DownloadUtil.fileAlreadyExists(fileName)) {
            Log.d("DownloadWorker", "File already exists: $fileName")
            return
        }

        val request = DownloadManager.Request(uri).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/RedefineNCM/$fileName",
            )
        }

        downloadManager.enqueue(request)
    }

    private fun getUrlsForIds(ids: List<Long>): List<SongUrlV1Data> {
        val repo = Repository(DatabaseProvider.getDao(RedefineNCMApplication.getApplicationContext()))
        val response = repo.getDownloadBatchSongUris(ids)
        Log.d("DownloadSong", response.toString())
        return response.map { it }
    }

}

object DownloadUtil {
    fun fileAlreadyExists(fileName: String): Boolean {
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/RedefineNCM")
        val file = java.io.File(dir, fileName)
        return file.exists()
    }

    fun fileAlreadyExistsByBaseName(baseName: String): Boolean {
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/RedefineNCM")
        if (!dir.exists() || !dir.isDirectory) return false

        return dir.listFiles()?.any { file ->
            file.nameWithoutExtension == baseName
        } ?: false
    }

    fun getExistingFileUriByBaseName(baseName: String): Uri? {
        val dir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/RedefineNCM")
        if (!dir.exists() || !dir.isDirectory) return null

        val matchedFile = dir.listFiles()?.firstOrNull { file ->
            file.nameWithoutExtension == baseName
        }

        return matchedFile?.let { Uri.fromFile(it) }
    }
}
