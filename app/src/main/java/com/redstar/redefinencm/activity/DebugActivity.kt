package com.redstar.redefinencm.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme

class DebugActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    ShowAllSettingsData()
//                    showUrlStatus(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

//@Composable
//fun ShowAllSettingsData() {
//    RedefineNCMApplication.getApplicationContext() as Context
//    val datas = runBlocking {
//        DataStoreManager.getAppDataStore().data.first()
//    }
//    Text(
//        text = datas.toString(),
//        modifier = Modifier.horizontalScroll(rememberScrollState()),
//    )
//}

// @Composable
// fun showUrlStatus(modifier: Modifier = Modifier){
//    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
//    val text = runBlocking { retrofit.songUrlV1(listOf(5264842), "jymaster").data[0].type }
//    Text(text = text.toString(), modifier = modifier)
// }
