package com.softteco.template.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import com.softteco.template.R
import com.softteco.template.ui.theme.AppTheme

@Composable
internal fun DeviceImage(
    imageUri: String,
    modifier: Modifier = Modifier,
    respectCacheHeaders: Boolean = false,
) {
    Surface(
        modifier = modifier.border(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            CircleShape
        ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
    ) {
        val context = LocalContext.current

        val painter = imageUri.let { avatar ->
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
        DeviceImage(
            imageUri = "https://i.pravatar.cc/300",
            Modifier.size(48.dp)
        )
    }
}

// endregion
