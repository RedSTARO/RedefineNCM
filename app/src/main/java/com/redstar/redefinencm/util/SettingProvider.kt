package com.redstar.redefinencm.util

import android.util.Log
import com.redstar.redefinencm.RedefineNCMApplication
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.File
import android.content.Intent
import androidx.core.content.FileProvider

object SettingProvider {
    lateinit var cookie: String
    lateinit var onlinePlayQuality: String
    lateinit var downloadQuality: String
    var statusBarLyric = false
    var replacePlaylist = false
    var checkUpdate = false

    private const val FILE_NAME = "RedefineNCM_app_settings.json"

    init {
        initSetting()
    }

    fun initSetting() {
        runBlocking {
            cookie = DataStoreManager.getStringItem("cookie", "")
            onlinePlayQuality = DataStoreManager.getStringItem("onlinePlayQuality", SoundQuality.STANDARD.name)
            downloadQuality = DataStoreManager.getStringItem("downloadQuality", SoundQuality.STANDARD.name)
            statusBarLyric = DataStoreManager.getBooleanItem("statusBarLyric", false)
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
            put("statusBarLyric", statusBarLyric)
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
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 加在 chooser 上

        context.startActivity(chooserIntent)
    }


    fun importAppSetting() {
        val context = RedefineNCMApplication.getApplicationContext()
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            Log.w("SettingProvider", "Settings file does not exist.")
            return
        }

        val content = file.readText()
        val json = JSONObject(content)

        cookie = json.getString("cookie")
        onlinePlayQuality = json.getString("onlinePlayQuality")
        downloadQuality = json.getString("downloadQuality")
        statusBarLyric = json.getBoolean("statusBarLyric")
        replacePlaylist = json.getBoolean("replacePlaylist")
        checkUpdate = json.getBoolean("checkUpdate")

        Log.d("SettingProvider", "Settings imported from ${file.absolutePath}")
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
    JYMASTER("超清母带"),
    ;

    override fun toString(): String = displayName
}
