package com.softteco.template.ui.feature.home.device.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.softteco.template.R
import com.softteco.template.ui.theme.AppTheme

@Composable
fun ManualSelectionScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenContent(onBackClicked, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.manual_selection_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.Outlined.ArrowBack, stringResource(R.string.back_description))
                }
            },
        )
    }
}

// region Previews

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(onBackClicked = {})
    }
}

// endregion
