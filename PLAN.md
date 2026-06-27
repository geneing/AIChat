# AIChat — Forward Plan

This document tracks the work deliberately deferred from v0.1.0 and any
follow-up items the v0.1.0 codebase should know about.

> **Where we are**: 16 of the 17 plan steps landed. The app builds
> (debug + R8 release), 23 unit tests pass, the v0.1.0 release
> branch is on `master`. The items below are the next milestones.

---

## Table of contents

1. [Step 14 — Adaptive two-pane layout (window-size-class)](#step-14--adaptive-two-pane-layout)
2. [Step 17 — Firebase cloud sync (Firestore + Google sign-in)](#step-17--firebase-cloud-sync)
3. [Phase 18 — On-device speech-to-speech (continuous duplex voice)](#phase-18--on-device-speech-to-speech-continuous-duplex-voice)
4. [Phase 19 — Background sync + workmanager](#phase-19--background-sync--workmanager)
5. [Phase 20 — Telemetry + crash reporting](#phase-20--telemetry--crash-reporting)
6. [Phase 21 — In-app updates + dynamic feature modules](#phase-21--in-app-updates--dynamic-feature-modules)
7. [Technical-debt backlog](#technical-debt-backlog)

---

## Step 14 — Adaptive two-pane layout

**Goal**: On tablets and foldables, show the chat list and the active
chat side-by-side instead of swapping them through the drawer.

**Build flag**: `feature/adaptive-layout`

### Files to add

- `app/src/main/java/com/eugene/aichat/ui/responsive/WindowSizeClassExt.kt` —
  helper that wraps `androidx.window.core.layout.WindowSizeClass` into
  the chat app's own `WindowSize` enum (`Compact`, `Medium`, `Expanded`).
- `app/src/main/java/com/eugene/aichat/ui/responsive/AdaptiveLayouts.kt` —
  `NavigationSuiteScaffold` host and `ListDetailPaneScaffold` (from
  `androidx.compose.material3.adaptive.layout`) for `Expanded` widths.
- `app/src/main/java/com/eugene/aichat/ui/responsive/PaneScaffold.kt` —
  chooses between `BottomBar` / `NavRail` / `NavigationDrawer` based on
  width class, mirroring Material 3 adaptive guidance.

### Files to modify

- `MainActivity.kt` — wrap `RootContent` in a `WindowSizeClass` provider
  using `calculateWindowSizeClass(activity)`.
- `SidePanel.kt` — accept a `WindowSize` parameter; in `Expanded` width
  render as a permanent navigation rail instead of drawer content.
- `ChatScreen.kt` — when `WindowSize == Expanded`, place the active
  conversation beside the `SidePanel` (no back stack management needed;
  the side panel just stays visible).
- `AppNavHost.kt` — switch to a `ListDetailPaneScaffold` with
  `listPane = SidePanel` and `detailPane = ChatScreen`; respect
  `WindowSize`.

### Dependencies (already in `libs.versions.toml`)

- `androidx.compose.material3.adaptive:adaptive`
- `androidx.compose.material3.adaptive:adaptive-layout`
- `androidx.window:window` (add to catalog if missing)

### Tests

- `WindowSizeClassExtTest`: bucket-edge cases.
- Compose screenshot test for `ListDetailPaneScaffold` on a 800dp width.

### Acceptance

- Phone (compact): existing drawer behaviour unchanged.
- 7-inch tablet (medium): side rail visible, chat fills the rest.
- 10-inch tablet / foldable (expanded): list + detail side-by-side.
- Back button on `Expanded` width closes the detail pane when the list
  was entered first.

---

## Step 17 — Firebase cloud sync

**Goal**: Sign in with Google; chat history, skills, agents, and
custom model configs sync across devices. Offline-first — local
writes always succeed; Firestore catches up when the network is up.

**Build flag**: `feature/firestore-sync`

### Phase 17a — Auth + sign-in

**Files**

- `app/src/main/java/com/eugene/aichat/ui/auth/SignInScreen.kt`
- `app/src/main/java/com/eugene/aichat/ui/auth/SignInViewModel.kt`
- `app/build.gradle.kts` — apply `com.google.gms.google-services` plugin,
  add `firebase-auth-ktx`, `play-services-auth`.
- `app/src/main/AndroidManifest.xml` — register the Credential Manager
  activity if needed; add a `network_security_config.xml` allowing
  cleartext to `*.googleapis.com` only on debug.

**Flow**

1. App opens; if `FirebaseAuth.currentUser == null`, show the
   `SignInScreen` (Credential Manager one-tap, plus "Continue without
   sign-in" for local-only use).
2. On success, the `SignInViewModel` writes the user's UID into
   `UserPreferences` and navigates to `HomeRoute`.
3. Tokens are refreshed automatically by the Firebase SDK; no
   application-level handling.

**Schema** (top-level `/users/{uid}/...`)

- `users/{uid}/profile` — display name, photo URL, default model
- `users/{uid}/chats/{chatId}` — chat metadata
- `users/{uid}/chats/{chatId}/messages/{messageId}` — message body,
  role, timestamps
- `users/{uid}/chats/{chatId}/messages/{messageId}/sources/{sourceId}`
  — citation rows
- `users/{uid}/skills/{skillId}` — user-authored skills
- `users/{uid}/agents/{agentId}` — user-authored agents
- `users/{uid}/modelConfigs/{modelId}` — custom model configs
  (API key **never** leaves the device)

### Phase 17b — Sync engine

**Files**

- `core/src/main/java/com/eugene/aichat/core/data/sync/SyncEngine.kt` —
  pulls all `pendingSync = true` rows from Room and pushes them to
  Firestore with `set(..., { merge: true })`; pulls remote changes
  and writes them locally with `last-write-wins` on `updatedAt`.
- `core/src/main/java/com/eugene/aichat/core/data/sync/SyncStatus.kt`
- `core/src/main/java/com/eugene/aichat/core/data/sync/SyncWorker.kt`
- `core/src/main/java/com/eugene/aichat/core/di/CoreSyncModule.kt` —
  provides the Firestore `FirebaseFirestore` instance, the engine,
  and the worker.

**Conflict resolution**

- **Chats / messages / sources / skills / agents**: `last-write-wins`
  on `updatedAt`. Built-in rows (`isBuiltIn == true`) are never
  written from the client.
- **Model configs**: API key column is excluded. Only `displayName`,
  `baseUrl`, `model`, `temperature`, etc. are synced.

**Triggers**

- Local insert/update marks `pendingSync = true` (already wired in
  Room entities).
- A `Flow<Entity>` observer in `SyncEngine` calls
  `requestSync()` on the `WorkManager` one-off worker.
- Worker grabs all `pendingSync = true` rows in dependency order
  (chats → messages → sources/skills/agents).
- After a successful push, the rows are updated with
  `pendingSync = false`.

**Pull**

- On sign-in and on app foreground, fetch a snapshot of the
  `/users/{uid}` subtree and replay it through the same
  conflict-resolution function.
- Local edits made while offline are reconciled by the same
  push worker on reconnect.

### Phase 17c — Privacy & safety

- **API keys never leave the device.** The `ModelConfigEntity`
  push path filters `apiKeyEncrypted` to `null`; the `EncryptedKeyStore`
  already keeps the real key on-device.
- `google-services.json` stays out of VCS (already in `.gitignore`).
- Add a `Clear local data` action in Settings that wipes the Room
  database and the encrypted key store.

### Tests

- `SyncEngineTest` with a fake `FirebaseFirestore` (the Firebase SDK
  is JVM-compatible, so we can use a real `LocalFirestore` if needed,
  or wrap the persistence in an interface and inject a fake).
- `WorkerTest` using `androidx.work:work-testing`.

### Acceptance

- Local writes succeed offline; pending rows are visible in a
  diagnostic Settings screen.
- Sign-in on a second device brings the same chats, skills, agents,
  model configs.
- Editing on device A and then opening the app on device B shows
  the edit (within a few seconds of backgrounding device A).
- Custom API keys never appear in Firestore documents.

---

## Phase 18 — On-device speech-to-speech (continuous duplex voice)

**Goal**: Remove the cloud round-trip from the voice path. When the
user enables "on-device voice" in Settings, the audio is
transcribed, sent to the model, and the model's text is rendered
to audio entirely on-device, with no network.

**Build flag**: `feature/ondevice-sts`

### Pre-requisites

- Pick an on-device STT engine:
  - **Whisper.cpp** via the existing `KokoroTTS` project's JNI bridge,
    or
  - **Vosk** (smaller models, streaming), or
  - **Android 14+ SpeechRecognizer on-device mode** (`EXTRA_PREFER_OFFLINE = true`).
- Pick an on-device TTS: **Piper** via the same JNI bridge, or
  **Sherpa-onnx**.

### Files

- `core/src/main/java/com/eugene\aichat\core\voice\ondevice/OnDeviceStt.kt`
  — wraps the chosen STT engine behind the existing `SttEngine`
  interface.
- `core/src/main/java/com/eugene\aichat\core\voice\ondevice/OnDeviceTts.kt`
  — same for TTS.
- `core/src/main/java/com/eugene\aichat\core\di/CoreVoiceModule.kt` —
  add a `@Provides @Named("onDevice")` and let the ViewModel
  pick the right one based on a new `UserPreferences.useOnDeviceVoice`.

### Build system

- A new `:native` Gradle module that produces the JNI `.so` files
  for arm64-v8a / armeabi-v7a / x86_64, with `externalNativeBuild {
  cmake { path = file("src/main/cpp/CMakeLists.txt") } }`.
- Add an opt-in flag in `app/build.gradle.kts` to skip the native
  build on CI.

### Acceptance

- Same latency budget as the cloud path on a Pixel 8.
- Works on Airplane mode after the models are downloaded once.

---

## Phase 19 — Background sync + WorkManager

**Goal**: Push pending changes to Firestore even when the app is in
the background; warm up the AI client on schedule so the first
message after a cold start is snappy.

**Build flag**: `feature/background-sync`

### Files

- `core/src/main/java/com/eugene\aichat\core\data\sync\SyncPeriodicWorker.kt`
  — periodic worker scheduled every 15 minutes via
  `PeriodicWorkRequestBuilder<>`.
- `core/src/main/java/com/eugene\aichat\core\data\sync\SyncConstraints.kt`
  — `NetworkType.UNMETERED`, `requiresBatteryNotLow(false)`.
- `core/src/main/java/com/eugene\aichat\core\data\sync\WarmupWorker.kt`
  — pre-warms the OkHttp connection pool and warms the
  `ProviderRegistry`'s `modelList` query (used to populate the
  model dropdown in the chat top bar).

### Manifest

- `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>`
  (optional; re-schedule the periodic worker after boot).

### Acceptance

- Edits made in another device show up within ~15 minutes of
  app being backgrounded on this device.
- Battery cost < 1%/day from the periodic worker.

---

## Phase 20 — Telemetry + crash reporting

**Goal**: Capture anonymous usage signals (which providers users
configure, which features they use) and wire crash reporting.

**Build flag**: `feature/telemetry`

### Decision: roll-our-own, not Firebase Crashlytics

Firebase adds a build-time dependency on `google-services`. To keep
the v0.1.0 build hermetic, roll a small Sentry-compatible client
that POSTs events to a self-hosted collector (Sentry / GlitchTip /
highlight.io). If you'd rather not host, swap in the official Sentry
Android SDK later.

### Files

- `core/src/main/java/com/eugene\aichat\core\observability/Event.kt`
- `core/src/main/java/com\eugene\aichat\core\observability\Client.kt`
- `core/src/main/java/com\eugene\aichat\core\observability/CrashHandler.kt`
  — `Thread.setDefaultUncaughtExceptionHandler` that flushes the
  queue and rethrows.
- `core/src/main/java/com\eugene\aichat\core\observability/EventBuffer.kt`
  — bounded in-memory queue with disk-spill for the next launch.

### Hooks

- `AiClientImpl` — log a `stream.failed` event with the provider and
  HTTP status (but never the API key or message body).
- `ChatViewModel` — `chat.sent` / `chat.streamed` counters.

### Acceptance

- An uncaught exception in the UI thread is reported within 5
  seconds; the user sees a non-blocking toast and the app is
  restarted cleanly.

---

## Phase 21 — In-app updates + dynamic feature modules

**Goal**: Ship new agent types / providers as on-demand modules so
the base APK stays small and updates can ship without Play Store
reviews.

**Build flag**: `feature/dynamic-modules`

### Plan

1. Split `:core` into `:core` (essential) and `:feature-agents` /
   `:feature-voice` modules with `distributeAs="on-demand"` in
   `app/build.gradle.kts`.
2. Wire the `SplitInstallManager` so the first time the user opens
   the "Agents" tab, the module is requested with
   `SplitInstallRequest`.
3. While the module downloads, show a skeleton with a progress
   indicator.

### Acceptance

- Base APK < 1.5 MB.
- Agent module installs in < 2 s on a typical connection.

---

## Technical-debt backlog

| Item | Where | Notes |
|---|---|---|
| **Replace `.first()` on the chat history Flow in `ContextAssembler.build`** | `core/.../ai/request/ContextAssembler.kt` | The current `.first()` is fine for short chats but will block on a busy DB. Add a one-shot suspend function on `ChatRepository` that returns a `List<Message>` for a given `chatId`. |
| **Hard-coded string "AIChat" in `TopAppBar` title fallback** | `app/.../ui/chat/ChatScreen.kt` | Move to `strings.xml` (`R.string.app_brand_short`). |
| **Drop empty `MessageContent`/`Converters` stub** | `core/.../data/db/Converters.kt` | Either re-introduce converters for `List<String>` (and add `@TypeConverters` to `AppDatabase`) or delete the file. |
| **Surface `OpenUrlTool` URL safety in UI** | `core/.../ai/tools/OpenUrlTool.kt` | Today it launches any URL. Add a user-prompt step for unknown hosts (e.g., `localhost`, `192.168.x.x`) to avoid accidental SSRF in dev builds. |
| **Replace `LocationManager` fallback in `LocationProvider` with `FusedLocationProviderClient` everywhere** | `core/.../ai/location/LocationProvider.kt` | Already migrated; verify the `LocationServices` is initialized lazily without crashing on emulators without Play Services. |
| **Add `Json` to the Hilt graph for ad-hoc serialization** | `core/.../di/CoreDataModule.kt` | Used internally by `AppJson`; expose for testability of tools that round-trip JSON. |
| **Add a `clear()` action to `EncryptedKeyStore` for sign-out** | `core/.../security/EncryptedKeyStore.kt` | Wipe all entries; tests should cover that re-creating a key regenerates the row. |
| **Add CI (GitHub Actions)** | `.github/workflows/ci.yml` | Runs `:app:assembleDebug` and `:core:testDebugUnitTest` on every push. |
| **Migrate to KMP-friendly module structure** | `:core` | The `:core` module is already pure JVM/Kotlin (only the voice and location code touches Android APIs). Splitting it into a `commonMain` KMP module would let us share types with an iOS or desktop build later. |

---

## Implementation order

1. **Phase 20 (telemetry)** — quick win, no UI changes; gives us crash visibility during the rest of the work.
2. **Step 14 (adaptive layout)** — unblocks the tablet UX without changing any data flow.
3. **Phase 19 (background sync)** — needed before step 17 ships, so sync feels instant.
4. **Step 17 (Firestore sync)** — the biggest item. Land in three sub-phases (auth → engine → privacy) and gate the privacy step behind a feature flag.
5. **Phase 21 (dynamic modules)** — the app will be feature-rich enough by then to justify splitting.
6. **Phase 18 (on-device STS)** — biggest native-code lift; schedule for after the cloud path is stable.

Estimated total: 8–12 dev-days.
