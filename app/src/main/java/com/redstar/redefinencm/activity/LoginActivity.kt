package com.redstar.redefinencm.activity

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.redstar.redefinencm.BuildConfig
import com.redstar.redefinencm.activity.mainActivity.MainActivity
import com.redstar.redefinencm.data.api.NCMApi
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.DataStoreManager
import com.redstar.redefinencm.viewmodel.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LoginViewModel = viewModel()
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var gotServer by remember { mutableStateOf(false) }

                    LaunchedEffect(Dispatchers.IO) {
                        gotServer = runBlocking {
                            (DataStoreManager.getStringItem("server", ""))
                        }.isNotEmpty()
                    }

                    if (gotServer) {
                        LoginPage(innerPadding = innerPadding, viewModel = viewModel)
                    } else {
                        ServerItem({ gotServer = true }) // This from SettingActivity
                    }
                }
            }
        }
    }
}

@Composable
fun LoginPage(innerPadding: PaddingValues, viewModel: LoginViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CookieLogin(viewModel)
//        QrLogin(viewModel)
    }
}

@Composable
fun CookieLogin(viewModel: LoginViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = "用户登录",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = "请输入您的 Cookie 以登录。",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        OutlinedTextField(
            label = { Text("Cookie") },
            value = viewModel.cookie,
            onValueChange = { viewModel.updateCookie(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(64.dp), // So fixed even long cookie text
        )
        Button(
            onClick = {
                if (viewModel.cookie.isBlank()) {
                    viewModel.cookieLoginErrorMessage = "Cookie 不能为空"
                    return@Button
                }
                viewModel.cookieLoginLoading = true
                viewModel.cookieLoginErrorMessage = ""
                coroutineScope.launch {
                    try {
                        checkLoggedInAndJump(viewModel.retrofit, viewModel.cookie, context)
                    } catch (e: Exception) {
                        viewModel.cookieLoginErrorMessage = "登录失败：${e.message}"
                    } finally {
                        viewModel.cookieLoginLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (viewModel.cookieLoginLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text("登录")
            }
        }
        if (viewModel.cookieLoginErrorMessage.isNotEmpty()) {
            Text(
                text = viewModel.cookieLoginErrorMessage,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
fun QrLogin(viewModel: LoginViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 获取二维码
    LaunchedEffect(Unit) {
        try {
            val keyResponse = viewModel.retrofit.loginQrKey()
            viewModel.qrLoginUnikey = keyResponse.data.unikey

            val qrImg = viewModel.retrofit.loginQrCreate(
                viewModel.qrLoginUnikey,
                true,
            ).data.qrimg.substringAfter("base64,")
            val qrBytes = Base64.decode(qrImg, Base64.DEFAULT)
            viewModel.qrLoginBitmap = BitmapFactory.decodeStream(ByteArrayInputStream(qrBytes))

            viewModel.qrLoginScanStatus = "Scan QR Code to log in" // 更新状态
        } catch (e: Exception) {
            viewModel.qrLoginScanStatus = "Failed to generate QR Code"
            if (BuildConfig.DEBUG) {
                Log.d("Login", "qrLogin, Error generating QR code: ${e.message}")
            }
        }
    }

    // UI 渲染
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        viewModel.qrLoginBitmap?.asImageBitmap()?.let {
            Image(bitmap = it, contentDescription = "QR Code", modifier = Modifier.size(300.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = viewModel.qrLoginScanStatus, fontSize = 18.sp, color = Color.Black)
    }

    // 轮询检查扫码状态
    LaunchedEffect(viewModel.cookie.isNotEmpty()) {
        coroutineScope.launch {
            while (viewModel.cookie.isNotEmpty()) {
                try {
                    val response = viewModel.retrofit.loginQrCheck(viewModel.qrLoginUnikey)
                    when (response.code) {
                        800 -> {
                            viewModel.qrLoginScanStatus = "QR Code Expired. Generating new one..."
                            viewModel.qrLoginUnikey = "" // 触发二维码刷新
                        }

                        801, 802 -> {
                            viewModel.qrLoginScanStatus = response.message
                        }

                        803 -> {
                            viewModel.updateCookie(response.cookie)
                            viewModel.qrLoginScanStatus = "Login Successful!"
                            checkLoggedInAndJump(viewModel.retrofit, viewModel.cookie, context)
                        }
                    }
                    delay(2000)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.d("Login", "qrLogin, Error checking QR status: ${e.message}")
                    }
                }
            }
        }
    }
}

suspend fun checkLoggedInAndJump(retrofit: NCMApi, cookie: String, context: Context) {
    val request = retrofit.loginStatus(cookie)
    if (request.data.code == 200) {
        // Save cookie
        if (BuildConfig.DEBUG) {
            Log.d("Login", "username: ${request.data.profile.nickname}")
        }
        if (request.data.profile.nickname.isNotBlank()) {
            // Jump to MainActivity
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            if (context is LoginActivity) {
                context.finish()
            }
        } else {
            throw Exception("Cookie 无效")
        }
    }
}

@Composable
@Preview
fun LoginActivityPreview() {
    ServerItem({})
    CookieLogin(viewModel())
    QrLogin(viewModel())
}
