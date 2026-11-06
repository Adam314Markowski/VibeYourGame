package com.byd.workout.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.workout.data.LogDto
import com.byd.workout.ui.theme.BydShapes
import com.byd.workout.ui.theme.BydTheme
import com.byd.workout.ui.theme.BydType
import com.byd.workout.util.weightLabel

@Composable
fun BydCard(
    modifier: Modifier = Modifier,
    shape: Shape = BydShapes.card,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = BydTheme.colors
    Column(
        modifier = modifier
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = BydType.screenTitle, color = BydTheme.colors.ink, modifier = modifier)
}

@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(text = text.uppercase(), style = BydType.eyebrow, color = BydTheme.colors.muted, modifier = modifier)
}

@Composable
fun CountPill(count: Int, modifier: Modifier = Modifier) {
    val colors = BydTheme.colors
    Text(
        text = count.toString(),
        style = BydType.tiny,
        color = colors.muted,
        modifier = modifier
            .background(colors.surface2, BydShapes.pill)
            .padding(horizontal = 7.dp, vertical = 1.dp),
    )
}

@Composable
fun SectionHeader(title: String, count: Int?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title.uppercase(), style = BydType.sectionLabel, color = BydTheme.colors.ink)
        if (count != null) CountPill(count)
    }
}

@Composable
fun EmptyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = BydType.small.copy(fontSize = 12.5.sp),
        color = BydTheme.colors.muted,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 6.dp),
    )
}

@Composable
fun SmallOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val colors = BydTheme.colors
    val tint = if (accent) colors.accent else colors.ink
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 32.dp),
        shape = BydShapes.small,
        border = BorderStroke(1.dp, if (accent) colors.accent else colors.line),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(text = text, style = BydType.button.copy(fontSize = 12.5.sp), color = tint)
    }
}

@Composable
fun LogEntryCard(
    entry: LogDto,
    modifier: Modifier = Modifier,
    background: Color = BydTheme.colors.surface2,
    bordered: Boolean = false,
) {
    val colors = BydTheme.colors
    val shape = BydShapes.row
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(background, shape)
            .then(if (bordered) Modifier.border(1.dp, colors.line, shape) else Modifier)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = entry.name, style = BydType.rowName, color = colors.ink, modifier = Modifier.weight(1f))
            if (entry.weight.isNotBlank()) {
                Text(text = weightLabel(entry.weight), style = BydType.tiny, color = colors.muted, maxLines = 1)
            }
        }
        if (entry.sets.isNotEmpty()) SetChips(entry.sets)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetChips(sets: List<String>) {
    val colors = BydTheme.colors
    FlowRow(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        sets.forEach { set ->
            Text(
                text = set,
                style = BydType.setChip,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(colors.surface, BydShapes.setChip)
                    .border(1.dp, colors.line, BydShapes.setChip)
                    .padding(horizontal = 5.dp),
            )
        }
    }
}
