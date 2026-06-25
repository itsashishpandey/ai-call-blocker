package com.smartcallblocker.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartcallblocker.app.data.db.dao.BlacklistDao
import com.smartcallblocker.app.data.db.dao.BlockedCallDao
import com.smartcallblocker.app.data.db.dao.BlockingRuleDao
import com.smartcallblocker.app.data.db.dao.CallEventDao
import com.smartcallblocker.app.data.db.dao.TemporaryBlockDao
import com.smartcallblocker.app.data.db.dao.WhitelistDao
import com.smartcallblocker.app.data.db.entities.BlacklistEntity
import com.smartcallblocker.app.data.db.entities.BlockedCallEntity
import com.smartcallblocker.app.data.db.entities.BlockingRuleEntity
import com.smartcallblocker.app.data.db.entities.CallEventEntity
import com.smartcallblocker.app.data.db.entities.TemporaryBlockEntity
import com.smartcallblocker.app.data.db.entities.WhitelistEntity

@Database(
    entities = [
        BlockedCallEntity::class,
        BlockingRuleEntity::class,
        WhitelistEntity::class,
        BlacklistEntity::class,
        TemporaryBlockEntity::class,
        CallEventEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedCallDao(): BlockedCallDao
    abstract fun ruleDao(): BlockingRuleDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun blacklistDao(): BlacklistDao
    abstract fun temporaryBlockDao(): TemporaryBlockDao
    abstract fun callEventDao(): CallEventDao

    companion object {
        const val DB_NAME = "smart_call_blocker.db"
    }
}
