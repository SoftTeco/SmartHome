package com.softteco.template.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import com.softteco.template.R
import com.softteco.template.ui.theme.AppTheme

private const val ANIMATION_DURATION = 500
private const val PLACEHOLDER_FRACTION = 0.6f

@Composable
internal fun Avatar(
    imageUri: String?,
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

        val painter: Painter? = imageUri?.let { avatar ->
            val imageLoader = ImageLoader.Builder(context)
                .crossfade(ANIMATION_DURATION)
                .respectCacheHeaders(respectCacheHeaders)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
            rememberAsyncImagePainter(model = avatar.toUri(), imageLoader = imageLoader)
        }

        Box(contentAlignment = Alignment.Center) {
            if (painter != null) {
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.avatar),
                )
            } else {
                Icon(
                    painterResource(R.drawable.baseline_person_24),
                    contentDescription = stringResource(R.string.avatar),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize(PLACEHOLDER_FRACTION),
                )
            }
        }
    }
}

// region Previews

@Preview
@Composable
private fun Preview() {
    AppTheme {
        Avatar(
            imageUri = "https://i.pravatar.cc/300",
            Modifier.size(80.dp)
        )
    }
}

@Preview
@Composable
private fun PlaceholderPreview() {
    AppTheme {
        Avatar(imageUri = null, Modifier.size(80.dp))
    }
}

// endregion
