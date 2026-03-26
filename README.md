# ⚡ Flux — Distributed Scalable Feed Engine

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

> A full-stack engineering case study demonstrating a scalable, distributed news feed system — native Android client built with Jetpack Compose and a backend engineered with Kotlin + Spring Boot, strictly adhering to Domain-Driven Design (DDD) principles.

---

## 🚧 Problem Statement

Modern social news feeds face significant challenges with high-frequency read/write ratios, eventual consistency, and media optimization. Monolithic architectures often bottleneck under these conditions.

**Flux** addresses these by implementing:

| Challenge | Solution |
|---|---|
| Scalability bottlenecks | Isolated bounded contexts (Auth, Feed, Interaction, Relationship) |
| Network retry failures | Idempotent UPSERT logic for high-frequency actions |
| Heavy media delivery | Cloudinary CDN with on-the-fly thumbnail generation |
| Network volatility on client | Cache-Then-Network strategy via Room Database |

---

## 📐 Architecture Overview

```mermaid
graph TD
    %% ── Android Client ──────────────────────────────
    subgraph Android ["📱 Android Client"]
        direction TB
        UI["🖼️ Jetpack Compose UI\n<i>Material 3 · Neomorphic Theme</i>"]
        VM["⚙️ ViewModels\n<i>MVI/MVVM · StateFlow</i>"]
        Repo["📦 Data Repositories\n<i>Single Source of Truth</i>"]
        Room[("💾 Room SQLite\n<i>Offline SSOT</i>")]

        UI  -- "StateFlow (immutable)" --> VM
        VM  -- "AppResult<T>"          --> Repo
        Repo <-->                          Room
    end

    %% ── Edge / External ─────────────────────────────
    CDN["☁️ Cloudinary CDN\n<i>On-the-fly thumbnails</i>"]

    %% ── Spring Boot Backend ──────────────────────────
    subgraph Backend ["⚙️ Spring Boot Backend  —  DDD Monorepo"]
        direction TB
        API["🔀 API Gateway\n<i>REST Controllers</i>"]

        subgraph Contexts ["Bounded Contexts"]
            direction LR
            Auth["🔐 Auth\n<i>JWT · Firebase</i>"]
            Feed["📰 Feed\n<i>Chronological Engine</i>"]
            Interact["❤️ Interaction\n<i>Like · Bookmark</i>"]
            Relate["🤝 Relationship\n<i>Follow · Unfollow</i>"]
        end

        PG[("🐘 PostgreSQL\n<i>Primary Data Store</i>")]
        Redis[("⚡ Redis\n<i>Feed Cache — Planned</i>")]

        API --> Auth & Feed & Interact & Relate
        Auth & Feed & Interact & Relate --> PG
        Feed -.->|"future"| Redis
    end

    %% ── Cross-cutting connections ────────────────────
    Repo  -- "REST / JSON"  --> API
    Repo  -- "Fetch Media"  --> CDN

    %% ── Styles ───────────────────────────────────────
    classDef client   fill:#1a1a2e,stroke:#ffd166,color:#ffd166
    classDef backend  fill:#1a1a2e,stroke:#3b9eff,color:#3b9eff
    classDef auth     fill:#1a1a2e,stroke:#ff6b6b,color:#ff6b6b
    classDef feed     fill:#1a1a2e,stroke:#3b9eff,color:#3b9eff
    classDef interact fill:#1a1a2e,stroke:#7c5cfc,color:#7c5cfc
    classDef rel      fill:#1a1a2e,stroke:#00e5b0,color:#00e5b0
    classDef infra    fill:#1a1a2e,stroke:#ff9f43,color:#ff9f43
    classDef external fill:#1a1a2e,stroke:#888,   color:#aaa

    class UI,VM,Repo,Room client
    class API backend
    class Auth auth
    class Feed feed
    class Interact interact
    class Relate rel
    class PG,Redis infra
    class CDN,FCM external
```

---

## 🗂️ Module Structure

### Android Client
```
feature/
├── auth/               # Login · SignUp · JWT token management
│   ├── data/           #   remote (AuthApi, DTOs) · local (UserDao, UserEntity)
│   ├── domain/         #   User model · AuthRepository interface
│   └── presentation/   #   AuthViewModel · LoginScreen · SignUpScreen
│
├── feed/               # Post listing · FeedViewModel
│   ├── data/           #   FeedApi · PostDto · FeedRepositoryImpl
│   ├── domain/         #   Post model · FeedRepository interface
│   └── presentation/   #   FeedScreen · FeedViewModel
│
├── interaction/        # Like · Bookmark
│   ├── data/           #   InteractionApi · InteractionDao
│   ├── domain/         #   InteractionRepository interface
│   └── presentation/   #   InteractionBar component
│
└── relationship/       # Follow · Unfollow · Connections
    ├── data/           #   RelationshipApi · FollowDao · FollowWorker (WorkManager)
    ├── domain/         #   RelationshipUser · ProfileStats models
    └── presentation/   #   ProfileScreen · ConnectionScreen · ProfileViewModel

core/
├── database/           # FluxDatabase · Converters
├── datastore/          # TokenManager (DataStore)
├── di/                 # AppModule · NetworkModule (Hilt)
├── navigation/         # Routes
├── network/            # AuthInterceptor · Result sealed class
└── ui/theme/           # Color · Type · Theme (Cosmic Ambient)
```

### Spring Boot Backend
```
server/
├── auth/               # AuthController · AuthService · JWT filter
├── feed/               # FeedController · FeedService · Post + PostAttachment
├── interaction/        # InteractionController · InteractionService · InteractionHelper
├── relationship/       # RelationshipController · FollowService · Follows model
└── config/             # SecurityConfig · CloudinaryConfig · JwtAuthFilter
```

---

## 🛠 Tech Stack

### 📱 Android Client

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose · Material 3 · Custom Neomorphic Theme |
| Architecture | Clean Architecture · MVI/MVVM · SSOT |
| Concurrency | Kotlin Coroutines · Flow |
| Local DB | Room Database |
| Networking | Retrofit · OkHttp |
| Background | WorkManager (pending follow sync) |
| DI | Hilt |

### ⚙️ Backend

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Framework | Spring Boot |
| Architecture | Domain-Driven Design (DDD) — 4 bounded contexts |
| Database | PostgreSQL |
| Media | Cloudinary (upload pipeline + CDN transforms) |
| Auth / Push | Firebase Auth · Firebase Cloud Messaging (FCM) |
| Cache | Redis *(planned)* |

---

## 📱 Mobile System Design Highlights

Inspired by **Manuel Vicente Vivo's "Mobile System Design"**:

**🔄 Offline Sync & SSOT**
The `ProfileRepository` enforces a strict **Cache-Then-Network** strategy. Room DB serves as the single source of truth — the UI renders instantly from cache while a background sync refreshes stale data silently.

**➡️ Unidirectional Data Flow (UDF)**
UI components are fully stateless. All events flow through ViewModels, which expose immutable `StateFlow` streams. This guarantees consistent UI states even during complex async retries.

**⚡ Idempotent Client-Server Communication**
Follow/Unfollow API requests are structured to be safely retried without causing duplicate backend states. Offline actions are queued via `WorkManager` (`FollowWorker`) and synced when connectivity resumes.

---

## 📊 Project Status

### ✅ Completed

- [x] **Core DDD Architecture** — Monorepo with isolated modules (`auth`, `feed`, `interaction`, `relationship`)
- [x] **Client Theming** — "Cosmic Ambient" custom design system using Material 3
- [x] **Relationship Module** — Follow/Unfollow with backend idempotency + WorkManager sync
- [x] **Offline-First Profile** — `ProfileRepository` with Room DB caching + network fallback
- [x] **Cloudinary Integration** — Backend media upload pipeline + edge thumbnail generation
- [x] **UI Screens** — Login, Dynamic Profile (View/Edit modes), Connections List

### ⏳ Pending

- [ ] **Feed Home Page UI** — "Fluid Timeline" with asymmetric visual clusters for the main feed
- [ ] **Interaction Wiring** — Connect Like/Bookmark from UI to the backend interaction module
- [ ] **Chronological Feed Engine** — SQL-based feed generation with proper DB indexing

---

## 🚀 Future Roadmap

```mermaid
timeline
    title Flux Evolution
    Phase 1 : Chronological Feed Engine
            : SQL indexing for fast retrieval
            : Interaction wiring (Like · Bookmark)
    Phase 2 : Distributed Caching
            : Redis hot-path feed cache
            : Read latency optimization
    Phase 3 : Real-Time Messaging
            : WebSockets via STOMP
            : 1-to-1 user messaging
    Phase 4 : Algorithmic Feed
            : Graph-based relevance model
            : User interaction mapping
    Phase 5 : Microservices & Streaming
            : Spring Cloud extraction
            : Apache Kafka event streaming
```

---

## 🤝 Contributing

This is an engineering case study. Issues and PRs are welcome for discussion of architectural decisions.

---

*Developed by [@neerajsahu14](https://github.com/neerajsahu14)*
