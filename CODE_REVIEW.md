# 🔍 EchoRooms Code Review Report

This document records the architectural reviews, storage leak audits, and code analysis for the **EchoRooms** Android codebase.

---

## 🚨 Critical Bugs & Runtime Crash Vectors

### 1. `MediaRecorder` Context Instantiation Crash (SDK >= 31)
* **Location:** [`AudioRecorderManager.kt:L52-55`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/audio/AudioRecorderManager.kt#L52-L55)
* **The Issue:** 
  On Android S (API 31) and higher, the app initializes the `MediaRecorder` using a manually instantiated `Application` object:
  ```kotlin
  mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      MediaRecorder(android.app.Application())
  }
  ```
  Creating an instance of `Application` via `Application()` is a direct violation of the Android framework. Since the newly constructed object is uninitialized (missing its underlying context base and initialization hooks), calling recorder methods on it will crash the app with an `IllegalStateException` or `NullPointerException`.
* **Recommendation:**
  Modify `startRecording` to accept a valid `Context` parameter (e.g., `context.applicationContext`), or pass it into the constructor of `AudioRecorderManager`.
  ```kotlin
  fun startRecording(context: Context, file: File) {
       mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           MediaRecorder(context.applicationContext)
       }
  ```

---

### 2. Broken Glowing Effect in HTML Export (Invalid CSS)
* **Location:** [`AtmosphericExporter.kt:L77`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/export/AtmosphericExporter.kt#L77) and [`L133`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/export/AtmosphericExporter.kt#L133)
* **The Issue:**
  In `AtmosphericExporter.kt`, the glowing shadow effect in the exported HTML is compiled using:
  ```css
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5), 0 0 20px rgba(${Integer.parseInt(glowHex.substring(1), 16)}, 0.15);
  ```
  If `glowHex` is `#FF0080`, this compiles to `rgba(16711808, 0.15)`. This is invalid CSS syntax (as `rgba` expects three comma-separated integers for red, green, and blue). The browser will discard this style block, completely breaking the glowing look.
* **Recommendation:**
  Extract the individual red, green, and blue values from the color hex and format it as `rgba(R, G, B, A)`:
  ```kotlin
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5), 0 0 20px rgba(${Integer.parseInt(glowHex.substring(1, 3), 16)}, ${Integer.parseInt(glowHex.substring(3, 5), 16)}, ${Integer.parseInt(glowHex.substring(5, 7), 16)}, 0.15);
  ```

---

## 💾 Performance & Storage Leaks

### 3. Orphaned File System Storage Leaks
* **Location:** [`RoomRepository.kt:L29-31`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/repository/RoomRepository.kt#L29-L31) and [`MemoryEntry.kt:L25-32`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/database/entity/MemoryEntry.kt#L25-L32)
* **The Issue:**
  The `MemoryEntry` database table is set up with `onDelete = ForeignKey.CASCADE`. When a user deletes a room via `roomRepository.deleteRoom(room)`, Room automatically deletes the matching database records for all entries.
  However, **the actual audio (.m4a), images (.jpg), and drawings (.png) stored on the user's local disk (`filesDir` or `cacheDir`) are never deleted**. The database rows disappear, but the physical files are orphaned on disk, leaking storage space permanently.
* **Recommendation:**
  When deleting a room, query all its memory entries first, delete their physical files, delete the cover image, and then delete the room database entry:
  ```kotlin
  suspend fun deleteRoomAndFiles(context: Context, room: RoomEntity, entries: List<MemoryEntry>) {
      // 1. Delete all media files associated with the entries
      entries.forEach { entry ->
          entry.filePath?.let { File(it).takeIf { f -> f.exists() }?.delete() }
      }
      // 2. Delete the room cover image file
      room.coverImagePath?.let { File(it).takeIf { f -> f.exists() }?.delete() }
      // 3. Delete from Database
      roomRepository.deleteRoom(room)
  }
  ```

---

## ⚙ Coroutines & Concurrency Safeguards

### 4. Permanent Coroutine Scope Termination Vulnerability
* **Location:** [`AudioPlayerManager.kt:L43`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/audio/AudioPlayerManager.kt#L43) and [`ProceduralSoundscapePlayer.kt:L14`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/audio/ProceduralSoundscapePlayer.kt#L14)
* **The Issue:**
  Both player classes initialize their coroutine scopes as follows:
  ```kotlin
  private val scope = CoroutineScope(Dispatchers.Main)
  // and
  private val scope = CoroutineScope(Dispatchers.Default)
  ```
  Since these scopes are created without a `Job()` or `SupervisorJob()`, they default to standard jobs. If *any* background coroutine in these scopes throws an uncaught exception (e.g. `MediaPlayer` throwing a status exception or a rendering calculation failing), the exception propagates up and **cancels the entire scope permanently**. All subsequent attempts to play voice logs or generate sound hums will silently fail.
* **Recommendation:**
  Always attach a `SupervisorJob` to application-level or class-level scopes:
  ```kotlin
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  ```

### 5. `MediaPlayer` State Exceptions inside Loops
* **Location:** [`AudioPlayerManager.kt:L183`](file:///d:/echoapps/EchoRooms/app/src/main/java/com/example/echorooms/data/audio/AudioPlayerManager.kt#L183)
* **The Issue:**
  In `startPositionTracking()`, the coroutine reads the media player position inside a loop:
  ```kotlin
  _currentPosition.value = mediaPlayer?.currentPosition ?: 0
  ```
  If the `MediaPlayer` transitions to an `Error` or `Idle` state asynchronously due to audio hardware failures, calling `.currentPosition` will throw an `IllegalStateException`, causing a crash.
* **Recommendation:**
  Wrap the query in a safe try-catch or state check:
  ```kotlin
  val position = try {
      if (mediaPlayer?.isPlaying == true) mediaPlayer?.currentPosition ?: 0 else 0
  } catch (e: IllegalStateException) {
      0
  }
  _currentPosition.value = position
  ```
