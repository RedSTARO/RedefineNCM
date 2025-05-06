package com.redstar.redefinencm.server

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.IOException

class MainActivity : ComponentActivity() {
    private var server: ApiServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var serverStarted by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                try {
                    server = ApiServer(applicationContext)
                    server?.start()
                    Log.i("MyApiServer", "✅ 启动成功: http://localhost:8080")
                    serverStarted = true
                } catch (e: IOException) {
                    Log.e("MyApiServer", "❌ 启动失败", e)
                    serverStarted = false
                }
            }

            Text(text = if (serverStarted) "✅ 服务器已启动在端口 8080" else "❌ 服务器启动失败")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }
}
