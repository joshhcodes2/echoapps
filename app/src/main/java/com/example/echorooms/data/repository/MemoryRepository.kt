package com.example.echorooms.data.repository

import com.example.echorooms.data.database.dao.MemoryEntryDao
import com.example.echorooms.data.database.entity.MemoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for memory entry operations within rooms.
 */
class MemoryRepository(private val memoryEntryDao: MemoryEntryDao) {

    fun getEntriesForRoom(roomId: Long): Flow<List<MemoryEntry>> =
        memoryEntryDao.getEntriesForRoom(roomId)

    fun getEntryById(id: Long): Flow<MemoryEntry?> =
        memoryEntryDao.getEntryById(id)

    fun getEntriesByType(roomId: Long, type: String): Flow<List<MemoryEntry>> =
        memoryEntryDao.getEntriesByType(roomId, type)

    fun getEntryCountForRoom(roomId: Long): Flow<Int> =
        memoryEntryDao.getEntryCountForRoom(roomId)

    fun getRecentEntries(limit: Int = 20): Flow<List<MemoryEntry>> =
        memoryEntryDao.getRecentEntries(limit)

    suspend fun addEntry(entry: MemoryEntry): Long =
        memoryEntryDao.insert(entry)

    suspend fun updateEntry(entry: MemoryEntry) =
        memoryEntryDao.update(entry)

    suspend fun deleteEntry(entry: MemoryEntry) =
        memoryEntryDao.delete(entry)

    suspend fun deleteEntryById(id: Long) =
        memoryEntryDao.deleteById(id)

    suspend fun deleteAllForRoom(roomId: Long) =
        memoryEntryDao.deleteAllForRoom(roomId)
}
