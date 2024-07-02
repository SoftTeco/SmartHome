package com.softteco.template.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.softteco.template.ui.theme.Dimens

@Composable
fun <T> DashboardValueBlock(
    value: T,
    valueName: String,
    measurementUnit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.padding(Dimens.PaddingDefault),
                imageVector = icon,
                contentDescription = valueName
            )
            Column(
                modifier = Modifier.padding(start = Dimens.PaddingSmall)
            ) {
                Text(
                    valueName,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.padding(vertical = Dimens.PaddingSmall))
                Text(
                    buildAnnotatedString {
                        append(value.toString())
                        withStyle(style = MaterialTheme.typography.headlineSmall.toSpanStyle()) {
                            append(measurementUnit)
                        }
                    },
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.padding(bottom = Dimens.PaddingSmall))
            }
        }
    }
}
