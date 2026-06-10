# AGENTS.md — RedefineNCM (Android)

Guidance for AI coding agents working in this repository. Keep it current when the
architecture changes.

## What this is

RedefineNCM is a third-party **NetEase Cloud Music (网易云音乐)** client for Android,
written in Kotlin + Jetpack Compose. It talks to a self-hosted
[NeteaseCloudMusicApi](https://github.com/Binaryify/NeteaseCloudMusicApi)-style backend
(base URL is user-configurable in Settings) and plays audio through media3/ExoPlayer with a
foreground `MediaSessionService`.

A Kotlin Multiplatform port of this app lives in the sibling `../RedefineNCM_KMP` directory.

## Module layout

- `app/` — the Android application (single module, all features).
- `serverMocker/` — an in-app NanoHTTPD mock server used for tests/offline development.
- `gradle/libs.versions.toml` — **the single source of truth for all versions.** Add or
  bump dependencies here, never inline in `build.gradle.kts`.

## Package map (`app/src/main/java/com/redstar/redefinencm/`)

- `activity/` — Compose screens hosted by Activities. `SplashActivity` → `LoginActivity`
  (QR login) → `mainActivity/MainActivity` (recommend / search / playlist pages),
  `NowPlayingActivity` (full player + lyrics), `SettingActivity`.
- `viewmodel/` — `MainViewModel`, `NowPlayingViewModel`, `LoginViewModel`. These own the
  `MediaController` connection and expose `StateFlow`s to the UI.
- `services/PlaybackService.kt` — `MediaSessionService` hosting the `ExoPlayer`. Also drives
  lyric syncing via the `LyricBus` (`SharedFlow`s) and the live-update notification lyric.
- `data/api/` — Retrofit `NCMApi` + `RetrofitInstance` (OkHttp interceptors inject `realIP`,
  `timestamp`, and cookie). `data/api/data/` holds response DTOs.
- `data/db/` — Room `AppDatabase`, `Dao`, and `entity/` cache entities. `DatabaseProvider`
  is a manual singleton; `TypeConverter` handles list/JSON columns.
- `data/Repository.kt` — the single repository; wraps API + DAO, exposes `Flow`s, and
  implements the cache-then-network pattern used across the app.
- `util/` — `DataSource.kt` (`RedirectingDataSourceFactory` resolves
  `redefinencm://playbackPlaceHolder?id=…` URIs to real stream URLs at play time),
  `DataStoreManager`/`SettingProvider` (preferences), `LyricParser`,
  `LiveUpdateLyricController`, `DownloadWorker` (WorkManager), `ImageParser`.

## How playback works (important context)

1. A playlist is loaded as media3 `MediaItem`s whose URI is a placeholder
   (`redefinencm://playbackPlaceHolder?id=<songId>`), not the real audio URL.
2. `RedirectingDataSourceFactory` (in `util/DataSource.kt`) intercepts the data-source open
   and swaps in the real CDN URL fetched from the API — so stream URLs stay fresh and are
   never persisted.
3. `NowPlayingViewModel` connects to `PlaybackService` via a `MediaController` and mirrors
   player state into `StateFlow`s.

### Shuffle / queue ordering invariant (regression-prone)

The visible queue (`playList`), the window-order index list (`playOrderWindowIndices`), and
the current-item highlight (`currentMediaIndexInList`) **must always be rebuilt together from
the current `Timeline`** — see `NowPlayingViewModel.rebuildPlaylistFromTimeline()`. Any code
path that changes order (shuffle toggle, `onTimelineChanged`, `onMediaItemTransition`) must
go through that one rebuild. Reading the highlight against a cached index list is the original
bug; do not reintroduce it. The pure, unit-tested model of this logic lives in the KMP repo
at `shared/.../player/PlayQueue.kt`.

## Build & run

- **JDK 11** target (`compileOptions` / `kotlin.jvmTarget`). Needs an Android SDK
  (`compileSdk = 36`, `minSdk = 29`).
- `./gradlew :app:assembleDebug` — build the app.
- `./gradlew :app:testDebugUnitTest` — JVM unit tests.
- `./gradlew :app:connectedDebugAndroidTest` — instrumented tests (needs a device/emulator).
- `versionName` comes from `git describe --tags`; `versionCode` is a timestamp. A build
  without git tags falls back to `GIT_FAILED` — harmless locally.

## Conventions

- **Compose-only UI**, Material 3 (`androidx.compose.material3`). Theming in `ui/theme/`.
- **Versions live in `libs.versions.toml`.** Reference them via `libs.*` aliases.
- State flows down via `StateFlow`/`collectAsState`; events go up via lambdas. ViewModels
  obtain `Context`/DB through the `RedefineNCMApplication` singleton and `DatabaseProvider`.
- Wrap network calls in `safeApiCall { … }` (in `data/api`). Cache to Room, expose via `Flow`.
- Some inline Chinese comments document intent — preserve them when editing nearby code.

## UI design direction

The app intentionally uses an aggressive Material 3 Expressive direction across **all** pages,
not only playlist detail. When changing UI, keep the visual language consistent with the
playlist redesign from `39f947bdb140f4e8f8d69b14f2b1e1bf0a1ffde5`.

- Use `MaterialTheme.colorScheme.surface` as the page base and `surfaceContainerHigh` /
  `surfaceContainerHighest` for tonal panels and list rows.
- Media/profile pages should use image-derived or primary-container gradient hero areas,
  oversized rounded artwork/avatar treatment, bold headline typography, and clear tonal depth.
- Use prominent primary pill actions and filled/tonal icon buttons for playback, search,
  download, queue, comments, and settings actions.
- Lists should prefer the connected-list shape language: large outer corners, tight inner
  corners, tiny vertical gaps. Reuse `ui/component/Expressive.kt` (`connectedListItemShape`)
  instead of rebuilding the shape logic locally.
- Avoid reintroducing old isolated elevated `Card` rows for ordinary lists. Prefer clickable
  `Surface` with M3 container roles unless a true standalone card is needed.
- `NowPlayingActivity` is the most important screen for this style: keep the album-color
  gradient hero, large rounded cover, tonal lyric panel, prominent transport controls, and
  connected-list queue/comments sheets.
- Keep the same direction on recommend, search, user playlist, settings, login, and mini-player
  surfaces so the app does not regress into mixed visual systems.
- Prefer color-scheme roles and image-extracted colors over hardcoded palettes. Use hardcoded
  black/white only for contrast overlays or derived readable content color.

## Dependency status

All versions live in `libs.versions.toml`; it is at the latest available across the board:
AGP 9.0.1, Kotlin 2.3.0, Compose BOM 2026.01.01, media3 1.9.1, Room 2.8.4, OkHttp 5.3.2,
Retrofit 3.0.0, **Coil 3.0.4** (migrated from Coil 2 — Coil 3 splits the network fetcher into
`coil-network-okhttp`). Dead legacy ExoPlayer 2.x was removed (superseded by media3). When
bumping, keep Kotlin / KSP / Compose-compiler versions in lockstep — they are tightly coupled.

## Gotchas

- The project pins very recent / pre-release toolchain versions (AGP 9, Kotlin 2.3,
  Compose BOM 2026.01). Verify a matching Android Gradle/JDK before assuming a red build is
  your change.
- `usesCleartextTraffic="true"` is intentional (self-hosted HTTP API).
- DB access is via a hand-rolled `DatabaseProvider` singleton, **not** Hilt, despite the
  `hilt-android` dependency being present.
