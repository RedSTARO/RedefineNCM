package com.redstar.redefinencm.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SettingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(
                    floatingActionButton = {
                        // You can add a floating action button if needed
                    },
                ) { innerPadding ->
                    Surface {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            ServerItem()
                            Spacer(modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            TextItem("cookie", "Account Cookie")

                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TextItem(settingItemKey: String, hintText: String) {
    // 使用 remember 来保持状态
    var settingValue by remember { mutableStateOf("") }

    // 获取当前 Context 和 CoroutineScope
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 读取 dataStore 中的 settingItem 信息，只在首次加载时执行
    LaunchedEffect(settingItemKey) {
        settingValue = context.dataStore.data
            .firstOrNull()?.get(stringPreferencesKey(settingItemKey)) ?: ""
    }

    // 保存到 dataStore
    val saveToDataStore: (String) -> Unit = { newValue ->
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey(settingItemKey)] = newValue
            }
        }
    }

    // 处理 TextField 输入框更新
    OutlinedTextField(
        label = { Text(hintText) },
        value = settingValue, // 使用 settingValue 作为输入框的值
        onValueChange = { newValue ->
            settingValue = newValue // 更新本地状态
            saveToDataStore(newValue) // 保存新的值到 dataStore
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(64.dp), // 保证文本框固定高度
        singleLine = true // 确保文本框单行显示
    )
}

@Composable
fun ServerItem() {
    val settingItemKey = "server"
    val hintText = "Server"
    var settingValue by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var status by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf("") }

    // 读取 dataStore 中的 settingItem 信息，只在首次加载时执行
    LaunchedEffect(settingItemKey) {
        settingValue = context.dataStore.data
            .firstOrNull()?.get(stringPreferencesKey(settingItemKey)) ?: ""
    }

    // 处理 TextField 输入框更新
    OutlinedTextField(
        label = { Text(hintText) },
        value = settingValue, // 使用 settingValue 作为输入框的值
        onValueChange = { newValue ->
            settingValue = newValue // 更新本地状态
            if (!settingValue.endsWith("/")) {
                settingValue = "$settingValue/"
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(64.dp), // 保证文本框固定高度
        singleLine = true // 确保文本框单行显示
    )

    Button(onClick = {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                Log.d("SettingActivity", "Save server at $settingValue")
                saveToDataStore(settingItemKey, settingValue, context)
                status = checkServerAvailable(settingValue)
                data = checkServerVersion(settingValue)
            } catch (e: Exception) {
                data = e.message.toString()
            }
        }
    }) {
        Text("Check server at $settingValue")
    }

    if (status) {
        Text("Server version: $data, OK")
    } else {
        Text("Server unavailable, message: $data")
    }
}


suspend fun checkServerAvailable(server: String): Boolean {
    println("$server/inner/version/")

    // Create a new Retrofit instance with the provided server URL
    val retrofit = Retrofit.Builder()
        .baseUrl(server) // Use the passed server URL directly
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build()) // Basic OkHttpClient without custom interceptors
        .build()

    val api = retrofit.create(NCMApi::class.java)
    return try {
        val code = api.innerVersion("${server}inner/version/").code
        code == 200
    } catch (e: Exception) {
        false
    }
}

suspend fun checkServerVersion(server: String): String {
    // Create a new Retrofit instance with the provided server URL
    val retrofit = Retrofit.Builder()
        .baseUrl(server) // Use the passed server URL directly
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build()) // Basic OkHttpClient without custom interceptors
        .build()

    val api = retrofit.create(NCMApi::class.java)
    return try {
        api.innerVersion("${server}inner/version/").data.version
    } catch (e: Exception) {
        e.message.toString()
    }
}

suspend fun saveToDataStore(key: String, value: String, context: Context) {
    context.dataStore.edit { preferences ->
        preferences[stringPreferencesKey(key)] = value
    }
}