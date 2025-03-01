package com.redstar.redefinencm

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.launch


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    cookieLogin(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    var userName by remember { mutableStateOf("Loading...") }
//
//    LaunchedEffect(Unit) {
//        scope.launch {
//            val ret = RetrofitInstance.retrofit.create(NCMApi::class.java)
//            val userAccount = withContext(Dispatchers.IO) { ret.userAccount() }
//            userName = userAccount.account.userName
//        }
//    }
//
//    Text(
//        text = userName,
//        modifier = modifier
//    )
//}

@Composable
fun cookieLogin(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    val coroutineScope = rememberCoroutineScope()
    var cookie by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var uid by remember { mutableStateOf(0L) }
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
                        val userAccount = retrofit.userAccount(cookie)
                        userName = userAccount.account.userName
                        uid = userAccount.account.id
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
        if (uid.toString().isNotEmpty()) {
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString("cookie", cookie)
//                TODO: https://developer.android.google.cn/topic/libraries/architecture/datastore?hl=zh-cn#prefs-vs-proto
                apply()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RedefineNCMTheme {
//        Greeting("Android")
    }
}