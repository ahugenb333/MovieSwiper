# MovieSwiper

Swipe through movies like a dating app, build a taste profile, and get personalized picks.

MovieSwiper is a Kotlin Multiplatform app (Android + iOS) with a small Ktor backend. The mobile app talks to your local server; the server talks to [TMDB](https://www.themoviedb.org/) for movie data and runs the recommendation logic.

## What it does

Three tabs at the bottom of the app:

| Tab | What it is |
|-----|------------|
| **Matcher** | Tinder-style swipe deck. Swipe right to save a movie, left to pass. Your likes and dislikes feed your taste profile. |
| **Finder** | Up to 20 yes/no questions (genres, decades, etc.) that narrow a pool of candidates. You get a ranked list of matches at the end. |
| **Watchlist** | Movies you swiped right on. |

### How recommendations work

- **Matcher** fetches movies from TMDB using filters based on your taste (liked genres, avoided genres, quality thresholds), then re-ranks them on the server.
- **Finder** starts with a broad pool of well-rated movies, asks adaptive questions to split that pool, and returns your top picks when done.
- Swipes in Matcher sync to the backend so future sessions get smarter over time.

## Prerequisites

- **JDK 21** (required for the server)
- **Android Studio** with an emulator, or a physical device
- A **TMDB API key** (read token from [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api))

## Quick start

### 1. Configure TMDB

Add your key to `local.properties` (this file is gitignored):

```properties
tmdb.api.key=your_tmdb_read_access_token_here
```

If the build still can't find it, set it in `data/api/build.gradle.kts` under the `buildkonfig` block.

### 2. Start the backend

The server runs on **port 8081** on purpose — port 8080 is commonly taken by Jenkins or other tools on macOS.

```bash
./gradlew :server:run
```

You should see:

```
Starting MovieSwiper Backend on http://0.0.0.0:8081 (Android emulator: http://10.0.2.2:8081)
```

**Port already in use?** Stop the old process and try again:

```bash
kill $(lsof -ti :8081)
./gradlew :server:run
```

The server logs each request, e.g. `GET /matcher/discover?... -> 200 OK`.

### 3. Run the Android app

From Android Studio, run the `androidApp` configuration on an emulator.

Or from the command line:

```bash
./gradlew :androidApp:installDebug
```

The app is preconfigured to reach the server at `http://10.0.2.2:8081` (the emulator's alias for your machine's localhost).

**Tip:** Start the server first, then launch the app. If Matcher shows an HTTP error, the backend probably isn't running.

### 4. iOS (optional)

Open `iosApp/` in Xcode and run from there. You'll need to point the backend URL at your machine's IP instead of `10.0.2.2`.

## Project layout

```
MovieSwiper/
├── androidApp/          Android entry point
├── iosApp/              iOS entry point
├── shared/              App shell, navigation, DI
├── server/              Ktor backend (port 8081)
├── feature/
│   ├── matcher/         Swipe UI
│   ├── questions/       Finder Q&A flow
│   └── browser/         Watchlist
├── data/
│   ├── api/             TMDB + backend HTTP clients
│   └── localdb/         On-device cache (SQLDelight)
└── core/
    ├── models/          Shared data types
    └── logic/           Scoring, discover queries, question selection
```

## API overview

| Endpoint | Purpose |
|----------|---------|
| `GET /matcher/discover?userId=&page=` | Personalized movie deck |
| `POST /profile/sync` | Sync swipe interactions |
| `POST /finder/next-question` | Next Finder question or final recommendations |

## Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| `403` + HTML in the app | Something else (often Jenkins) is on port 8080. Use 8081 and restart the server. |
| `Address already in use` | A previous server instance is still running. `kill $(lsof -ti :8081)` |
| `Pinning is deprecated since Android Q` in logcat | Harmless Android system noise from a dependency. Safe to ignore. |
| Empty Matcher / connection errors | Server not running, or app not rebuilt after URL changes |

## Tech stack

Kotlin Multiplatform · Compose Multiplatform · Decompose · Ktor · Koin · SQLDelight · Coil · TMDB API
