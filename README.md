# Plants vs. Zombies 2 — Advanced Programming Project

<p align="center">
  <a href="https://openjdk.org/"><img src="https://img.shields.io/badge/Java-25-ED8B00?style=flat&logo=openjdk&logoColor=white" alt="Java" /></a>
  <a href="https://libgdx.com/"><img src="https://img.shields.io/badge/libGDX-1.14.2-E74C3C?style=flat" alt="libGDX" /></a>
  <a href="https://gradle.org/"><img src="https://img.shields.io/badge/Build-Gradle-02303A?style=flat&logo=gradle&logoColor=white" alt="Gradle" /></a>
  <a href="https://junit.org/junit5/"><img src="https://img.shields.io/badge/Tests-JUnit%205-25A162?style=flat&logo=junit5&logoColor=white" alt="JUnit" /></a>
  <a href="https://www.sqlite.org/"><img src="https://img.shields.io/badge/DB-SQLite-003B57?style=flat&logo=sqlite&logoColor=white" alt="SQLite" /></a>
  <a href="#architecture"><img src="https://img.shields.io/badge/Architecture-MVC-0A66C2?style=flat" alt="Architecture" /></a>
</p>

<img width="2752" height="1536" alt="group16-banner" src="https://github.com/user-attachments/assets/2f3a1eea-c378-4ea4-900c-d0236bf51388" />

**Team:** The Final Wave (Group 16)

A Java / libGDX recreation of *Plants vs. Zombies 2* for the Advanced Programming course at Sharif University of Technology. The game ships with a full adventure campaign, GUI menus, minigames, SQLite persistence, and a client-server layer for online accounts, leaderboard sync, and networked *I, Zombie* matches.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Repository Layout](#repository-layout)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Game](#running-the-game)
- [Running the Server](#running-the-server)
- [CLI Mode](#cli-mode)
- [Tests](#tests)
- [Configuration](#configuration)
- [Development Phases](#development-phases)
- [Team Members](#team-members)

---

## Overview

Players defend their lawn with plants against zombie waves across multiple chapters. The codebase separates engine logic from rendering and networking:

- **Phase 1** — core simulation, entities, and a text-based CLI
- **Phase 2** — libGDX GUI, animations, audio, menus, and minigames
- **Phase 3** — TCP server, account sync, matchmaking, and online *I, Zombie*

The GUI client is the primary way to play. The CLI entry point remains available for debugging and automated smoke tests.

---

## Features

| Area | Highlights |
|------|------------|
| **Adventure** | Chapters, level map, plant selection, boss fights, special level rules |
| **GUI** | Main menu, shop, collection, greenhouse, travel log, score game |
| **Minigames** | Vase Breaker, Walnut Bowling, Beghouled, Zombotany, I, Zombie (local + online) |
| **Combat polish** | Projectiles, explosions, plant food, lawn mowers, chapter-specific visuals |
| **Audio** | Menu / chapter BGM, gameplay SFX, zen garden music |
| **UX** | Marigold loading screen between major menu transitions, fade transitions elsewhere |
| **Persistence** | Local SQLite user progress, plants, quests, minigame records |
| **Networking** | Auth, leaderboard, progress sync, matchmaking, live match state |

---

## Architecture

Strict **Model-View-Controller** layout under `io.github.finalwave`:

```
io.github.finalwave
├── model/          Game rules, entities, adventure, quests, persistence
├── controller/     Command parsing, screen flow, gameplay orchestration
├── view/           CLI views + libGDX GUI (screens, HUD, render sync)
├── network/        Client-side networking helpers (shared with server module)
├── server/         Dedicated game server (Gradle subproject)
├── login/          Login gateway abstractions
├── registration/   Sign-up gateway abstractions
├── leaderboard/    Leaderboard gateway abstractions
└── score/          Score submission gateway abstractions
```

Game logic lives in `model/` and does not depend on libGDX. GUI code in `view/gui/` observes controllers and renders the simulation.

Gradle modules:

| Module | Role |
|--------|------|
| `:core` | Game engine, controllers, CLI + GUI code |
| `:lwjgl3` | Desktop launcher (`Lwjgl3Launcher`) |
| `:network` | Wire protocol and shared network types |
| `:server` | Standalone TCP server |

---

## Repository Layout

```
ap-project/
├── assets/
│   ├── EMOJI/           In-match emoji images (tracked in git)
│   ├── sounds/          BGM + SFX (tracked in git)
│   ├── STICKERS/        Reaction sticker atlases (tracked in git)
│   ├── ATLASES/         PVZ2 atlases (local only — not in git)
│   ├── IMAGES/          PAM animations & images (local only)
│   └── RESOURCES.json   Atlas index (local only)
├── core/                Main Java sources and tests
├── lwjgl3/              Desktop application entry point
├── network/             Network protocol + client helpers
├── server/              Game server
└── gradlew              Gradle wrapper
```

> **Note:** Most of `assets/` is gitignored because atlases and PAM files are very large. **`assets/EMOJI`**, **`assets/sounds`**, and **`assets/STICKERS`** are committed so audio and network reactions work out of the box. You still need the full PVZ2 asset pack locally (`ATLASES/`, `IMAGES/`, `RESOURCES.json`, etc.) before running the GUI — obtain that from your team or course materials.

---

## Tech Stack

- **Language:** Java 25
- **Game framework:** libGDX 1.14.2
- **Animation / UI:** libPVZ PAM player, pvz-skin, Scene2D
- **Build:** Gradle
- **Database:** SQLite (local client DB + server DB)
- **Serialization:** Jackson
- **Tests:** JUnit 5

---

## Prerequisites

1. **JDK 25** installed and available on your `PATH`
2. **Full local `assets/` art pack** — `ATLASES/`, `IMAGES/`, `RESOURCES.json`, and related PVZ2 files (not stored in git)
3. **Bundled media is already in the repo** — `assets/sounds/`, `assets/EMOJI/`, and `assets/STICKERS/` are tracked; no manual copy step needed after clone
4. For online features: the **game server** running on port `5454` (default)

### Audio & reactions

The GUI loads BGM/SFX from `assets/sounds/`. Network match reactions use `assets/EMOJI/` and `assets/STICKERS/`. These folders are part of the repository.

### macOS note

The Gradle `lwjgl3:run` task automatically adds `-XstartOnFirstThread` on macOS. No extra setup is required.

---

## Getting Started

```bash
# Clone the repository
git clone https://github.com/advanced-progamming-sut-2026/group-16.git

# Enter the project directory
cd group-16

# Add the large PVZ2 art pack into assets/ (ATLASES, IMAGES, RESOURCES.json, …)
# EMOJI, sounds, and STICKERS are already in the repo.

# Verify the project compiles
./gradlew :core:compileJava
```

---

## Running the Game

Start the desktop GUI client:

```bash
./gradlew lwjgl3:run
```

What happens:

1. **Boot screen** preloads menu atlases
2. **Sign up / log in** (local SQLite by default)
3. **Main menu** → adventure, minigames, greenhouse, shop, etc.

The client connects to `127.0.0.1:5454` for network features. Start the server first if you want online login sync, leaderboard, or *I, Zombie* matchmaking.

Build a runnable distribution:

```bash
./gradlew lwjgl3:dist
# Output under lwjgl3/build/distributions/
```

---

## Running the Server

In a separate terminal:

```bash
./gradlew :server:run
```

By default the server listens on **port 5454** and stores accounts in `server/users.db`.

Custom port:

```bash
PVZ_SERVER_PORT=6000 ./gradlew :server:run
# or
./gradlew :server:run --args=6000
```

Custom database URL:

```bash
./gradlew :server:run -Dpvz.database.url=jdbc:sqlite:server/custom.db
```

Then launch the GUI client in another terminal.

---

## CLI Mode

Phase 1 gameplay is still available through the CLI entry point:

- **Main class:** `io.github.finalwave.Main`
- **Module:** `:core`

Recommended: run `Main` from your IDE after `./gradlew :core:compileJava`.

The CLI uses the same SQLite database file (`users.db` in the project root by default) and accepts typed commands for menus, planting, and match control. Most end-to-end coverage lives in the JUnit CLI smoke tests under `core/src/test/java/io/github/finalwave/controller/`.

---

## Tests

Run all module tests:

```bash
./gradlew test
```

Run only core tests:

```bash
./gradlew :core:test
```

Run a single test class:

```bash
./gradlew :core:test --tests "io.github.finalwave.controller.CliAdventureSmokeTest"
```

Compile without running tests:

```bash
./gradlew :core:compileJava :core:compileTestJava
```

---

## Configuration

| Property / env var | Default | Purpose |
|--------------------|---------|---------|
| `pvz.database.url` | `jdbc:sqlite:users.db` | Client/local SQLite database |
| `PVZ_DATABASE_URL` | — | Server database override |
| `PVZ_SERVER_PORT` | `5454` | Server listen port |
| `pvz.session.file` | `user.session` | Saved session file (optional) |

Example — isolated test database:

```bash
./gradlew lwjgl3:run -Dpvz.database.url=jdbc:sqlite:tmp/test-users.db
```

---

## Development Phases

| Phase | Focus | Status |
|-------|-------|--------|
| **0** | UML + repository setup | Done |
| **1** | Core engine + CLI gameplay | Done |
| **2** | libGDX GUI, animations, audio, menus, minigames | In progress / largely complete |
| **3** | Networking, server, online I, Zombie | In progress |

---

## Team Members

| Name | GitHub | Student Number |
|------|--------|----------------|
| Amirhossein Ebrahimi | [@amiseb](https://github.com/amiseb) | 404105372 |
| Mahdyar Entezami | [@MahdyarEn](https://github.com/MahdyarEn) | 404171003 |
| Shayan Salehe | [@shayan15sa](https://github.com/shayan15sa) | 404171111 |

---

<p align="center">
  Made with sunflowers, brains, and Gradle builds that take longer than a Gargantuar swing.
</p>
