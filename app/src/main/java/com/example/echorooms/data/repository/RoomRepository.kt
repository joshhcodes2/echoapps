package com.example.echorooms.data.repository

import com.example.echorooms.data.database.dao.RoomDao
import com.example.echorooms.data.database.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for memory room operations.
 * Abstracts data source from ViewModels.
 */
class RoomRepository(private val roomDao: RoomDao) {

    fun getAllRooms(): Flow<List<RoomEntity>> = roomDao.getAllRooms()

    fun getRoomById(id: Long): Flow<RoomEntity?> = roomDao.getRoomById(id)

    fun searchRooms(query: String): Flow<List<RoomEntity>> = roomDao.searchRooms(query)

    fun getFavoriteRooms(): Flow<List<RoomEntity>> = roomDao.getFavoriteRooms()

    fun getRoomsByTheme(theme: String): Flow<List<RoomEntity>> = roomDao.getRoomsByTheme(theme)

    fun getRoomCount(): Flow<Int> = roomDao.getRoomCount()

    suspend fun createRoom(room: RoomEntity): Long = roomDao.insert(room)

    suspend fun updateRoom(room: RoomEntity) = roomDao.update(room)

    suspend fun deleteRoom(room: RoomEntity) = roomDao.delete(room)

    suspend fun deleteRoomById(id: Long) = roomDao.deleteById(id)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        roomDao.updateFavorite(id, isFavorite)

    suspend fun touchRoom(id: Long) = roomDao.touchRoom(id)
}
