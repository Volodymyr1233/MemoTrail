package com.example.memotrail.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.memotrail.data.local.dao.MediaEntryDao
import com.example.memotrail.data.local.dao.TripDao
import com.example.memotrail.data.local.dao.TripDayDao
import com.example.memotrail.data.local.dao.TripTagDao
import com.example.memotrail.data.local.entity.MediaEntryEntity
import com.example.memotrail.data.local.entity.TagEntity
import com.example.memotrail.data.local.entity.TripDayEntity
import com.example.memotrail.data.local.entity.TripEntity
import com.example.memotrail.data.local.entity.TripTagCrossRefEntity

@Database(
    entities = [
        TripEntity::class,
        TripDayEntity::class,
        MediaEntryEntity::class,
        TagEntity::class,
        TripTagCrossRefEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MemoTrailDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun tripDayDao(): TripDayDao
    abstract fun mediaEntryDao(): MediaEntryDao
    abstract fun tripTagDao(): TripTagDao

    companion object {
        private const val DB_NAME = "memotrail.db"

        fun create(context: Context): MemoTrailDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                MemoTrailDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
