package com.example.echorooms.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echorooms.data.database.entity.RoomEntity
import com.example.echorooms.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MainScreenUiState {
    object Loading : MainScreenUiState
    data class Error(val throwable: Throwable) : MainScreenUiState
    data class Success(val rooms: List<RoomEntity>) : MainScreenUiState
}

class MainScreenViewModel(private val roomRepository: RoomRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val showOnlyFavorites = MutableStateFlow(false)

    val uiState: StateFlow<MainScreenUiState> = combine(
        roomRepository.getAllRooms(),
        searchQuery,
        showOnlyFavorites
    ) { rooms, query, onlyFavs ->
        val filtered = rooms.filter { room ->
            val matchesQuery = room.title.contains(query, ignoreCase = true) ||
                    room.description.contains(query, ignoreCase = true)
            val matchesFav = !onlyFavs || room.isFavorite
            matchesQuery && matchesFav
        }
        MainScreenUiState.Success(filtered)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainScreenUiState.Loading
    )

    fun toggleFavorite(room: RoomEntity) {
        viewModelScope.launch {
            roomRepository.toggleFavorite(room.id, !room.isFavorite)
        }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch {
            roomRepository.deleteRoom(room)
        }
    }
}
