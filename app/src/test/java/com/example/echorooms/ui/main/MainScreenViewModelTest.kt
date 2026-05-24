package com.example.echorooms.ui.main

import com.example.echorooms.data.database.dao.RoomDao
import com.example.echorooms.data.database.entity.RoomEntity
import com.example.echorooms.data.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun uiState_initiallyLoading() = runTest {
        val fakeDao = FakeRoomDao()
        val repository = RoomRepository(fakeDao)
        val viewModel = MainScreenViewModel(repository)
        assertEquals(MainScreenUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_displaysRoomsWhenEmitted() = runTest {
        val fakeDao = FakeRoomDao()
        val repository = RoomRepository(fakeDao)
        val viewModel = MainScreenViewModel(repository)

        val room1 = RoomEntity(id = 1, title = "Neon Dreams", description = "Cyberpunk mood")
        val room2 = RoomEntity(id = 2, title = "Forest Walk", description = "Nature mood")
        fakeDao.emit(listOf(room1, room2))

        val state = viewModel.uiState.first { it is MainScreenUiState.Success }
        assertTrue(state is MainScreenUiState.Success)
        val rooms = (state as MainScreenUiState.Success).rooms
        assertEquals(2, rooms.size)
        assertEquals("Neon Dreams", rooms[0].title)
        assertEquals("Forest Walk", rooms[1].title)
    }

    @Test
    fun uiState_filtersBySearchQuery() = runTest {
        val fakeDao = FakeRoomDao()
        val repository = RoomRepository(fakeDao)
        val viewModel = MainScreenViewModel(repository)

        val room1 = RoomEntity(id = 1, title = "Neon Dreams", description = "Cyberpunk mood")
        val room2 = RoomEntity(id = 2, title = "Forest Walk", description = "Nature mood")
        fakeDao.emit(listOf(room1, room2))

        viewModel.searchQuery.value = "forest"

        val state = viewModel.uiState.first { state ->
            state is MainScreenUiState.Success && state.rooms.size == 1
        }
        val rooms = (state as MainScreenUiState.Success).rooms
        assertEquals(1, rooms.size)
        assertEquals("Forest Walk", rooms[0].title)
    }

    @Test
    fun uiState_filtersByFavorites() = runTest {
        val fakeDao = FakeRoomDao()
        val repository = RoomRepository(fakeDao)
        val viewModel = MainScreenViewModel(repository)

        val room1 = RoomEntity(id = 1, title = "Neon Dreams", isFavorite = true)
        val room2 = RoomEntity(id = 2, title = "Forest Walk", isFavorite = false)
        fakeDao.emit(listOf(room1, room2))

        viewModel.showOnlyFavorites.value = true

        val state = viewModel.uiState.first { state ->
            state is MainScreenUiState.Success && state.rooms.size == 1
        }
        val rooms = (state as MainScreenUiState.Success).rooms
        assertEquals(1, rooms.size)
        assertEquals("Neon Dreams", rooms[0].title)
    }
}

class FakeRoomDao : RoomDao {
    private val roomsFlow = MutableStateFlow<List<RoomEntity>>(emptyList())

    fun emit(rooms: List<RoomEntity>) {
        roomsFlow.value = rooms
    }

    override fun getAllRooms(): Flow<List<RoomEntity>> = roomsFlow

    override fun getRoomById(id: Long): Flow<RoomEntity?> {
        return roomsFlow.map { list -> list.find { it.id == id } }
    }

    override fun searchRooms(query: String): Flow<List<RoomEntity>> {
        return roomsFlow.map { list ->
            list.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
    }

    override fun getFavoriteRooms(): Flow<List<RoomEntity>> {
        return roomsFlow.map { list -> list.filter { it.isFavorite } }
    }

    override fun getRoomsByTheme(theme: String): Flow<List<RoomEntity>> {
        return roomsFlow.map { list -> list.filter { it.moodTheme == theme } }
    }

    override fun getRoomCount(): Flow<Int> {
        return roomsFlow.map { it.size }
    }

    override suspend fun insert(room: RoomEntity): Long {
        val current = roomsFlow.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        val roomWithId = room.copy(id = newId)
        current.add(roomWithId)
        roomsFlow.value = current
        return newId
    }

    override suspend fun update(room: RoomEntity) {
        val current = roomsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == room.id }
        if (index != -1) {
            current[index] = room
            roomsFlow.value = current
        }
    }

    override suspend fun delete(room: RoomEntity) {
        val current = roomsFlow.value.toMutableList()
        current.removeAll { it.id == room.id }
        roomsFlow.value = current
    }

    override suspend fun deleteById(id: Long) {
        val current = roomsFlow.value.toMutableList()
        current.removeAll { it.id == id }
        roomsFlow.value = current
    }

    override suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long) {
        val current = roomsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(isFavorite = isFavorite, updatedAt = updatedAt)
            roomsFlow.value = current
        }
    }

    override suspend fun touchRoom(id: Long, updatedAt: Long) {
        val current = roomsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            current[index] = current[index].copy(updatedAt = updatedAt)
            roomsFlow.value = current
        }
    }
}
