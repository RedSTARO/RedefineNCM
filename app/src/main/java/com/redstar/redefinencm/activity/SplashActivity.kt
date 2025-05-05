package com.redstar.redefinencm.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.activity.MainActivity.MainActivity
import com.redstar.redefinencm.activity.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var cookie by remember { mutableStateOf("") }
                    LaunchedEffect(Unit) {
                        checkAppUpdate()
                    }
                    cookie = runBlocking {
                        DataStoreManager.getAppDataStore().data.first()[stringPreferencesKey(
                            "cookie"
                        )] ?: ""
                    }
                    this.startActivity(
                        Intent(
                            this,
                            if (cookie.isBlank()) LoginActivity::class.java else MainActivity::class.java
                        )
                    )
                }
            }
        }
    }
}

fun checkAppUpdate() {
    try {
        if (BuildConfig.DEBUG) {
            val url = "https://api.github.com/repos/RedSTARO/RedefineNCM/commits/master"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // 捕获在请求失败时发生的异常
                    if (BuildConfig.DEBUG) {
                        try {
                            Log.e("UpdateCheck", "Failed to fetch latest commit", e)
                        } catch (exception: Exception) {
                            Log.e("UpdateCheck", "Error in failure callback", exception)
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        response.body?.string()?.let { responseBody ->
                            val jsonObject = JSONObject(responseBody)
                            val latestCommitSha = jsonObject.getString("sha")
                            val savedSha = BuildConfig.GIT_SHA

                            if (latestCommitSha != savedSha) {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        RedefineNCMApplication.getApplicationContext() as Context,
                                        "New version is out, check updates!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } catch (exception: Exception) {
                        if (BuildConfig.DEBUG) {
                            Log.e("UpdateCheck", "Error parsing response", exception)
                        }
                    }
                }
            })
        } else {
            val url = "https://api.github.com/repos/RedSTARO/RedefineNCM/releases/latest"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (BuildConfig.DEBUG) {
                        try {
                            Log.e("UpdateCheck", "Failed to fetch update info", e)
                        } catch (exception: Exception) {
                            Log.e("UpdateCheck", "Error in failure callback", exception)
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        response.body?.string()?.let { responseBody ->
                            val jsonObject = JSONObject(responseBody)
                            val latestVersion = jsonObject.getString("tag_name")
                            val currentVersion = "v_${BuildConfig.RELEASE_VER}"

                            if (latestVersion != currentVersion) {
                                // 发现新版本，通知用户
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        RedefineNCMApplication.getApplicationContext() as Context,
                                        "发现新版本：$latestVersion",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } catch (exception: Exception) {
                        if (BuildConfig.DEBUG) {
                            Log.e("UpdateCheck", "Error parsing response", exception)
                        }
                    }
                }
            })
        }
    } catch (e: Exception) {
        Toast.makeText(
            RedefineNCMApplication.getApplicationContext() as Context,
            "Unable to check cause ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}