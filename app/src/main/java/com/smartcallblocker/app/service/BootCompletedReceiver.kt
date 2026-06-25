package com.smartcallblocker.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartcallblocker.app.data.repository.TemporaryBlockRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * On boot we expire stale temporary blocks so a number doesn't stay blocked
 * across reboots once its window has passed.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var tempRepo: TemporaryBlockRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return
        val pending = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                tempRepo.expireOld()
                tempRepo.purgeExpired()
            } finally {
                pending.finish()
            }
        }
    }
}
