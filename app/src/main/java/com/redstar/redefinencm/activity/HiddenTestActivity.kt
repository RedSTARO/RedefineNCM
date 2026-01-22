package com.redstar.redefinencm.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.redstar.redefinencm.ui.theme.RedefineNCMTheme

class HiddenTestActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RedefineNCMTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    var showHotComments by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { showHotComments = true },
                            modifier = Modifier.weight(1f),
                            enabled = !showHotComments
                        ) {
                            Text("Show Hot")
                        }
                        Button(
                            onClick = { showHotComments = false },
                            modifier = Modifier.weight(1f),
                            enabled = showHotComments
                        ) {
                            Text("Show Top")
                        }
                    }
                }
            }
        }
    }
}
