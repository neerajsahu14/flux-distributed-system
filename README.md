# ⚡ Flux — Distributed Scalable Feed Engine

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

> A full-stack engineering case study directly applying the core architectural principles from [@manuelvicnt](https://github.com/manuelvicnt) **Manuel Vicente Vivo's "Mobile System Design Interview"**. This project demonstrates how to build a scalable, distributed news feed system by architecting around strict mobile constraints—unstable networks, battery preservation, and complex state management.

---

## 📸 App Screenshots

> **Cosmic Ambient** design system — Material 3 with custom neomorphic & glassmorphic theming

<table>
  <tr>
    <td align="center"><b>Login</b></td>
    <td align="center"><b>Sign Up</b></td>
    <td align="center"><b>Feed</b></td>
    <td align="center"><b>Profile</b></td>
  </tr>
  <tr>
    <td><img src="screenshots/login.jpg" width="180"/></td>
    <td><img src="screenshots/signup.jpg" width="180"/></td>
    <td><img src="screenshots/feed.jpg" width="180"/></td>
    <td><img src="screenshots/profile.jpg" width="180"/></td>
  </tr>
</table>

<table>
  <tr>
    <td align="center"><b>Explore</b></td>
    <td align="center"><b>Feed Detail</b></td>
    <td align="center"><b>Create Post</b></td>
    <td align="center"><b>Create Post Preview</b></td>
  </tr>
  <tr>
    <td><img src="screenshots/explore.jpg" width="180"/></td>
    <td><img src="screenshots/feed_detail.jpg" width="180"/></td>
    <td><img src="screenshots/createpost.jpg" width="180"/></td>
    <td><img src="screenshots/create post preview.jpg" width="180"/></td>
  </tr>
</table>

---

## 📱 Architectural Philosophy: Designing for Mobile Constraints

The entire architecture of Flux is dictated by the mobile system design heuristics outlined in Vivo's foundational literature. Rather than treating the mobile app as a dumb terminal, the client and server are co-designed to handle high-latency environments gracefully.

### 1. The Cache-Then-Network Strategy (SSOT)
Mobile networks are inherently unreliable. To prevent blocking the UI thread waiting for HTTP responses, Flux utilizes a strict **Single Source of Truth (SSOT)** via Room SQLite. 
* The UI never observes the network directly. 
* Network calls update the local database. The Jetpack Compose UI observes the local database via Kotlin `Flow`. This ensures instant cold-starts and seamless offline rendering.

### 2. Unidirectional Data Flow (UDF)
State mutations are strictly controlled to prevent UI inconsistencies during async network retries. UI components are stateless, pushing intents to ViewModels. ViewModels process these through Repositories, mutating the SSOT, which then emits a new, immutable `StateFlow` back to the UI.

### 3. API Resiliency & Idempotency
High-frequency mobile actions (like tapping "Follow" or "Like" rapidly in a subway tunnel) cause network retries. The backend Interaction and Relationship modules are engineered with **Idempotent UPSERT logic**, ensuring that client-side OkHttp retries never result in duplicate database records or inflated interaction counts.

### 4. Edge Media Processing & Thread Starvation Prevention
To respect mobile bandwidth and client-side memory limits, heavy media processing is offloaded to Cloudinary's Edge CDN for on-the-fly thumbnail generation. On the backend, these network I/O calls are explicitly moved outside of the PostgreSQL `@Transactional` boundaries to prevent database connection pool exhaustion during concurrent mobile uploads.

---

## 📐 System Architecture

The system utilizes a Clean Architecture approach on the client to isolate the SSOT, and a Domain-Driven Design (DDD) approach on the backend to separate bounded contexts.

```mermaid
graph TD
    %% ── Android Client ──────────────────────────────
    subgraph Android ["📱 Android Client (SSOT & UDF)"]
        direction TB
        UI["🖼️ Jetpack Compose UI\n<i>Stateless Observers</i>"]
        VM["⚙️ ViewModels\n<i>StateFlow Emitters</i>"]
        Repo["📦 Repositories\n<i>Network/Cache Arbitrators</i>"]
        Room[("💾 Room SQLite\n<i>Single Source of Truth</i>")]

        UI  -- "Intents" --> VM
        VM  -- "StateFlow" --> UI
        VM  -- "AppResult<T>" --> Repo
        Repo <--> Room
    end

    %% ── Edge / External ─────────────────────────────
    CDN["☁️ Cloudinary CDN\n<i>Edge Optimization</i>"]

    %% ── Spring Boot Backend ──────────────────────────
    subgraph Backend ["⚙️ Spring Boot Backend — DDD Monorepo"]
        direction TB
        API["🔀 API Gateway\n<i>REST Controllers</i>"]

        subgraph Contexts ["Bounded Contexts"]
            direction LR
            Auth["🔐 Auth\n<i>Stateless JWT</i>"]
            Feed["📰 Feed\n<i>Chronological Engine</i>"]
            Interact["❤️ Interaction\n<i>Idempotent UPSERTs</i>"]
            Relate["🤝 Relationship\n<i>Idempotent Follows</i>"]
        end

        PG[("🐘 PostgreSQL\n<i>Supavisor Pooled</i>")]
        Redis[("⚡ Redis\n<i>Planned</i>")]

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
    class CDN external
```
---

## 🗄️ Database Schema

> PostgreSQL schema spanning 4 bounded contexts — Auth, Feed, Interaction, Relationship

```mermaid
erDiagram
    users {
        int8 id PK
        varchar username
        varchar email
        varchar password_hash
        text bio
        text profile_pic_url
        timestamptz created_at
        bool isvalid
    }

    posts {
        int8 id PK
        int8 user_id FK
        text content
        varchar request_id
        int4 like_count
        int4 attachment_count
        int4 share_count
        timestamptz created_at
        timestamptz updated_at
        bool isvalid
    }

    follows {
        int8 follower_id FK
        int8 followee_id FK
        varchar request_id
        timestamptz created_at
        bool isvalid
    }

    interactions {
        int8 id PK
        int8 user_id FK
        int8 post_id FK
        varchar action_type
        varchar request_id
        timestamptz created_at
        bool isvalid
    }

    post_attachments {
        int8 id PK
        int8 post_id FK
        varchar media_type
        text content_url
        text thumbnail_url
        text caption
        int4 display_order
        bool isvalid
    }

    users ||--o{ posts : "writes"
    users ||--o{ follows : "follower_id"
    users ||--o{ follows : "followee_id"
    users ||--o{ interactions : "performs"
    posts ||--o{ interactions : "receives"
    posts ||--o{ post_attachments : "has"
```

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

## 📊 Project Status

### Complited

- [x] **Core DDD Architecture** — Monorepo with isolated modules (`auth`, `feed`, `interaction`, `relationship`)
- [x] **Client Theming** — "Cosmic Ambient" custom design system using Material 3
- [x] **Relationship Module** — Follow/Unfollow with backend idempotency + WorkManager sync
- [x] **Offline-First Profile** — `ProfileRepository` with Room DB caching + network fallback
- [x] **Cloudinary Integration** — Backend media upload pipeline + edge thumbnail generation
- [x] **UI Screens** — Login, Dynamic Profile (View/Edit modes), Connections List
- [x] **Feed Home Page UI** — "Fluid Timeline" with asymmetric visual clusters for the main feed
- [x] **Interaction Wiring** — Connect Like/Bookmark from UI to the backend interaction module
- [x] **Chronological Feed Engine** — SQL-based feed generation with proper DB indexing

### Remaining
- [ ] **XML Based caption** - Editing and preview
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

This is an engineering case study focused on applying mobile system design concepts. Issues and PRs are welcome for discussion of architectural decisions, scaling strategies, and optimizations.

---

*Developed by [@neerajsahu14](https://github.com/neerajsahu14)*
