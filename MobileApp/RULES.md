# SafeCall — Engineering Rules

These rules govern every change to this codebase. They keep the project aligned with **Clean Architecture**, **Clean Code**, **SOLID**, and **DRY**. If a rule and a shortcut conflict, the rule wins.

---

## 1. Architecture rules (Clean Architecture)

### 1.1 Dependency direction is one-way
- `presentation` → `domain`
- `data` → `domain`
- `service` → `domain`
- `domain` depends on **nothing inside the app** (only Kotlin stdlib + coroutines).

> If you ever need `import android.*` inside `domain/`, you are doing it wrong. Move the Android-specific bit out to `service/` or `data/` and define a port (interface) in `domain/repository/`.

### 1.2 Layer responsibilities are non-negotiable
| Layer | Allowed | Forbidden |
|---|---|---|
| `domain/` | Pure Kotlin: models, ports (interfaces), use cases | Android, Ktor, Koin, Compose, Room, any framework |
| `data/` | Implementations of domain ports, DTOs, mappers, HTTP, DataStore | UI code, Android services, business decisions |
| `service/` | Android `Service` / `BroadcastReceiver` / system APIs, calling use cases | Business decisions, HTTP, parsing |
| `presentation/` | Compose UI, ViewModels, navigation | HTTP, parsing, business decisions |
| `core/` | Cross-cutting helpers (dispatchers, logger, result type, config) | Anything domain-specific |
| `di/` | Wiring only | Logic of any kind |

### 1.3 Business logic lives in `domain/usecase/`
- A use case = **one operator `invoke`** that returns a domain type (or `AppResult<T>`).
- If a service or a ViewModel contains an `if/when` that decides *what the app does* (vs. *how it talks to the OS*), that branch belongs in a use case.

### 1.4 Cross a layer only through a port
- Service code calls **use cases**, not repositories directly.
- Use cases call **ports** (interfaces in `domain/repository/`), never concrete classes.
- Concrete implementations are bound in `di/` — nowhere else.

---

## 2. SOLID rules

### 2.1 Single Responsibility (SRP)
- One class = one reason to change.
- A file with two unrelated classes is a smell — split it.
- Services already split: `ScamScreeningService` decides allow/reject; `AudioCaptureService` captures; `OverlayAlertService` alerts. Don't merge them.

### 2.2 Open/Closed (OCP)
- Add a new transcription provider? Implement `SpeechTranscriber`, bind it in `dataModule`. Do not edit existing implementations.
- Add a new verdict surface (e.g. SMS notification)? Implement `AlertNotifier`. Do not edit `RaiseScamAlertUseCase`.

### 2.3 Liskov Substitution (LSP)
- Any implementation of a port must honour the port's contract — same return semantics, same exception/`AppResult` behaviour.
- If your impl needs an extra method to be useful, the **port** is wrong; fix the port, not the caller.

### 2.4 Interface Segregation (ISP)
- Ports stay narrow. `SpeechTranscriber.transcribe(chunk): TranscriptChunk` — that's it. No "kitchen-sink" interfaces.
- If a consumer only needs half the methods, split the interface.

### 2.5 Dependency Inversion (DIP)
- High-level code (`usecase/`, `service/`) depends on **abstractions** (`domain/repository/*`), never on `ClaudeApi`, `KtorGoogleSpeechApi`, etc.
- Constructor injection only. No `Singleton.getInstance()`, no static `Context` lookups, no `ServiceLocator` outside `di/`.

---

## 3. DRY rules

### 3.1 Reuse what exists before adding
- Logging → `core/logging/Logger`. Don't call `Log.d` directly.
- Coroutine context → `core/dispatchers/AppDispatchers`. Don't call `Dispatchers.IO` directly outside `core/`.
- Success/failure → `core/result/AppResult`. Don't invent another sealed result type.
- Audio slicing → `service/audio/AudioChunker`. Don't reimplement chunking.
- API keys / model name → `core/config/ApiKeyProvider`. Don't read `BuildConfig` outside `core/`.

### 3.2 No copy-pasted code blocks
- The third occurrence triggers extraction. (Two is a coincidence; three is a pattern.)
- Extracted helpers live in the **lowest** layer that all callers can reach.

### 3.3 One canonical source per concept
- One DTO ↔ domain mapper per type, in `data/.../dto/`.
- One Koin definition per port, in the matching `di/` module. No duplicate `single<X>` bindings.

---

## 4. Clean Code rules

### 4.1 Naming
- Use cases end in `UseCase`. Ports describe a capability (`ScamDetector`, not `ClaudeManager`). Implementations name the technology (`ClaudeScamDetector`, `KtorGoogleSpeechApi`).
- No abbreviations except universally known ones (`api`, `id`, `url`).
- Booleans read as predicates: `isBlocked`, `hasOverlay`, `canDrawOverlays`.

### 4.2 Functions
- Small. If a function does not fit on a screen, it does too much.
- One level of abstraction per function. Don't mix HTTP wiring with business decisions in the same body.
- No flag-arguments (`doX(force: Boolean)`); split into two functions or two use cases.

### 4.3 Comments
- Default: write none. Make the code say it.
- Acceptable comments: a non-obvious *why*, a documented invariant, a `TODO:` with a concrete next step.
- Forbidden: comments that restate the code, `// added by X`, `// removed`, change-log noise.

### 4.4 Error handling
- Return `AppResult.Failure(AppError.*)` for expected failures (network down, auth bad, permission missing).
- Throw only for programmer errors (illegal argument, impossible state).
- Never swallow exceptions silently — at minimum, log via `Logger`.

### 4.5 Nullability
- Nullable types must mean *"this value is genuinely optional"*. Don't use `null` as a poor man's error code — use `AppResult`.

### 4.6 Immutability
- Domain models are `data class` with `val`s. No mutable domain state.
- ViewModel state is exposed as `StateFlow`, mutated only inside the ViewModel.

### 4.7 Coroutines
- Suspend functions in ports, never `Flow<X>` unless the consumer truly needs a stream.
- Every long-running scope (`Service`, `ViewModel`) owns its own `SupervisorJob` and cancels it on tear-down.
- No `GlobalScope`. Ever.

### 4.8 Threading
- Use `AppDispatchers` from DI. The IO/Default split happens at the boundary (data impls, audio capture), not inside use cases.
- Use cases must be safe to call from any dispatcher.

---

## 5. Android-specific rules

### 5.1 Services
- `Service`s are thin: receive the system callback, call a use case, forward the result. No business logic in `onScreenCall`, `onStartCommand`, etc.
- Foreground services declare their `foregroundServiceType` in both the manifest **and** the `startForeground` call.

### 5.2 Permissions
- All permission/role requests happen in `presentation/setup/`. Services never prompt.
- Code that uses a permission-gated API must assume the permission is granted; the setup flow is the gate.

### 5.3 Resources & strings
- All user-facing text in `res/values/strings.xml`. No hardcoded English in Kotlin.
- One string key per concept; reuse rather than duplicate.

### 5.4 Secrets
- Secrets live in `local.properties` (gitignored) → exposed via `BuildConfig` → read **only** through `ApiKeyProvider`.
- Never commit a real key. `local.properties.template` is the only committed file that mentions keys.

---

## 6. Testing rules

### 6.1 What to test
- **Domain use cases**: must have unit tests. Mock ports with MockK.
- **Mappers** (DTO ↔ domain): unit-tested round-trip.
- **Service / Compose**: instrumented or Compose tests if behaviour is non-trivial.

### 6.2 Test naming
- Backtick descriptive names: `` `blocked number is rejected` ``.
- One behaviour per test. If a test has two `assert`s on unrelated things, split it.

### 6.3 No shared mutable state between tests
- Each test builds its own subject + mocks. No `@BeforeClass` god-objects.

---

## 7. Dependencies & build

### 7.1 Version catalog only
- All versions/libraries go through `gradle/libs.versions.toml`. No inline `"androidx.xxx:1.2.3"` strings in `build.gradle.kts`.

### 7.2 Add a library only when it earns its place
- Justify in the PR description: what the library buys you that a few lines of code wouldn't.
- Prefer libraries already in the catalog (Ktor, Koin, kotlinx.serialization, DataStore).

### 7.3 minSdk / targetSdk
- `minSdk = 29` is load-bearing (CallScreeningService surface). Bumping or lowering requires explicit discussion.

---

## 8. Pull-request checklist

Before opening a PR, confirm:
- [ ] No new `domain/` file imports anything Android, Ktor, or Koin.
- [ ] New behaviour has a use case; business logic isn't hiding in a service or ViewModel.
- [ ] Concrete classes are bound in `di/`, not constructed inline.
- [ ] No duplicated logic — third-occurrence extracted.
- [ ] No new direct calls to `Log.*`, `Dispatchers.*`, or `BuildConfig.*` outside `core/`.
- [ ] User-facing strings are in `strings.xml`.
- [ ] Use cases have tests; new ports have at least one fake/mocked test.
- [ ] `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` is green.

---

## 9. When in doubt

> **"Where should this code go?"**
> Ask: *does this decide what the app does?* → `domain/usecase/`.
> *Does this talk to the outside world?* → `data/`.
> *Does this respond to an Android system callback?* → `service/`.
> *Does this render pixels or collect user input?* → `presentation/`.
> *Is it a helper used by all of the above?* → `core/`.

If two layers seem to fit, the **lower** layer (closer to `domain`) wins.
