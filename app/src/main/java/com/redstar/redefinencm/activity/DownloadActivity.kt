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
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme
import com.redstar.redefinencm.util.DownloadStorage
import com.redstar.redefinencm.util.SettingProvider
import com.redstar.redefinencm.util.SoundQuality
import kotlin.reflect.KClass

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
    var files by remember { mutableStateOf(DownloadStorage.listDownloadedFiles()) }

    LaunchedEffect(Unit) {
        files = DownloadStorage.listDownloadedFiles()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        QualitySelectItem(
            SettingProvider.downloadQuality,
            "Download Quality",
            SoundQuality::class
        ) { SettingProvider.updateDownloadQuality(it) }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(files) { file ->
                Text(text = file.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun <T> QualitySelectItem(
    settingItem: String,
    hintText: String,
    enumClass: KClass<T>,
    settingItemUpdater: (T) -> Unit,
) where T : Enum<T> {
    var itemSelected by remember { mutableStateOf(settingItem) }
    var expanded by remember { mutableStateOf(false) }
    val entries = enumClass.java.enumConstants
    val currentEnum = entries.find { it.name == itemSelected }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { expanded = true },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = currentEnum?.toString() ?: hintText,
                color = if (currentEnum != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                },
                modifier = Modifier.weight(1f),
            )
            androidx.compose.material3.IconButton(onClick = { expanded = !expanded }) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                    contentDescription = "More options"
                )
            }
        }
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
        ) {
            entries.forEach { item ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        itemSelected = item.name
                        expanded = false
                        settingItemUpdater(item)
                    },
                )
            }
        }
    }
}
