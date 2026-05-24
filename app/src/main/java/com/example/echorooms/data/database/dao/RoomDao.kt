package com.example.echorooms.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.echorooms.data.database.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for memory rooms.
 * All queries return Flow for reactive UI updates.
 */
@Dao
interface RoomDao {

    @Query("SELECT * FROM rooms ORDER BY updatedAt DESC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun getRoomById(id: Long): Flow<RoomEntity?>

    @Query("SELECT * FROM rooms WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchRooms(query: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteRooms(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE moodTheme = :theme ORDER BY updatedAt DESC")
    fun getRoomsByTheme(theme: String): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: RoomEntity): Long

    @Update
    suspend fun update(room: RoomEntity)

    @Delete
    suspend fun delete(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE rooms SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE rooms SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touchRoom(id: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM rooms")
    fun getRoomCount(): Flow<Int>
}
