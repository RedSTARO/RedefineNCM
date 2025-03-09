package com.redstar.redefinencm.api
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.activity.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//TODO: Move to Cache-Then-Network

object RetrofitInstance {
    private const val BASE_URL = "https://ncm.tryagain.fun/"
    private val REAL_IP = getRealIP()
    private val COOKIE = getCookie()
    private val NoCookieUrl = listOf("login")
    private val TimestampUrl = listOf("login")

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val originalUrl = original.url

            // 创建基本的 URL 并添加 realIP 参数
            var newUrl = originalUrl.newBuilder()
                .addQueryParameter("realIP", REAL_IP)
                .build()

            // 如果 URL 包含 "TimestampUrl"，则添加 timestamp 参数
            if (TimestampUrl.any { originalUrl.encodedPath.contains(it) }) {
                newUrl = newUrl.newBuilder()
                    .addQueryParameter("timestamp", System.currentTimeMillis().toString())
                    .build()
            }

            // 如果 URL 包含 "NoCookieUrl"，则不添加 cookie 参数
            if (!NoCookieUrl.any { originalUrl.encodedPath.contains(it) }) {
                newUrl = newUrl.newBuilder()
                    .addQueryParameter("cookie", COOKIE)
                    .build()
            }


            chain.proceed(original.newBuilder()
                .url(newUrl)
                .build())
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}

fun getRealIP(): String {
    return "192.168.1.1"
}

fun getCookie(): String {
    return runBlocking {
        ((RedefineNCMApplication.getApplicationContext() as Context)).dataStore.data.first()[stringPreferencesKey("cookie")] ?: ""
    }
}
