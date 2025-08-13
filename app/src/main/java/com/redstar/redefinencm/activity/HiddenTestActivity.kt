package com.redstar.redefinencm.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class HiddenTestActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var showHotComments by remember { mutableStateOf(true) }
            ButtonGroup(
                overflowIndicator = { /* 可替换为图标，如 Icon(Icons.Default.MoreVert) */ Text("...") },
            ) {
                toggleableItem(
                    weight = 1f,
                    checked = showHotComments,
                    onCheckedChange = { showHotComments = true },
                    label = "Show Hot"
                )
                toggleableItem(
                    weight = 1f,
                    checked = !showHotComments,
                    onCheckedChange = { showHotComments = false },
                    label = "Show Top"
                )
            }
        }
    }
}