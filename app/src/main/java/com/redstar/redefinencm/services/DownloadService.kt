package com.redstar.redefinencm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream

data class DownloadStatus(val id: Long, val fileName: String, val progress: Int)

class DownloadService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val retrofit: NCMApi = RetrofitInstance.retrofit.create(NCMApi::class.java)
    private val client = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

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
        updateStatus(DownloadStatus(data.id, fileName, 0))
        notifyProgress(data.id.toInt(), fileName, 0)

        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return
            val outFile = DownloadStorage.createFile(fileName)
            response.body?.let { body ->
                val total = body.contentLength()
                val input = body.byteStream()
                val output = FileOutputStream(outFile)
                var bytesRead = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    bytesRead += read
                    val progress = if (total > 0) ((bytesRead * 100) / total).toInt() else 0
                    updateStatus(DownloadStatus(data.id, fileName, progress))
                    notifyProgress(data.id.toInt(), fileName, progress)
                }
                output.flush()
                output.close()
                input.close()
            }
        }
        updateStatus(DownloadStatus(data.id, fileName, 100))
        notifyFinished(data.id.toInt(), fileName)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_IDS = "extra_ids"
        private const val TAG = "DownloadService"
        private const val CHANNEL_ID = "download_channel"

        val statuses = MutableStateFlow<List<DownloadStatus>>(emptyList())

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

    private fun updateStatus(status: DownloadStatus) {
        statuses.update { list ->
            val mutable = list.toMutableList()
            val index = mutable.indexOfFirst { it.id == status.id }
            if (index >= 0) {
                mutable[index] = status
            } else {
                mutable.add(status)
            }
            mutable
        }
    }

    private fun notifyProgress(id: Int, title: String, progress: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .build()
        NotificationManagerCompat.from(this).notify(id, notification)
    }

    private fun notifyFinished(id: Int, title: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentText("Download complete")
            .build()
        NotificationManagerCompat.from(this).notify(id, notification)
    }
}
