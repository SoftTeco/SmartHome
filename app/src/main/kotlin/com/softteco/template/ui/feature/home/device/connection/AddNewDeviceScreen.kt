package com.softteco.template.ui.feature.home.device.connection

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softteco.template.R
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.ui.theme.Dimens.PaddingDefault
import com.softteco.template.ui.theme.Dimens.PaddingNormal
import com.softteco.template.ui.theme.Dimens.PaddingSmall

@Composable
fun AddNewDeviceScreen(
    onBackClicked: () -> Unit,
    onScanClick: () -> Unit,
    onManualClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenContent(onBackClicked, onScanClick, onManualClick, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    onBackClicked: () -> Unit,
    onScanClick: () -> Unit,
    onManualClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.add_new_device_title)) },
            navigationIcon = {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.Outlined.ArrowBack, stringResource(R.string.back_description))
                }
            },
            modifier = Modifier.statusBarsPadding()
        )

        Column(
            Modifier.padding(start = PaddingDefault, top = PaddingNormal, end = PaddingDefault),
            verticalArrangement = Arrangement.spacedBy(PaddingNormal)
        ) {
            Mode(
                titleRes = R.string.add_device_scan_qr_title,
                subtitleRes = R.string.add_device_scan_qr_subtitle,
                imageRes = R.drawable.scan,
                onClick = onScanClick
            )
            Mode(
                titleRes = R.string.add_device_manual_selection_title,
                subtitleRes = R.string.add_device_manual_selection_subtitle,
                imageRes = R.drawable.devices,
                onClick = onManualClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Mode(
    @StringRes
    titleRes: Int,
    @StringRes
    subtitleRes: Int,
    @DrawableRes
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(onClick = onClick, modifier.height(200.dp)) {
        Row(
            Modifier.padding(PaddingDefault),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(PaddingSmall))
                Text(
                    stringResource(subtitleRes),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.width(PaddingDefault))
            Icon(
                Icons.Outlined.ArrowForwardIos,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Image(
            painterResource(imageRes),
            null,
            Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}

// region Previews

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    AppTheme {
        ScreenContent(
            onBackClicked = {},
            onScanClick = {},
            onManualClick = {},
        )
    }
}

// endregion
