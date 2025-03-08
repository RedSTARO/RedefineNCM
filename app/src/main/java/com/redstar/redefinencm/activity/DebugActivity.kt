package com.redstar.redefinencm.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.redstar.redefinencm.RedefineNCMApplication
import com.redstar.redefinencm.api.NCMApi
import com.redstar.redefinencm.api.RetrofitInstance
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class DebugActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    showAllSettingsData(Modifier.padding(innerPadding))
                    showUrlStatus(Modifier.padding(innerPadding))
                }}}
    }
}

@Composable
fun showAllSettingsData(moodifier: Modifier = Modifier){
    val content = RedefineNCMApplication.Companion.getApplicationContext() as Context
    val datas = runBlocking {
        ((RedefineNCMApplication.Companion.getApplicationContext() as Context)).dataStore.data.first()
    }
    Text(
        text = datas.toString(),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    )
}

@Composable
fun showUrlStatus(modifier: Modifier = Modifier){
    val retrofit = RetrofitInstance.retrofit.create(NCMApi::class.java)
    val text = runBlocking { retrofit.songUrlV1(listOf(5264842), "jymaster").data[0].type }
    Text(text = text.toString(), modifier = modifier)
}