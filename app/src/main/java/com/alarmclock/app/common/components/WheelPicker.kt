package com.alarmclock.app.common.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private const val ITEM_HEIGHT_DP = 50
private const val VISIBLE_ITEMS = 3

/**
 * A vertically-snapping wheel picker over [range], formatted with [format].
 * The centered item renders bold/large; neighbours render faded/smaller, matching
 * the rolodex-style time pickers seen throughout the reference app.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    range: IntRange,
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    format: (Int) -> String = { it.toString().padStart(2, '0') }
) {
    val values = remember(range) { range.toList() }
    val initialIndex = remember(selected, values) { values.indexOf(selected).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialIndex)
    val scope = rememberCoroutineScope()

    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf initialIndex
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: initialIndex
        }
    }

    LaunchedEffect(centerIndex) {
        if (centerIndex in values.indices) {
            val value = values[centerIndex]
            if (value != selected) onSelectedChange(value)
        }
    }

    LaunchedEffect(selected) {
        val idx = values.indexOf(selected)
        if (idx >= 0 && idx != centerIndex && !listState.isScrollInProgress) {
            scope.launch { listState.animateScrollToItem(idx) }
        }
    }

    Box(modifier = modifier.height((ITEM_HEIGHT_DP * VISIBLE_ITEMS).dp).width(72.dp)) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                vertical = (ITEM_HEIGHT_DP * (VISIBLE_ITEMS / 2)).dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height((ITEM_HEIGHT_DP * VISIBLE_ITEMS).dp)
        ) {
            itemsIndexed(values) { index, value ->
                val isCentered = index == centerIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ITEM_HEIGHT_DP.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = format(value),
                        fontSize = if (isCentered) 34.sp else 20.sp,
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCentered) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.graphicsLayer {
                            alpha = if (isCentered) 1f else 0.6f
                        }
                    )
                }
            }
        }
    }
}
