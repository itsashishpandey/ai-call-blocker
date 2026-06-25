package com.smartcallblocker.app.di

import android.content.Context
import androidx.room.Room
import com.smartcallblocker.app.data.db.AppDatabase
import com.smartcallblocker.app.data.db.dao.BlacklistDao
import com.smartcallblocker.app.data.db.dao.BlockedCallDao
import com.smartcallblocker.app.data.db.dao.BlockingRuleDao
import com.smartcallblocker.app.data.db.dao.CallEventDao
import com.smartcallblocker.app.data.db.dao.TemporaryBlockDao
import com.smartcallblocker.app.data.db.dao.WhitelistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBlockedCallDao(db: AppDatabase): BlockedCallDao = db.blockedCallDao()
    @Provides fun provideRuleDao(db: AppDatabase): BlockingRuleDao = db.ruleDao()
    @Provides fun provideWhitelistDao(db: AppDatabase): WhitelistDao = db.whitelistDao()
    @Provides fun provideBlacklistDao(db: AppDatabase): BlacklistDao = db.blacklistDao()
    @Provides fun provideTemporaryBlockDao(db: AppDatabase): TemporaryBlockDao = db.temporaryBlockDao()
    @Provides fun provideCallEventDao(db: AppDatabase): CallEventDao = db.callEventDao()
}
