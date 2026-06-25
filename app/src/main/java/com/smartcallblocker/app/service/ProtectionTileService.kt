package com.smartcallblocker.app.service

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.smartcallblocker.app.R
import com.smartcallblocker.app.data.preferences.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Quick Settings tile — lets the user toggle protection from the system shade.
 */
@AndroidEntryPoint
class ProtectionTileService : TileService() {

    @Inject lateinit var settings: SettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val current = settings.protectionEnabledSnapshot()
            settings.setProtectionEnabled(!current)
            refreshTile()
        }
    }

    private fun refreshTile() {
        val tile = qsTile ?: return
        val enabled = runBlocking { settings.protectionEnabledSnapshot() }
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.contentDescription = if (enabled)
            getString(R.string.protection_active) else getString(R.string.protection_inactive)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_qs_shield)
        tile.updateTile()
    }
}
