# AIChat

A Gemini / ChatGPT–style AI chat app for Android, written in Kotlin with Jetpack Compose.

> **Status:** Scaffold in progress. See `AGENTS.md` and the implementation plan in the commit log for the full roadmap.

## Tech stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose (BOM 2024.12.01) + Material 3 + Material 3 Adaptive
- **Architecture:** MVVM + Repository, unidirectional state flow
- **DI:** Hilt 2.52
- **Async:** Coroutines 1.9 + Flow
- **Networking:** Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization
- **Local storage:** Room 2.6.1 + DataStore 1.1
- **Navigation:** androidx.navigation.compose 2.8 (type-safe routes)
- **Cloud sync:** Firebase Firestore *(deferred — local-only for now)*
- **Testing:** JUnit 5 + Turbine + MockK + Truth + Robolectric

## Modules

- `:app` — Activity, Application, navigation host, all Compose screens
- `:core` — data layer (Room, DataStore, repositories), network, AI client, voice, location, shared theming

## Build

```bash
# from the project root
./gradlew :app:assembleDebug
```

The wrapper is included (Gradle 8.10.2). Set `local.properties#sdk.dir` to your local Android SDK.

## Branches

| Branch | Purpose |
|---|---|
| `master` | Releases |
| `develop` | Integration |
| `feature/<name>` | Features (scaffold, chat, voice, settings, sync, …) |
| `fix/<short-desc>` | Bug fixes |
| `release/<version>` | Release prep |

We use **Conventional Commits** (`feat:`, `fix:`, `chore:`, `refactor:`, `test:`, `docs:`) and commit frequently.

## Step-by-step roadmap

See `AGENTS.md` and the original plan for the full deliverable list. The high-level sequence is:

1. **Scaffold** ← you are here
2. Data layer (Room + DataStore + repositories)
3. Settings: theme + model config CRUD
4. Network + AI client (Retrofit, SSE, providers)
5. Chat MVP (send → stream → render)
6. Thinking + sources + markdown
7. Attachments (image camera/gallery)
8. Voice: STT + TTS + voice overlay + barge-in
9. Tools + ActionDispatcher (URL/maps/web search/location)
10. Agent runtime (multi-step)
11. Location provider
12. Sidebar + history
13. Skills + Agents CRUD UI
14. Responsive + polish
15. Test pass
16. Release hygiene
17. Firestore sync (deferred)

## License

See [`LICENSE`](LICENSE).
