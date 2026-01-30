package com.redstar.redefinencm.activity.mainActivity

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchDemoPage(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val queryState = remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
            )
        }
        with(sharedTransitionScope) {
            TextField(
                value = queryState.value,
                onValueChange = { queryState.value = it },
                placeholder = { Text("Search") },
                modifier = Modifier
                    .sharedBounds(
                        rememberSharedContentState(SharedKeys.search()),
                        animatedVisibilityScope,
                    )
                    .fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search demo page",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Type to see shared bounds from the home search card.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
