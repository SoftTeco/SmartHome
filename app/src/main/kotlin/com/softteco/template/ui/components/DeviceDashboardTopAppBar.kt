package com.softteco.template.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.softteco.template.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDashboardTopAppBar(
    title: String?,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = { onBackClicked() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_description)
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.device_settings)
                )
            }
        },
    )
}
