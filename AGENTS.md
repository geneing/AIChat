Plan a new Android app called "AIChat". Do NOT write files yet.

Requirements:
- Language: Kotlin, UI: Jetpack Compose
- minSdk 24, targetSdk 35, AGP latest stable
- Architecture: MVVM + Repository, unidirectional state flow
- DI: Hilt, Async: Coroutines + Flow
- Networking: Retrofit + OkHttp + kotlinx.serialization
- Local storage: Room
- Cloud synchronization: Firebase Firestore, offline-first
- Navigation: androidx.navigation.compose
- Testing: JUnit5 + Turbine + MockK
- GIT: GitHub repository git@github.com:geneing/AIChat.git 
- GIT: use branches and worktree for AI coding tasks, features, bugfixes, and releases. Commit frequently with clear descriptions of changes.
 

App Requirements:
1. User wants an AI chat experience with a clean and responsive UI. Similar to Gemini App, ChatGPT App.
2. Chat screen 
   3. Main Screen ![Main Screen](./Main%20Screen.png "Main Screen") with:
      1. Top bar with app name and settings icon
      2. Chat dialog with requests and responses (RecyclerView/Compose LazyColumn)
      3. Chat dialog should support text, images, source references (through clickable hyperlinks). 
      4. Chat dialog should compress thinking responses and show an indicator that allows to expand thinking text.
      3. Input field with send button and attachment options (text, image, voice) and voice mode button
      4. Support for text, image, and voice messages
      5. Support for AI model selection from the configured list (OpenRouter, OpenCode, OpenAI)
      6. Support for seamless voice dialog with speech-to-text and text-to-speech capabilities, detection of user interruptions, and real-time streaming of AI responses.
      7. UI should support phone and tablet layouts, with responsive design for different screen sizes and orientations.
      8. UI should support dark and light themes, with the ability to switch between them in settings, or follow system mode.
4. Sidebar ![Sidebar](Side%20Panel.png)  with options for:
   1. Chat history
   2. Settings
3. Settings
   1. Google account sign-in
   2. Theme 
   2. AI model configuration (url, api key). Support OpenRouter, OpenCode, OpenAI for now.
   3. System prompt
   4. Skills 
   5. Agents 
4. AI model interaction backend
   5. Should support thinking and agentic behavior, with the ability to handle multiple agents and skills.
   6. Should support web searching as required by the agents or models.
   7. Should support passing location information and also local information to the AI model for location-based responses.
   8. Should support streaming responses from the AI model, with the ability to handle interruptions and provide real-time feedback to the user.
   9. Should support voice input and output, with the ability to handle interruptions and provide real-time feedback to the user.
   10. Should be able to start android actions based on the AI model's responses, such as opening a web page, starting maps with specific driving directions, etc.

Deliverables in the plan:
- Full package/folder layout under app/src/main/java
- Gradle module + version-catalog entries
- AndroidManifest entries (Application, Activity, permissions, intent filters)
- Hilt graph (modules + @Inject points)
- Navigation graph
- Data layer schema (Room entities, DAOs, API DTOs)
- Agent and skill configuration schema
- AI model interaction flow (how to handle requests, responses, streaming, interruptions)
- UI component designs (Jetpack Compose)
- Implementation order (what to scaffold first so it builds end-to-end earliest)