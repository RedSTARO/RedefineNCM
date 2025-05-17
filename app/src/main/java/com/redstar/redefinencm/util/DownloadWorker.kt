package com.redstar.redefinencm.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import android.app.DownloadManager
import androidx.core.net.toUri
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.api.data.SongUrlV1Data
import com.redstar.redefinencm.data.api.safeApiCall

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)

    override suspend fun doWork(): Result {
        val idArray = inputData.getLongArray("listOfSongId") ?: return Result.failure()
        val ids = idArray.toList()

        return try {
            val urls = getUrlsForIds(ids)

            val batches = urls.chunked(5)
            for (batch in batches) {
                coroutineScope {
                    batch.map { eachSong ->
                        async {
                            enqueueDownload(eachSong.url, eachSong.id.toString())
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

        val request = DownloadManager.Request(uri).apply {
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/RedefineNCM/" + fileName)
        }

        downloadManager.enqueue(request)
    }

    private suspend fun getUrlsForIds(ids: List<Long>): List<SongUrlV1Data> {
        val response = safeApiCall {
            retrofit.songUrlV1(
                ids,
                DataStoreManager.getStringItem("downloadQuality", "standard")
            )
        }
        return response?.data?.map { it } ?: emptyList()
    }
}
