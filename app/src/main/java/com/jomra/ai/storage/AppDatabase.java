package com.jomra.ai.storage;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.jomra.ai.memory.MemoryDao;
import com.jomra.ai.memory.MemoryEntity;

@Database(entities = {MemoryEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract MemoryDao memoryDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "jomra_database")
                            .fallbackToDestructiveMigration() // TUBER: Destructive migration for simple schema evolution
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
