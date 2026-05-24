package com.example.echorooms

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey
@Serializable data object CreateRoom : NavKey
@Serializable data class RoomDetail(val roomId: Long) : NavKey
