package com.example.echorooms

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.echorooms.ui.main.MainScreen
import com.example.echorooms.ui.create.CreateRoomScreen
import com.example.echorooms.ui.detail.RoomDetailScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(
                    onItemClick = { navKey -> backStack.add(navKey) }
                )
            }
            entry<CreateRoom> {
                CreateRoomScreen(
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<RoomDetail> { roomDetail ->
                RoomDetailScreen(
                    roomId = roomDetail.roomId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
