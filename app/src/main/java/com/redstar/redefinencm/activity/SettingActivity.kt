package com.redstar.redefinencm.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.DataStoreManager
import com.redstar.redefinencm.util.SettingProvider
import com.redstar.redefinencm.util.SettingProvider.adaptOriginalAndroidLyric
import com.redstar.redefinencm.util.SoundQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.KClass

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
                    // 注册 Launcher
                    val importSettingLauncher: ActivityResultLauncher<Intent> =
                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                            if (result.resultCode == Activity.RESULT_OK) {
                                result.data?.data?.let { uri ->
                                    SettingProvider.importAppSettingFromUri(this, uri)
                                }
                            }
                        }
                    SettingPage(this, importSettingLauncher)
                }
            }
        }
    }
}

@Composable
fun SettingPage(activity: Activity, importSettingLauncher: ActivityResultLauncher<Intent>) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )

            SettingSectionTitle("Server")
            ServerItem()

            SettingSectionTitle("Account")
            TextItem(SettingProvider.cookie, "Account Cookie") { SettingProvider.updateCookie(it) }

            SettingSectionTitle("Playback")
            SelectItem(
                SettingProvider.onlinePlayQuality,
                "Music Quality Online",
                SoundQuality::class
            ) { SettingProvider.updateOnlinePlayQuality(it) }
            SelectItem(
                SettingProvider.downloadQuality,
                "Music Quality Download",
                SoundQuality::class
            ) { SettingProvider.updateDownloadQuality(it) }
            SwitchItem(
                SettingProvider.replacePlaylist,
                "Replace playlist when click single songs"
            ) { SettingProvider.updateReplacePlaylist(it) }
            SwitchItem(
                SettingProvider.adaptOriginalAndroidLyric,
                "Adapt original Android Live Update lyric"
            ) { SettingProvider.updateAdaptOriginalAndroidLyric(it) }
            SwitchItem(
                SettingProvider.showDownloadStatus,
                "Show download status in playlist",
            ) { SettingProvider.updateShowDownloadStatus(it) }
            SwitchItem(
                SettingProvider.searchPrediction,
                "Search prediction (suggestions while typing)",
            ) { SettingProvider.updateSearchPrediction(it) }

            SettingSectionTitle("General")
            SwitchItem(
                SettingProvider.checkUpdate,
                "Check update when app start"
            ) { SettingProvider.updateCheckUpdate(it) }

            SettingSectionTitle("Backup")
            ButtonItem("Export app setting") { SettingProvider.exportAppSetting() }
            ButtonItem("Import app setting") {
                SettingProvider.startImportSetting(activity, importSettingLauncher)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
fun TextItem(
    settingItem: String,
    hintText: String,
    settingItemUpdater: (String) -> Unit,
) {
    var settingValue by remember { mutableStateOf(settingItem) }

    OutlinedTextField(
        label = { Text(hintText) },
        value = settingValue,
        onValueChange = { newValue ->
            settingValue = newValue
            settingItemUpdater(newValue)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(64.dp),
        singleLine = true
    )
}


@Composable
fun SettingSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
fun SwitchItem(settingItem: Boolean, hintText: String, enabled: Boolean = true, settingItemUpdater: (Boolean) -> Unit) {
    var checked by remember { mutableStateOf(settingItem) }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    settingItemUpdater(checked)
                },
                enabled = enabled,
            )
        }
    }
}

@Composable
fun <T> SelectItem(
    settingItem: String,
    hintText: String,
    enumClass: KClass<T>,
    settingItemUpdater: (T) -> Unit, // 泛型 T 的参数
) where T : Enum<T> {
    var itemSelected by remember { mutableStateOf(settingItem) }
    var expanded by remember { mutableStateOf(false) }

    val entries = enumClass.java.enumConstants
    val currentEnum = entries.find { it.name == itemSelected }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = true },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = currentEnum?.toString() ?: "Not set",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "More options")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            entries.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        itemSelected = item.name
                        expanded = false
                        settingItemUpdater(item)
                    },
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
    LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var status by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf("") }

    // 读取 dataStore 中的 settingItem 信息，只在首次加载时执行
    LaunchedEffect(settingItemKey) {
        settingValue = DataStoreManager.getStringItem(settingItemKey, "")
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
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
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(64.dp), // 保证文本框固定高度
            singleLine = true, // 确保文本框单行显示
        )
        FilledTonalButton(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        if (BuildConfig.DEBUG) {
                            Log.d("SettingActivity", "Save server at $settingValue")
                        }
                        status = checkServerAvailable(settingValue)
                        data = checkServerVersion(settingValue)
                    } catch (e: Exception) {
                        data = e.message.toString()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Check server at $settingValue")
        }

        if (status) {
            Text("Server version: $data, OK")
            runBlocking {
                DataStoreManager.setStringItem(settingItemKey, settingValue)
            }
            gotServerCallback(settingValue)
        } else {
            Text("Server unavailable, message: $data")
        }
    }
}

@Composable
fun ButtonItem(hintText: String, onClickAction: () -> Unit) {
    FilledTonalButton(
        onClick = onClickAction,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(text = hintText)
    }
}

suspend fun checkServerAvailable(server: String): Boolean {
    // Create a new Retrofit instance with the provided server URL
    val retrofit = Retrofit.Builder()
        .baseUrl(server) // Use the passed server URL directly
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().build()) // Basic OkHttpClient without custom interceptors
        .build()

    val api = retrofit.create(NCMApi::class.java)
    return try {
        val code = api.innerVersion("${server}inner/version/").code
        Log.d("ServerItem", code.toString())
        code == 200
    } catch (e: Exception) {
        Log.d("ServerItemAva", e.message.toString())
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
        val stat = api.innerVersion("${server}inner/version/").data.version
        Log.d("ServerItem", stat)
        stat
    } catch (e: Exception) {
        Log.d("ServerItemStat", e.message.toString())
        e.message.toString()
    }
}

//@Composable
//@Preview
//fun SettingPagePreview() {
//    SettingPage()
//}
