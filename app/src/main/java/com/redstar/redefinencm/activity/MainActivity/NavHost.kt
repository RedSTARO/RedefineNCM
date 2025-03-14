package com.redstar.redefinencm.activity.MainActivity

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import com.redstar.redefinencm.BuildConfig


@Composable
fun MainScreen() {
    // 创建一个 NavController 实例
    val navController = rememberNavController()
    if (BuildConfig.DEBUG) {
        Log.d("Main", "MainStartup, Nav Controller: $navController")
    }
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    var uid by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) {
        uid = retrofit.userAccount().account.id
        if (BuildConfig.DEBUG) {
            Log.d("Main", "UID: $uid")
        }
    }

    if (uid != null) {
        // 使用 NavHost 管理导航
        NavHost(
            navController = navController,  // 传入 NavController 实例
            startDestination = "userPlaylistPage"       // 指定开始的页面
        ) {
            composable("userPlaylistPage") {
                showUserPlaylistPage(
                    retrofit = retrofit,
                    uid = uid!!,
                    navController = navController
                )
            }

            composable("playlistDetailPage/{songId}") { backStackEntry ->
                val songId = backStackEntry.arguments?.getString("songId")
                if (BuildConfig.DEBUG) {
                    Log.d("Main", "SongList ID: $songId")
                }
                showPlaylistDetailPage(
                    retrofit = retrofit,
                    songlistID = songId!!.toLong(),
                    navController = navController
                )
            }
        }
    }
}