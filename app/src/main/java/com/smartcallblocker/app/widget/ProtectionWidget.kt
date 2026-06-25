package com.smartcallblocker.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.smartcallblocker.app.MainActivity
import com.smartcallblocker.app.data.preferences.SettingsRepository
import com.smartcallblocker.app.data.repository.BlockedCallRepository
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ProtectionWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun blockedRepo(): BlockedCallRepository
        fun settings(): SettingsRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val today = startOfToday()
        val blockedToday = entry.blockedRepo().countSince(today).first()
        val total = entry.blockedRepo().countAll().first()
        val enabled = entry.settings().protectionEnabled.first()

        provideContent {
            GlanceTheme {
                WidgetContent(enabled = enabled, blockedToday = blockedToday, total = total)
            }
        }
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private val brandPrimary = Color(0xFF1E40AF)
private val brandPrimaryDark = Color(0xFF1E3A8A)
private val brandSecondary = Color(0xFF3B82F6)
private val brandDanger = Color(0xFFEF4444)

@androidx.compose.runtime.Composable
private fun WidgetContent(enabled: Boolean, blockedToday: Int, total: Int) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(if (enabled) brandPrimary else brandDanger)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(14.dp),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (enabled) "PROTECTED" else "OFF",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = blockedToday.toString(),
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = "blocked today",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                    fontSize = 11.sp,
                ),
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = "$total all-time",
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                    fontSize = 10.sp,
                ),
            )
        }
    }
}
