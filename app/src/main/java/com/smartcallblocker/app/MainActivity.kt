package com.smartcallblocker.app

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.smartcallblocker.app.data.preferences.SettingsRepository
import com.smartcallblocker.app.data.preferences.ThemeMode
import com.smartcallblocker.app.ui.navigation.AppNavGraph
import com.smartcallblocker.app.ui.navigation.Destination
import com.smartcallblocker.app.ui.theme.SmartCallBlockerTheme
import com.smartcallblocker.app.util.BiometricGate
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Resolve start destination once before composing so the splash route only fires for new sessions.
        var initialStart = Destination.Splash.route
        // We do a blocking read here only once at activity creation.
        kotlinx.coroutines.runBlocking {
            val onboarded = settings.onboardingCompletedSnapshot()
            initialStart = if (onboarded) Destination.Splash.route else Destination.Onboarding.route
        }

        setContent {
            val themeVm: ThemeViewModel = hiltViewModel()
            val mode by themeVm.themeMode.collectAsState()
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (mode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }
            SmartCallBlockerTheme(
                darkTheme = isDark,
                dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            ) {
                val lockVm: AppLockViewModel = hiltViewModel()
                val locked by lockVm.locked.collectAsState()

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController, startDestination = initialStart)

                    if (locked) {
                        LockOverlay(onUnlock = {
                            BiometricGate.show(
                                activity = this@MainActivity,
                                onSuccess = { lockVm.unlock() },
                            )
                        })
                    }
                }

                // Re-lock when the app pauses (when user backs out, switches apps, etc.)
                LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
                    lockVm.onPause()
                }
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    lockVm.onResume(this@MainActivity)
                }
            }
        }
    }
}

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        viewModelScope.launch {
            settings.themeMode.collectLatest { _themeMode.value = it }
        }
    }
}

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    /** Has the user authenticated in the current session? */
    private var sessionAuthenticated = false

    fun unlock() {
        sessionAuthenticated = true
        _locked.value = false
    }

    fun onPause() {
        sessionAuthenticated = false  // re-prompt on next resume
    }

    fun onResume(activity: FragmentActivity) {
        viewModelScope.launch {
            val needsLock = settings.appLock.first() && BiometricGate.canAuthenticate(activity)
            if (needsLock && !sessionAuthenticated) {
                _locked.value = true
                BiometricGate.show(
                    activity = activity,
                    onSuccess = { unlock() },
                )
            }
        }
    }
}

@Composable
private fun LockOverlay(onUnlock: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(46.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "AI Spam Call Blocker is locked",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Authenticate to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onUnlock) { Text("Unlock") }
        }
    }
}
