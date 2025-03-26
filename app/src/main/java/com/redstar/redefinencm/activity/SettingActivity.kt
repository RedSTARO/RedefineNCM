package com.redstar.redefinencm.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
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
//                            standard => 标准,higher => 较高, exhigh=>极高, lossless=>无损, hires=>Hi-Res, jyeffect => 高清环绕声, sky => 沉浸环绕声, dolby => 杜比全景声, jymaster => 超清母带
                            val soundQuality = mapOf(
                                "standard" to "标准",
                                "higher" to "较高",
                                "exhigh" to "极高",
                                "lossless" to "无损",
                                "hires" to "Hi-Res",
                                "jyeffect" to "高清环绕声",
                                "sky" to "沉浸环绕声",
                                "dolby" to "杜比全景声",
                                "jymaster" to "超清母带"
                            )
                            SelectItem("onlinePlayQuality", "Music Quality Online", soundQuality)
                            SelectItem("downloadQuality", "Music Quality Download", soundQuality)
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

    // 处理 TextField 输入框更新
    OutlinedTextField(
        label = { Text(hintText) },
        value = settingValue, // 使用 settingValue 作为输入框的值
        onValueChange = { newValue ->
            settingValue = newValue // 更新本地状态
            scope.launch {
                saveToDataStore(settingItemKey, newValue, context) // 保存新的值到 dataStore
            }

        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(64.dp), // 保证文本框固定高度
        singleLine = true // 确保文本框单行显示
    )
}

@Composable
fun SelectItem(settingItemKey: String, hintText: String, valueAndItemMap: Map<String, String>) {
    var itemSelected by remember { mutableStateOf(hintText) }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(settingItemKey) {
        itemSelected = context.dataStore.data
            .firstOrNull()?.get(stringPreferencesKey(settingItemKey)) ?: ""
        itemSelected = valueAndItemMap[itemSelected] ?: hintText
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { expanded = true },  // Make whole Card clickable
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) { // This looks like a OutlinedTextField even a card
        Spacer(Modifier.padding(2.5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.padding(5.dp))
            if (itemSelected == hintText) {
                Text(
                    text = itemSelected,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = itemSelected,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "More options")
            }
        }
        Spacer(Modifier.padding(2.5.dp))
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            valueAndItemMap.forEach { (value, item) ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        itemSelected = value
                        expanded = false
                        coroutineScope.launch(Dispatchers.IO) {
                            saveToDataStore(settingItemKey, itemSelected, context)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ServerItem(gotServerCallback: (settingValue: String) -> Unit = {}) {
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

    Column(modifier = Modifier.padding(16.dp)) {
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
            gotServerCallback(settingValue)
        } else {
            Text("Server unavailable, message: $data")
        }
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