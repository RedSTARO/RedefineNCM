package com.redstar.redefinencm.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.redstar.redefinencm.RedefineNCMApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.File

object SettingProvider {
    lateinit var cookie: String
    lateinit var onlinePlayQuality: String
    lateinit var downloadQuality: String
    var replacePlaylist = false
    var checkUpdate = false

    private const val FILE_NAME = "RedefineNCM_app_settings.json"

    init {
        initSetting()
    }

    fun updateCookie(newCookie: String) {
        cookie = newCookie
        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.setStringItem("cookie", cookie)
        }
    }

    fun updateOnlinePlayQuality(newQuality: SoundQuality) {
        onlinePlayQuality = newQuality.name
        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.setStringItem("onlinePlayQuality", onlinePlayQuality)
        }
    }

    fun updateDownloadQuality(newQuality: SoundQuality) {
        downloadQuality = newQuality.name
        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.setStringItem("downloadQuality", downloadQuality)
        }
    }

    

    fun updateReplacePlaylist(newStatus: Boolean) {
        replacePlaylist = newStatus
        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.setBooleanItem("replacePlaylist", replacePlaylist)
        }
    }

    fun updateCheckUpdate(newStatus: Boolean) {
        checkUpdate = newStatus
        CoroutineScope(Dispatchers.IO).launch {
            DataStoreManager.setBooleanItem("checkUpdate", checkUpdate)
        }
    }

    fun initSetting() {
        runBlocking {
            cookie = DataStoreManager.getStringItem("cookie", "")
            onlinePlayQuality =
                DataStoreManager.getStringItem("onlinePlayQuality", SoundQuality.STANDARD.name)
            downloadQuality =
                DataStoreManager.getStringItem("downloadQuality", SoundQuality.STANDARD.name)
            replacePlaylist = DataStoreManager.getBooleanItem("replacePlaylist", false)
            checkUpdate = DataStoreManager.getBooleanItem("checkUpdate", false)
        }
    }

    fun exportAppSetting() {
        val context = RedefineNCMApplication.getApplicationContext()

        val json = JSONObject().apply {
            put("cookie", cookie)
            put("onlinePlayQuality", onlinePlayQuality)
            put("downloadQuality", downloadQuality)
            put("replacePlaylist", replacePlaylist)
            put("checkUpdate", checkUpdate)
        }

        val cacheFile = File(context.cacheDir, FILE_NAME)
        cacheFile.writeText(json.toString())

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "导出设置文件")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooserIntent)
    }

    /**
     * 启动系统文件选择器，选择 JSON 设置文件
     */
    fun startImportSetting(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        launcher.launch(Intent.createChooser(intent, "选择设置文件"))
    }

    /**
     * 通过 Uri 导入设置
     */
    fun importAppSettingFromUri(context: Context, uri: Uri, callback: () -> Unit = {}) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.use { it.readText() } ?: return
            val json = JSONObject(content)

            cookie = json.getString("cookie")
            onlinePlayQuality = json.getString("onlinePlayQuality")
            downloadQuality = json.getString("downloadQuality")
            replacePlaylist = json.getBoolean("replacePlaylist")
            checkUpdate = json.getBoolean("checkUpdate")

            // 同步存储
            CoroutineScope(Dispatchers.IO).launch {
                DataStoreManager.setStringItem("cookie", cookie)
                DataStoreManager.setStringItem("onlinePlayQuality", onlinePlayQuality)
                DataStoreManager.setStringItem("downloadQuality", downloadQuality)
                DataStoreManager.setBooleanItem("replacePlaylist", replacePlaylist)
                DataStoreManager.setBooleanItem("checkUpdate", checkUpdate)
            }

            Log.d("SettingProvider", "Settings imported from Uri")
            callback()
        } catch (e: Exception) {
            Log.e("SettingProvider", "Failed to import settings", e)
        }
    }
}

enum class SoundQuality(private val displayName: String) {
    STANDARD("标准"),
    HIGHER("较高"),
    EXHIGH("极高"),
    LOSSLESS("无损"),
    HIRES("Hi-Res"),
    JYEFFECT("高清环绕声"),
    SKY("沉浸环绕声"),
    DOLBY("杜比全景声"),
    JYMASTER("超清母带");

    override fun toString(): String = displayName
}
