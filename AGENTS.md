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

### Shuffle / queue ordering invariant (regression-prone) — FIXED

The visible queue (`playList`), the window-order index list (`playOrderWindowIndices`), and
the current-item highlight (`currentMediaIndexInList`) **must always be rebuilt together from
the current `Timeline`** — see `NowPlayingViewModel.rebuildPlaylistFromTimeline()`.

**The bug:** `onMediaItemTransition` previously called `updateNowPlayingMediaIndex()` which
only recomputed the highlight from *cached* `playOrderWindowIndices`. Under shuffle, ExoPlayer
can regenerate its internal permutation without firing `onTimelineChanged`, rendering the
cached indices stale and causing the highlight to point at the wrong row.

**The fix (applied):** `onMediaItemTransition` now calls `refreshOnTrackTransition()` which
always delegates to `rebuildPlaylistFromTimeline()`. This ensures every track change triggers
a full rebuild of all three values from the current Timeline, so they can never disagree.

The shuffle toggle (`onShuffleModeEnabledChanged`) and timeline changes
(`onTimelineChanged`) also go through `rebuildPlaylistFromTimeline()` — the same single
rebuild path. Do not reintroduce separate update paths that read one value from cache and
another from the timeline.

The pure, unit-tested model of this logic lives in the KMP repo at
`shared/.../player/PlayQueue.kt`, with the regression suite in
`shared/src/commonTest/.../player/PlayQueueTest.kt`. (`NowPlayingViewModel`'s
`rebuildPlaylistFromTimeline()` is the simplified queue+index form still in use there.)

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

## UI design requirements

- Use an aggressive Material 3 Expressive style consistently across all pages, especially
  `NowPlayingActivity`.
- Do not anchor persistent guidance to specific commit hashes or repository history; this
  repository may be migrated.
- Use `MaterialTheme.colorScheme.surface` for page bases and `surfaceContainerHigh` /
  `surfaceContainerHighest` for tonal panels and list rows.
- Media/profile screens should use gradient hero areas, oversized rounded artwork/avatar
  treatment, bold headline typography, and image-derived or color-scheme-driven accents.
- Use prominent primary pill actions and filled/tonal icon buttons for playback, search,
  download, queue, comments, and settings actions.
- Lists should use the connected-list shape language: large outer corners, tight inner corners,
  and small vertical gaps. Reuse `ui/component/Expressive.kt` (`connectedListItemShape`).
- Avoid old isolated elevated `Card` rows for ordinary lists. Prefer clickable `Surface` with
  Material 3 container roles unless a standalone card is actually needed.
- NowPlaying: hero gradient derived from album art, oversized rounded cover, bold marquee
  title, M3 Expressive shapes on all interactive elements.
- Playlist detail: album-color gradient header with prominent pill "Play All" button,
  connected-list song rows with download status indicators.
- User page: blurred background hero with avatar, connected-list playlists with special
  badges for "Liked Songs" and "Private Radar".
- Search: shared-element transition from the search pill to the full search bar, connected-list
  results and suggestions with album art thumbnails.
- Settings: gradient hero header, tonal surface rows with consistent shapes, pill-shaped
  action buttons.
- Mini player FAB: image-derived color with adaptive content luminance, oversized rounded
  shape, compact playback controls.

## Dependency status

All versions live in `libs.versions.toml`. **Verified against Maven Central / release notes
2026-06-11** — this repo was already on latest across the board except two libs, now bumped:
- **Coil 3.4.0 → 3.5.0** ✓ (Coil 3 splits the network fetcher into `coil-network-okhttp`).
- **OkHttp 5.3.2 → 5.4.0** ✓ (compatible with the pinned Retrofit 3.0.0, which requires OkHttp 5.x).

Confirmed already-latest and intentionally left: AGP 9.2.0, Kotlin 2.4.0, Compose BOM 2026.05.x,
media3 1.10.1 (latest), Retrofit 3.0.0 (latest). **Room stays on 2.8.x:** Room 2.x is in
maintenance mode and **Room 3.0 is a breaking, KMP-focused alpha** (`androidx.room3.*` namespace)
— do NOT adopt it here. Hilt 2.59.1 not re-verified this pass. When bumping, keep Kotlin / KSP /
Compose-compiler in lockstep — they are tightly coupled.

> Note on M3 Expressive: `MaterialExpressiveTheme`/`MotionScheme` are `internal` in material3
> 1.4.0 (the Compose-BOM-managed version here) and only become experimental-public from
> material3 1.5.0-alpha+. This app already applies an Expressive look via the public shape +
> typography scales + dynamic color (`ui/theme/`). Switching to `MaterialExpressiveTheme`
> requires moving material3 to 1.5.0-alpha+ (and managing the Compose-BOM conflict) — deferred.

**Convergence with the KMP repo (goal #4):** **Kotlin is now converged at 2.4.0** in both repos
(the KMP repo was bumped 2.3.21→2.4.0 and build-verified, 2026-06-11). **AGP differs:** this repo
is on 9.2.0, the KMP repo stays on 9.0.1 — bumping the KMP repo to AGP 9.2.0 requires Gradle ≥9.4.1
and then breaks on aapt2:9.2.0 resolution + config-cache serialization (tested), so it's deferred
there (see `../RedefineNCM_KMP/AGENTS.md`). coroutines/serialization are KMP-only (this repo uses
Gson/none), so nothing to converge there.

## Gotchas

- The project pins very recent / pre-release toolchain versions (AGP 9, Kotlin 2.4,
  Compose BOM 2026.05). Verify a matching Android Gradle/JDK before assuming a red build is
  your change.
- `usesCleartextTraffic="true"` is intentional (self-hosted HTTP API).
- DB access is via a hand-rolled `DatabaseProvider` singleton, **not** Hilt, despite the
  `hilt-android` dependency being present.
- **Shuffle regression guard:** Any change to playback state listeners must ensure the
  invariant that `playList`, `playOrderWindowIndices`, and `currentMediaIndexInList` are
  always updated together from the current Timeline via `rebuildPlaylistFromTimeline()`.
  The KMP repo's `PlayQueueTest` is the regression suite for this behavior
  (see `../RedefineNCM_KMP/AGENTS.md`).
