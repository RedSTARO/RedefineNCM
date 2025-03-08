package com.redstar.redefinencm.activity.MainActivity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.redstar.redefinencm.activity.MainActivity.*
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*


@Composable
fun MainScreen() {
    // 创建一个 NavController 实例
    val navController = rememberNavController()
    println("NOW IN MAIN ACTIVITY")
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    val context = LocalContext.current
    var uid by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        uid = retrofit.userAccount().account.id
    }

    // 使用 NavHost 管理导航
    NavHost(
        navController = navController,  // 传入 NavController 实例
        startDestination = "userPlaylistPage"       // 指定开始的页面
    ) {
        composable("userPlaylistPage") {
            showUserPlaylistPage(
                retrofit = retrofit,
                uid = uid,
                navController = navController
            )
        }

        composable("playlistDetailPage/{songId}") { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId")
            // 播放页
            showPlaylistDetailPage(
                retrofit = retrofit,
                songlistID = songId!!.toLong(),
                navController = navController)
        }
    }
}