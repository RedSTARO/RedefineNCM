package com.redstar.redefinencm.activity

import android.os.Bundle
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
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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
                            serverItem()
                            Spacer(modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            textItem("cookie", "Account Cookie")

                        }
                    }
                }
            }
        }
    }
}


@Composable
fun textItem(settingItemKey: String, hintText: String) {
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
fun serverItem() {
    val settingItemKey = "server"
    val hintText = "Server"
    var settingValue by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    var code by remember { mutableStateOf(0) }
    var data by remember { mutableStateOf("") }

    // 读取 dataStore 中的 settingItem 信息，只在首次加载时执行
    LaunchedEffect(settingItemKey) {
        settingValue = context.dataStore.data
            .firstOrNull()?.get(stringPreferencesKey(settingItemKey)) ?: ""
    }

    // 保存到 dataStore
    val saveToDataStore: (String) -> Unit = { newValue ->
        coroutineScope.launch {
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
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(64.dp), // 保证文本框固定高度
        singleLine = true // 确保文本框单行显示
    )

    Button(onClick = {
        coroutineScope.launch(Dispatchers.IO) {
            code = retrofit.innerVersion().code
            data = retrofit.innerVersion().data.version
        }

        if (settingValue.isNotEmpty()) {
            saveToDataStore(settingValue)
        }
    }) {
        Text("Check server at $settingValue")
    }

    if (code == 200) {
        Text("Server version: $data, OK")
    }

}

