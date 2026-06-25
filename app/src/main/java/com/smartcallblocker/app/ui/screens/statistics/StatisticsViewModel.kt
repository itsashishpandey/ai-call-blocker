package com.smartcallblocker.app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.data.repository.BlockedCallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatisticsUiState(
    val totalBlocked: Int = 0,
    val blockedToday: Int = 0,
    val blockedThisWeek: Int = 0,
    val blockedThisMonth: Int = 0,
    /** Day-by-day for the last 30 days, oldest first. */
    val last30Days: List<DayCount> = emptyList(),
    val topRules: List<LabelCount> = emptyList(),
    val topCountries: List<LabelCount> = emptyList(),
    val topNumbers: List<LabelCount> = emptyList(),
)

data class DayCount(val dayStart: Long, val count: Int)
data class LabelCount(val label: String, val count: Int)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repo: BlockedCallRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsUiState())
    val state: StateFlow<StatisticsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAll().collectLatest { calls ->
                _state.value = compute(calls)
            }
        }
    }

    private fun compute(calls: List<BlockedCallEntity>): StatisticsUiState {
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val weekStart = startOfWeek(now)
        val monthStart = startOfMonth(now)

        val days = (29 downTo 0).map { offset ->
            val dayStart = todayStart - offset * DAY_MS
            val dayEnd = dayStart + DAY_MS
            val n = calls.count { it.callDateTime in dayStart until dayEnd }
            DayCount(dayStart, n)
        }

        val topRules = calls
            .groupingBy { it.matchedRuleName ?: it.matchedRuleType ?: "Unknown" }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { LabelCount(it.first, it.second) }

        val topCountries = calls
            .mapNotNull { it.countryCode?.takeIf { c -> c.isNotBlank() } }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { LabelCount(it.first, it.second) }

        val topNumbers = calls
            .filter { it.normalizedNumber.isNotBlank() }
            .groupingBy { it.normalizedNumber }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { LabelCount(it.first, it.second) }

        return StatisticsUiState(
            totalBlocked = calls.size,
            blockedToday = calls.count { it.callDateTime >= todayStart },
            blockedThisWeek = calls.count { it.callDateTime >= weekStart },
            blockedThisMonth = calls.count { it.callDateTime >= monthStart },
            last30Days = days,
            topRules = topRules,
            topCountries = topCountries,
            topNumbers = topNumbers,
        )
    }

    private fun startOfDay(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfWeek(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }.timeInMillis

    private fun startOfMonth(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        set(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
