package com.redstar.redefinencm.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.data.api.RetrofitInstance
import com.redstar.redefinencm.data.api.data.SongUrlV1Data
import com.redstar.redefinencm.data.api.safeApiCall
import com.redstar.redefinencm.util.DownloadStorage
import com.redstar.redefinencm.util.SettingProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream

class DownloadService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)
    private val client = OkHttpClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ids = intent?.getLongArrayExtra(EXTRA_IDS)?.toList() ?: emptyList()
        if (ids.isNotEmpty()) {
            scope.launch { handleDownload(ids) }
        }
        return START_NOT_STICKY
    }

    private suspend fun handleDownload(ids: List<Long>) {
        val urls = getUrlsForIds(ids)
        urls.forEach { data ->
            downloadFile(data)
        }
        stopSelf()
    }

    private suspend fun getUrlsForIds(ids: List<Long>): List<SongUrlV1Data> {
        val response = safeApiCall {
            retrofit.songUrlV1(ids, SettingProvider.downloadQuality)
        }
        return response?.data ?: emptyList()
    }

    private fun downloadFile(data: SongUrlV1Data) {
        val url = data.url ?: return
        val uri = url.toUri()
        val fileName = data.id.toString() + "." + (uri.lastPathSegment?.substringAfterLast('.') ?: "mp3")

        if (DownloadStorage.fileAlreadyExists(fileName)) {
            Log.d(TAG, "File exists: $fileName")
            return
        }

        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return
            val outFile = DownloadStorage.createFile(fileName)
            val sink = FileOutputStream(outFile)
            response.body?.byteStream()?.use { input ->
                sink.use { output -> input.copyTo(output) }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_IDS = "extra_ids"
        private const val TAG = "DownloadService"

        fun start(context: Context, ids: List<Long>) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_IDS, ids.toLongArray())
            }
            context.startService(intent)
        }

        fun getDownloadedUri(id: Long): Uri? {
            return DownloadStorage.getExistingFileUriByBaseName(id.toString())
        }
    }
}
