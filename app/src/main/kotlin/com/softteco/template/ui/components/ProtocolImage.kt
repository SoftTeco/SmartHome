package com.softteco.template.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import com.softteco.template.R
import com.softteco.template.data.device.Device
import com.softteco.template.data.device.ProtocolType
import com.softteco.template.ui.theme.AppTheme
import com.softteco.template.utils.protocol.getProtocolImage
import java.util.UUID

@Composable
internal fun ProtocolImage(
    device: Device,
    connectionStatus: Boolean?,
    modifier: Modifier = Modifier,
    respectCacheHeaders: Boolean = false,
) {
    Surface(
        modifier = modifier.border(
            1.dp,
            when (connectionStatus) {
                true -> Color.Green
                false -> Color.Red
                else -> Color.Gray
            },
            CircleShape
        ),
        shape = CircleShape
    ) {
        val context = LocalContext.current

        val painter = getProtocolImage(device.protocolType).let { avatar ->
            val imageLoader = ImageLoader.Builder(context)
                .respectCacheHeaders(respectCacheHeaders)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            rememberAsyncImagePainter(model = avatar.toUri(), imageLoader = imageLoader)
        }

        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painter,
                contentDescription = stringResource(R.string.avatar),
            )
        }
    }
}

// region Previews

@Preview
@Composable
private fun Preview() {
    AppTheme {
        ProtocolImage(
            device = Device.Basic(
                type = Device.Type.TemperatureAndHumidity,
                family = Device.Family.Sensor,
                model = Device.Model.LYWSD03MMC,
                id = UUID.randomUUID(),
                defaultName = "Temperature and Humidity Monitor",
                name = "Temperature and Humidity Monitor",
                macAddress = "00:00:00:00:00",
                img = "file:///android_asset/icon/temperature_monitor.webp",
                location = "Bedroom",
                protocolType = ProtocolType.UNKNOWN
            ),
            connectionStatus = false,
            Modifier.size(24.dp)
        )
    }
}

// endregion
