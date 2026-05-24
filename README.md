# EchoRooms 🌌
### *Futuristic Emotional Memory Capsule for Android*

EchoRooms is an ambient, multi-sensory journaling and memory preservation application for Android. Built using **Jetpack Compose**, it moves beyond traditional text-based journals to archive human memories as immersive "chambers." Each chamber has its own unique atmospheric color profile, procedural soundscapes, real-time emotional visualization, physical parallax depth, and state-of-the-art security layers.

---

## 🛠 Tech Stack & Architecture

- **Core**: Kotlin & Jetpack Compose (featuring the next-generation **Navigation3** runtime).
- **Design System**: Custom cinematic Glassmorphic architecture, real-time canvas-based aura mapping, and neon glow rendering pipelines.
- **Local Storage**: Offline-first design built on **Room database** (SQLite) to store chambers and memory timeline items.
- **Hardware Integration**:
  - **Sensors**: Physical accelerometer-bound parallax tilt bindings.
  - **Security**: Android Biometric Authentication (fingerprint verification).
  - **Haptics**: Advanced vibration patterns (haptic charging and explosive pulses).
- **Sensory Processing**:
  - **Audio Engine**: Low-latency AudioRecord and MediaPlayer wrappers.
  - **Procedural Soundscape**: Synthetic background humming synchronized with the active atmosphere.
  - **Image Engine**: Coil3 for memory-efficient local caching and rendering.

---

## 🌌 Core Features

### 1. Synesthesia Aura Core
Visualizes the emotional spectrum of your journal entry. It computes real-time ratios of **Warmth, Melancholy, Anxiety, and Peace**, converting them into a moving fluid aura (using organic Bézier curves) and glowing gradients.
- **Warmth**: Increases the core size and outer glow.
- **Melancholy**: Slows the rotation rate and physically drags the center downwards.
- **Anxiety**: Introduces high-frequency noise and spiky ripples to the aura's edges.

### 2. Custom Atmosphere Designer
Choose from 6 premium atmospheric presets or design your own:
- ⚡ **Cyberpunk**: Neon-drenched digital pinks and cyans.
- 🌧 **Rainy Night**: Moody reflections and quiet blue storms.
- 🌅 **Sunset Glow**: Warm orange fading into memory.
- 🚀 **Space Drift**: Ethereal purple voids and cosmic whispers.
- 💻 **Retro Terminal**: Phosphor green and digital echoes.
- 🌙 **Midnight Blue**: Deep blue contemplation.
- 🧪 **Synthesizer**: Synthesize your custom RGB colors, choose particle drift styles, and select the background procedural hum.

### 3. Holographic Parallax Depth
The main chamber banner integrates accelerometer sensor data. When you tilt your physical phone, the banner shifts and tilts in 3D space, creating a physical hologram effect.

### 4. Scheduled Time Capsules & Ritual Decryption
Archive rooms under lock and key. You can seal a chamber for a specific period (e.g. 24 hours, 7 days). 
- **Decryption Ritual**: To unlock a time capsule, you must trigger a biometric ritual. By pressing and holding the glowing core, the app generates a 3-second vibrating haptic charge. Once fully charged, it triggers a haptic explosion and unlocks the chamber.

### 5. Multi-Sensory Memory Timeline
Within each room, users can archive different formats:
- 📝 **Text Journals**: Sleek note cards with markdown display support.
- 🎙 **Voice Memos**: Live waveform visualizer during recording and ambient playback.
- 📸 **Photo Memories**: Local image integration.
- 🎨 **Sketch Pads**: Integrated drawing canvas to paint your mood.
- ⭐ **Milestone Constellations**: Renders milestones as stars in a connected astronomical map.

---

## 📂 Project Structure

```
app/src/main/java/com/example/echorooms/
│
├── data/
│   ├── audio/         # Voice recording, playback, and procedural hum generator
│   ├── database/      # SQLite Entities (Room, MemoryEntry, CustomThemeData), DAOs, and DB setup
│   └── export/        # Atmospheric Exporter (share chambers as backups/JSON packages)
│
├── hardware/          # Parallax accelerometer bindings and haptic vibration logic
│
├── security/          # Biometric fingerprint scanner bridge
│
├── theme/             # Color systems, typography, and premium custom modifiers (.neonGlow, .glassmorphic)
│
└── ui/
    ├── components/    # Reusable UI widgets (DrawingCanvas, WaveformVisualizer, AuraBlob, ConstellationMap)
    ├── create/        # Chamber creation flow and Custom Theme Designer ViewModels
    ├── detail/        # Chamber detail timeline workspace
    └── main/          # Dashboard display with grid layout and quick search/filter toggles
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Koala or newer recommended).
- **Android SDK 34+** (Targeting SDK 35).
- **JDK 17** configured in your IDE.
- A physical Android device is recommended to experience the **Parallax Accel Sensor** and **Haptic Feedback engine**.

### Installation & Run
1. Clone this repository:
   ```bash
   git clone https://github.com/joshhcodes2/echoapps.git
   ```
2. Open the project in Android Studio.
3. Allow Gradle to sync and fetch all dependencies.
4. Connect your Android device or start an emulator.
5. Click **Run** (`Shift + F10`) to deploy to your device.
