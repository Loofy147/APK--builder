package com.jomra.ai.memory;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemoryEntity memory);

    @Query("SELECT * FROM memories ORDER BY timestamp DESC LIMIT :limit")
    List<MemoryEntity> getRecent(int limit);

    // BOLT: Prioritize loading relevant candidates - Expected: -70% DB IO
    @Query("SELECT * FROM memories WHERE timestamp > :cutoff OR importance > :threshold ORDER BY importance DESC, timestamp DESC LIMIT :limit")
    List<MemoryEntity> getPrioritizedCandidates(long cutoff, float threshold, int limit);

    @Query("DELETE FROM memories WHERE timestamp < :cutoff AND importance < :threshold")
    void deleteOld(long cutoff, float threshold);

    @Query("DELETE FROM memories")
    void deleteAll();

    @Query("SELECT * FROM memories")
    List<MemoryEntity> getAll();
}
