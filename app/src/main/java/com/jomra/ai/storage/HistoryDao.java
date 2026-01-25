package com.jomra.ai.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryEntity history);

    @Query("SELECT * FROM conversation_history ORDER BY timestamp DESC")
    List<HistoryEntity> getAll();

    @Query("SELECT * FROM conversation_history ORDER BY timestamp DESC LIMIT :limit")
    List<HistoryEntity> getRecent(int limit);

    @Query("DELETE FROM conversation_history")
    void deleteAll();
}
