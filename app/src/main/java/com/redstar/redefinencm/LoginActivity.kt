package com.redstar.redefinencm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.PopupWindow
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val cookie = runBlocking {
                        ((RedefineNCMApplication.getApplicationContext() as Context)).dataStore.data.first()[stringPreferencesKey("cookie")] ?: ""
                    }
                    println(cookie)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        println("NOW IN LOGIN ACTIVITY")
                        if (cookie.isNullOrBlank()) {
                            println("Jump to Login")
                            val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
                            cookieLogin(retrofit)
                            qrLogin(retrofit)
                        }
                    }
                }

            }
        }
    }
}


@Composable
fun cookieLogin(retrofit: NCMApi, modifier: Modifier = Modifier) {
    LocalContext.current
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
                        checkLoggedInAndJump(retrofit, cookie)
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
fun qrLogin(retrofit: NCMApi, modifier: Modifier = Modifier){
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        val unikey = retrofit.loginQrKey().data.unikey
        val qrImg = retrofit.loginQrCreate(unikey, true).data.qrimg.substringAfter("base64,")
        bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(Base64.decode(qrImg, Base64.DEFAULT)))
    }
    if(!bitmap?.toString().isNullOrBlank()){
        println("Render qr")
        Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(300.dp))
    }
    else{
        Text("请等待二维码生成")
    }

    // TODO: Check scan status
}

suspend fun checkLoggedInAndJump(retrofit: NCMApi, cookie: String){
    val context = RedefineNCMApplication.getApplicationContext() as Context
    if (retrofit.loginStatus().data.code == 200) {
        // Save cookie
        println(cookie)
        println(retrofit.loginStatus().data.account)
        if(!retrofit.loginStatus().data.account.nickname.isNullOrBlank()){
//             定义键
        val COOKIE_KEY = stringPreferencesKey("cookie")
        // 写入数据
            context.dataStore.edit { preferences ->
                preferences[COOKIE_KEY] = cookie
        }
//             Jump to Main Activity
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
        }
        else{
            throw Exception("Cookie 无效")
        }

    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RedefineNCMTheme {
        cookieLogin(retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java))
    }
}