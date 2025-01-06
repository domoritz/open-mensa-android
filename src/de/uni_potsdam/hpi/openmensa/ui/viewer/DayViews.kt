package de.uni_potsdam.hpi.openmensa.ui.viewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMaxBy

object DayViews {
    @Composable
    fun DateHeader(text: String) {
        Column (
            Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    @Composable
    fun BigMessage(text: String, modifier: Modifier = Modifier) {
        Box (modifier.padding(16.dp)) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    @Composable
    fun TableLayout(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Layout(modifier = modifier, content = content) { measurables, constraints ->
            val labels = measurables.filterIndexed { index, measurable -> index % 2 == 0 }
            val values = measurables.filterIndexed { index, measurable -> index % 2 == 1 }

            val labelConstraints =
                if(constraints.hasBoundedWidth) Constraints(maxWidth = constraints.maxWidth * 2 / 3)
                else Constraints()

            val labelMeasure = labels.map {
                it.measure(labelConstraints)
            }

            val labelMaxWidth = labelMeasure.fastMaxBy { it.width }?.width ?: 0

            val valueConstraints =
                if(constraints.hasBoundedWidth) Constraints(maxWidth = (constraints.maxWidth - labelMaxWidth).coerceAtLeast(0))
                else Constraints()

            val valueMeasure = values.map {
                it.measure(valueConstraints)
            }

            val totalWidth =
                if (constraints.hasBoundedWidth) constraints.maxWidth
                else labelMaxWidth + (valueMeasure.fastMaxBy { it.width }?.width ?: 0)

            val totalHeight = labelMeasure.zip(valueMeasure).sumOf { (label, value) ->
                label.height.coerceAtLeast(value.height)
            }

            layout(totalWidth, totalHeight) {
                var yStart = 0

                labelMeasure.zip(valueMeasure).forEachIndexed { index, (label, value) ->
                    val height = label.height.coerceAtLeast(value.height)

                    label.placeRelative(0, yStart + (height - label.height) / 2)
                    value.placeRelative(labelMaxWidth, yStart + (height - value.height) / 2)

                    yStart += height
                }
            }
        }
    }
}