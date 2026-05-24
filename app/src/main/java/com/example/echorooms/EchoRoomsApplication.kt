package com.example.echorooms

import android.app.Application
import com.example.echorooms.data.database.EchoRoomsDatabase
import com.example.echorooms.data.repository.MemoryRepository
import com.example.echorooms.data.repository.RoomRepository

class EchoRoomsApplication : Application() {
    lateinit var database: EchoRoomsDatabase
        private set

    lateinit var roomRepository: RoomRepository
        private set

    lateinit var memoryRepository: MemoryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = EchoRoomsDatabase.getDatabase(this)
        roomRepository = RoomRepository(database.roomDao())
        memoryRepository = MemoryRepository(database.memoryEntryDao())
    }
}
