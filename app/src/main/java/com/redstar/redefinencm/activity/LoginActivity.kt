package com.redstar.redefinencm.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.activity.MainActivity.MainActivity
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var cookie by remember { mutableStateOf("") }
                    var gotServer by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        cookie = runBlocking {
                            ((RedefineNCMApplication.getApplicationContext() as Context)).dataStore.data.first()[stringPreferencesKey(
                                "cookie"
                            )] ?: ""
                        }
                        gotServer = runBlocking {
                            ((RedefineNCMApplication.getApplicationContext() as Context)).dataStore.data.first()[stringPreferencesKey(
                                "server"
                            )] ?: ""
                        }.isNotBlank()
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d("Login", "Cookie: $cookie")
                    }

                    if (gotServer) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (cookie.isBlank()) {
                                if (BuildConfig.DEBUG) {
                                    Log.d("Login", "No Cookie, login")
                                }
                                val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
                                CookieLogin(retrofit)
//                            qrLogin(retrofit) // Disabled QR login due to 高使用门槛 :>
                            } else {
//                            TODO: Add a new splash screen
                                if (BuildConfig.DEBUG) {
                                    Log.d("Login", "Got cookie, jump to main")
                                }
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    } else {
                        ServerItem({ gotServer = true }) // This from SettingActivity
                    }
                }

            }
        }
    }
}


@Composable
fun CookieLogin(retrofit: NCMApi, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var cookie by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "用户登录",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "请输入您的 Cookie 以登录。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            label = { Text("Cookie") },
            value = cookie,
            onValueChange = { cookie = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(64.dp) // So fixed even long cookie text
        )
        Button(
            onClick = {
                if (cookie.isBlank()) {
                    errorMessage = "Cookie 不能为空"
                    return@Button
                }
                isLoading = true
                errorMessage = ""
                coroutineScope.launch {
                    try {
                        checkLoggedInAndJump(retrofit, cookie, context)
                    } catch (e: Exception) {
                        errorMessage = "登录失败：${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("登录")
            }
        }
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun QrLogin(retrofit: NCMApi) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var unikey by remember { mutableStateOf("") }
    var gotCookie by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf("Generating Code") }
    var cookie by remember { mutableStateOf("") }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope() // 独立的协程作用域

    // 获取二维码
    LaunchedEffect(Unit) {
        try {
            val keyResponse = retrofit.loginQrKey()
            unikey = keyResponse.data.unikey

            val qrImg = retrofit.loginQrCreate(unikey, true).data.qrimg.substringAfter("base64,")
            val qrBytes = Base64.decode(qrImg, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(qrBytes))

            scanStatus = "Scan QR Code to log in" // 更新状态
        } catch (e: Exception) {
            scanStatus = "Failed to generate QR Code"
            Log.d("Login", "qrLogin, Error generating QR code: ${e.message}")
        }
    }

    // UI 渲染
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        bitmap?.asImageBitmap()?.let {
            Image(bitmap = it, contentDescription = "QR Code", modifier = Modifier.size(300.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = scanStatus, fontSize = 18.sp, color = Color.Black)
    }

    // 轮询检查扫码状态
    LaunchedEffect(unikey) {
        coroutineScope.launch {
            while (!gotCookie) {
                try {
                    val response = retrofit.loginQrCheck(unikey)
                    when (response.code) {
                        800 -> {
                            scanStatus = "QR Code Expired. Generating new one..."
                            unikey = "" // 触发二维码刷新
                        }

                        801, 802 -> {
                            scanStatus = response.message
                        }

                        803 -> {
                            cookie = response.cookie
                            gotCookie = true
                            scanStatus = "Login Successful!"
                            checkLoggedInAndJump(retrofit, cookie, context)
                        }
                    }
                    delay(2000)
                } catch (e: Exception) {
                    Log.d("Login", "qrLogin, Error checking QR status: ${e.message}")
                }
            }
        }
    }
}

suspend fun checkLoggedInAndJump(retrofit: NCMApi, cookie: String, context: Context) {
//    val context = RedefineNCMApplication.getApplicationContext() as Context
    if (retrofit.loginStatus(cookie).data.code == 200) {
        // Save cookie
        Log.d("Login", "username: ${retrofit.loginStatus(cookie).data.profile.nickname}")
        if (retrofit.loginStatus(cookie).data.profile.nickname.isNotBlank()) {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("cookie")] = cookie
            }
//        Jump to MainActivity
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        } else {
            throw Exception("Cookie 无效")
        }

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RedefineNCMTheme {
        CookieLogin(retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java))
    }
}