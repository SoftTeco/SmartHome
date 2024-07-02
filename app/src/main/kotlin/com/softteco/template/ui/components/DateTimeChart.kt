package com.softteco.template.ui.components

import android.graphics.PorterDuff
import android.graphics.Typeface
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.rememberLegendItem
import com.patrykandpatrick.vico.compose.common.rememberVerticalLegend
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.component
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shader.TopBottomShader
import com.patrykandpatrick.vico.core.common.shape.Shape
import com.softteco.template.ui.feature.devicedashboard.devices.thermometer.ITEM_PLACER_COUNT
import com.softteco.template.ui.feature.devicedashboard.devices.thermometer.xToDateMapKeyLocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Composable
fun DateTimeChart(
    values: Map<LocalDateTime, Float>,
    bottomAxisValueFormatter: CartesianValueFormatter,
    timeUnit: ChronoUnit,
    @StringRes yAxisTitle: Int,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { CartesianChartModelProducer.build() }

    LaunchedEffect(values) {
        withContext(Dispatchers.Default) {
            if (values.isNotEmpty()) {
                val minDateTime = values.keys.minOrNull() ?: LocalDateTime.now()

                val xToDates = values.keys.associateBy {
                    timeUnit.between(minDateTime, it).toFloat()
                }

                modelProducer.tryRunTransaction {
                    lineSeries { series(xToDates.keys, values.values) }
                    updateExtras {
                        it[xToDateMapKeyLocalDateTime] = xToDates
                    }
                }
            }
        }
    }

    CreateDateTimeChart(modelProducer, bottomAxisValueFormatter, yAxisTitle, modifier)
}

@Composable
fun CreateDateTimeChart(
    modelProducer: CartesianChartModelProducer,
    bottomAxisValueFormatter: CartesianValueFormatter,
    @StringRes yAxisTitle: Int,
    modifier: Modifier = Modifier
) {
    val marker = rememberMarker()
    CartesianChartHost(
        chart =
        rememberCartesianChart(
            rememberLineCartesianLayer(
                lines =
                listOf(
                    rememberLineSpec(
                        shader =
                        TopBottomShader(
                            DynamicShader.color(MaterialTheme.colorScheme.primary),
                            DynamicShader.color(MaterialTheme.colorScheme.error)
                        ),
                        backgroundShader = createBackgroundShader(),
                    )
                )
            ),
            startAxis = createStartAxis(),
            bottomAxis =
            rememberBottomAxis(
                valueFormatter = bottomAxisValueFormatter,
                itemPlacer =
                remember {
                    AxisItemPlacer.Horizontal.default(spacing = 1, addExtremeLabelPadding = true)
                },
            ),
            legend = rememberLegend(yAxisTitle)
        ),
        modelProducer = modelProducer,
        modifier = modifier,
        marker = marker,
        runInitialAnimation = false,
        horizontalLayout = HorizontalLayout.fullWidth(),
        scrollState = rememberVicoScrollState(
            scrollEnabled = true,
            initialScroll = Scroll.Absolute.End,
            autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased
        ),
    )
}

@Composable
private fun rememberLegend(@StringRes yAxisTitle: Int,) =
    rememberVerticalLegend<CartesianMeasureContext, CartesianDrawContext>(
        items =
        listOf(MaterialTheme.colorScheme.primary).mapIndexed { index, chartColor ->
            rememberLegendItem(
                icon = rememberShapeComponent(Shape.Pill, chartColor),
                label =
                rememberTextComponent(
                    color = vicoTheme.textColor,
                    textSize = 12.sp,
                    typeface = Typeface.MONOSPACE,
                ),
                labelText = stringResource(yAxisTitle, index + 1),
            )
        },
        iconSize = 8.dp,
        iconPadding = 8.dp,
        spacing = 4.dp,
        padding = Dimensions.of(top = 8.dp),
    )

@Composable
fun createBackgroundShader(): TopBottomShader {
    return TopBottomShader(
        DynamicShader.compose(
            DynamicShader.component(
                componentSize = 6.dp,
                component =
                rememberShapeComponent(
                    shape = Shape.Pill,
                    color = MaterialTheme.colorScheme.primary,
                    margins = Dimensions.of(1.dp),
                ),
            ),
            DynamicShader.verticalGradient(
                arrayOf(
                    Color.Black,
                    Color.Transparent
                )
            ),
            PorterDuff.Mode.DST_IN,
        ),
        DynamicShader.compose(
            DynamicShader.component(
                componentSize = 5.dp,
                component =
                rememberShapeComponent(
                    shape = Shape.Rectangle,
                    color = MaterialTheme.colorScheme.error,
                    margins = Dimensions.of(horizontal = 2.dp),
                ),
                checkeredArrangement = false,
            ),
            DynamicShader.verticalGradient(
                arrayOf(
                    Color.Transparent,
                    Color.Black
                )
            ),
            PorterDuff.Mode.DST_IN,
        ),
    )
}

@Composable
fun createStartAxis(): VerticalAxis<AxisPosition.Vertical.Start> {
    return rememberStartAxis(
        label =
        rememberAxisLabelComponent(
            color = MaterialTheme.colorScheme.onBackground,
            background =
            rememberShapeComponent(
                shape = Shape.Pill,
                color = Color.Transparent,
                strokeColor = MaterialTheme.colorScheme.outlineVariant,
                strokeWidth = 1.dp,
            ),
            padding = Dimensions.of(horizontal = 6.dp, vertical = 2.dp),
            margins = Dimensions.of(end = 8.dp),
        ),
        guideline =
        rememberLineComponent(
            color = MaterialTheme.colorScheme.outlineVariant,
            shape =
            remember { Shape.dashed(shape = Shape.Pill, dashLength = 4.dp, gapLength = 8.dp) },
        ),
        itemPlacer = remember { AxisItemPlacer.Vertical.count(count = { ITEM_PLACER_COUNT }) },
    )
}
