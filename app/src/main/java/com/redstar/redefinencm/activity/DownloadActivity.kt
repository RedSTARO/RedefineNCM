package com.redstar.redefinencm.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redstar.redefinencm.services.DownloadService
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.DownloadStorage

class DownloadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedefineNCMTheme {
                DownloadManagerPage()
            }
        }
    }
}

@Composable
fun DownloadManagerPage() {
    val statuses by DownloadService.statuses.collectAsState()
    var files by remember { mutableStateOf(DownloadStorage.listDownloadedFiles()) }

    LaunchedEffect(statuses) {
        files = DownloadStorage.listDownloadedFiles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Downloading", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(statuses) { status ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(status.fileName, style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = status.progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Downloaded Files", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(files) { file ->
                Text(text = file.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
