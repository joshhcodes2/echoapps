package com.example.echorooms.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.echorooms.data.database.entity.MemoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for memory entries within rooms.
 * Supports filtering by type and room, with reactive Flow returns.
 */
@Dao
interface MemoryEntryDao {

    @Query("SELECT * FROM memory_entries WHERE roomId = :roomId ORDER BY createdAt DESC")
    fun getEntriesForRoom(roomId: Long): Flow<List<MemoryEntry>>

    @Query("SELECT * FROM memory_entries WHERE id = :id")
    fun getEntryById(id: Long): Flow<MemoryEntry?>

    @Query("SELECT * FROM memory_entries WHERE roomId = :roomId AND type = :type ORDER BY createdAt DESC")
    fun getEntriesByType(roomId: Long, type: String): Flow<List<MemoryEntry>>

    @Query("SELECT COUNT(*) FROM memory_entries WHERE roomId = :roomId")
    fun getEntryCountForRoom(roomId: Long): Flow<Int>

    @Query("SELECT * FROM memory_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 20): Flow<List<MemoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MemoryEntry): Long

    @Update
    suspend fun update(entry: MemoryEntry)

    @Delete
    suspend fun delete(entry: MemoryEntry)

    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM memory_entries WHERE roomId = :roomId")
    suspend fun deleteAllForRoom(roomId: Long)
}
