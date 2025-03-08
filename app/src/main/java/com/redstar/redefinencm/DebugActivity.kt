package com.redstar.redefinencm

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
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.datastore.preferences.core.stringPreferencesKey
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie

class DebugActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    showAllSettingsData(Modifier.padding(innerPadding))
                }}}
    }
}

@Composable
fun showAllSettingsData(moodifier: Modifier = Modifier){
    val content = RedefineNCMApplication.getApplicationContext() as Context
    val datas = runBlocking {
        ((RedefineNCMApplication.getApplicationContext() as Context)).dataStore.data.first()
    }
    Text(
        text = datas.toString(),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    )

}