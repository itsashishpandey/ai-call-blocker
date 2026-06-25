package com.smartcallblocker.app.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartcallblocker.app.ui.components.EmptyState
import com.smartcallblocker.app.ui.components.ScreenTopBar
import com.smartcallblocker.app.ui.components.StatCard
import com.smartcallblocker.app.ui.theme.StatusAllowed
import com.smartcallblocker.app.ui.theme.StatusBlocked
import com.smartcallblocker.app.ui.theme.StatusPending
import com.smartcallblocker.app.ui.theme.StatusSilenced
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    vm: StatisticsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()

    Scaffold(topBar = { ScreenTopBar(title = "Statistics", onBack = onBack) }) { padding ->
        if (state.totalBlocked == 0) {
            EmptyState(
                icon = Icons.Outlined.Insights,
                title = "No data yet",
                description = "Your blocked-call statistics will appear here as the app blocks calls.",
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top-line numbers
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Today",
                    value = state.blockedToday.toString(),
                    icon = Icons.Rounded.Today,
                    tint = StatusBlocked,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "This week",
                    value = state.blockedThisWeek.toString(),
                    icon = Icons.Rounded.ViewWeek,
                    tint = StatusSilenced,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "This month",
                    value = state.blockedThisMonth.toString(),
                    icon = Icons.Rounded.CalendarMonth,
                    tint = StatusPending,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "All time",
                    value = state.totalBlocked.toString(),
                    icon = Icons.Rounded.History,
                    tint = StatusAllowed,
                    modifier = Modifier.weight(1f),
                )
            }

            // 30-day chart
            ChartCard(state.last30Days)

            // Top rules / countries / numbers
            if (state.topRules.isNotEmpty()) {
                BarListCard(title = "Top blocked rules", items = state.topRules, accent = StatusBlocked)
            }
            if (state.topCountries.isNotEmpty()) {
                BarListCard(title = "Top countries", items = state.topCountries, accent = StatusSilenced)
            }
            if (state.topNumbers.isNotEmpty()) {
                BarListCard(title = "Top blocked numbers", items = state.topNumbers, accent = StatusPending)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChartCard(days: List<DayCount>) {
    val color = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val max = (days.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    val total = days.sumOf { it.count }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Last 30 days",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "$total blocked",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                val w = size.width
                val h = size.height
                val barAreaH = h - 14f // leave room for labels
                val gap = 2f
                val barW = (w - gap * (days.size - 1)) / days.size

                // baseline grid
                drawLine(
                    color = gridColor,
                    start = Offset(0f, barAreaH),
                    end = Offset(w, barAreaH),
                    strokeWidth = 1f,
                )
                drawLine(
                    color = gridColor,
                    start = Offset(0f, barAreaH / 2f),
                    end = Offset(w, barAreaH / 2f),
                    strokeWidth = 1f,
                )

                // bars
                days.forEachIndexed { i, d ->
                    val frac = d.count.toFloat() / max
                    val barH = (barAreaH - 6f) * frac
                    val x = i * (barW + gap)
                    val y = barAreaH - barH
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barW, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val first = days.firstOrNull()?.dayStart
                val last = days.lastOrNull()?.dayStart
                val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
                Text(first?.let { fmt.format(Date(it)) } ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(last?.let { fmt.format(Date(it)) } ?: "Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BarListCard(title: String, items: List<LabelCount>, accent: Color) {
    val max = (items.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            items.forEach { item ->
                BarRow(label = item.label, count = item.count, max = max, accent = accent)
            }
        }
    }
}

@Composable
private fun BarRow(label: String, count: Int, max: Int, accent: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent,
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = count.toFloat() / max)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
        }
    }
}
