<div align="center">

<br>

# 🎧 Enmelo · Vision

### The why, the what, and the rules we build by

![Status](https://img.shields.io/badge/status-draft_0.2-orange?style=for-the-badge)
![Platform](https://img.shields.io/badge/platform-Android_first-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![License](https://img.shields.io/badge/license-GPL-blue?style=for-the-badge)

</div>

> 📝 Items marked **[PROPOSED]** are suggestions the project owner has not yet confirmed. Everything else comes from the owner's own decisions.

---

## 📑 Table of Contents

1. [What Enmelo Is](#-what-enmelo-is)
2. [Who It Is For](#-who-it-is-for)
3. [What It Does (Long-Term)](#-what-it-does-long-term)
4. [Platforms](#-platforms)
5. [What It Is Not](#-what-it-is-not)
6. [Principles](#-principles)
7. [First Release (v0.1)](#-first-release-v01)
8. [v1 Scope](#-v1-scope)
9. [Pace and Ownership](#-pace-and-ownership)
10. [Open Questions](#-open-questions)
11. [Related Projects](#-related-projects)

---

## 🎯 What Enmelo Is

Enmelo is an all-in-one, modern, feature-rich player for everything audio: music, music videos, podcasts, audiobooks. It aims to bring together features that today are scattered across many apps, in one fast and customizable player.

It is **not** a basic player. The goal is a rare, all-features player that does not exist on the Play Store today.

---

## 👥 Who It Is For

| Audience | What they want |
|---|---|
| 📚 **Mixed-library people** | Files on the phone, downloaded files, and music from a home server, all shown as one library |
| 💎 **Sound-quality fans** | Lossless and high-resolution formats (FLAC, WAV and others) as first-class, with the app telling you when one is playing |
| 🏠 **Self-hosters** | A great client for Jellyfin and Encore |
| 🤝 **Open-source contributors** | A codebase they can understand and extend. Enmelo is built for the whole world, not for one country |

---

## 🌍 What It Does (Long-Term)

- 🎵 Plays music, music videos, podcasts, audiobooks, and other audio
- 🖼️ Spotify / Apple Music-style interface with custom library management
- 📂 One organized library across three sources: phone library, downloaded library, and server library
- 🔌 Talks to servers through plugins: **Encore** (the companion server), native servers, and **Jellyfin**. Each plugin may behave differently
- 📤 Can submit music files to a server from any client device
- 🎨 Customizable and regenerable UI, driven by XML-like description files (how far this goes on Android is an open research question)
- 🔁 Every feature that lands on one platform should reach the others

---

## 📱 Platforms

| Priority | Platform | Notes |
|:-:|---|---|
| 1 | 🤖 **Android** | Primary focus, native |
| 2 | 💻 **Windows / PC** | Lightweight, with a UI that feels psychologically familiar to the Android one |
| 3 | 📺 **Smart Android TVs (LG)** and other music-playing devices | |

---

## 🚫 What It Is Not

- ❌ Not a basic "play my files" player
- ❌ Not a video player. It can play normal videos, but its interface and features are built around music videos
- ❌ Not a closed or ad-supported service. It is open source
- ❌ **[PROPOSED]** Not a streaming service. It plays what you own or host; it does not license catalogs

---

## 🧭 Principles

Every contributor is asked to respect these. Break one only with a written reason (see `docs/decisions/`).

| # | Principle | In short |
|:-:|---|---|
| 1 | ⚡ **Fast and snappy first** | The app must never feel laggy or glitchy. A feature that makes it slow needs a strong reason, or it does not ship |
| 2 | 🧩 **Lightweight core, features as modules and plugins** | The core stays small. Servers, sources, and optional features plug in without changing the core |
| 3 | 🔁 **One idea, every platform** | Design features so they can be carried to Android, PC, and TV, not tied to one |
| 4 | 📖 **Understandable by strangers** | Code, docs, and decisions are written so a newcomer can pick the project up and change it safely |
| 5 | 🛠️ **Easy to edit, debug, fix, and maintain** | Prefer simple, readable solutions over clever ones |
| 6 | 🔍 **Honest about quality** | The app is truthful about what it is playing (format, bit depth) and what it can and cannot do |

---

## 🚀 First Release (v0.1)

The first prototype only has to **make it run**: play the audio files stored on the phone. Everything else (new UIs, server plugins, version layers) comes after, one step at a time.

**[PROPOSED]** v0.1 done means:

- [ ] The app opens and lists audio files from the phone
- [ ] Tapping a track plays it, with play/pause/seek
- [ ] Playback continues with the screen off

---

## 📦 v1 Scope

Decided 2026-11-13. These ten features make up v1. Everything else is on the roadmap in the README and comes later.

| # | Feature | Decision |
|:-:|---|---|
| 1 | 🎵 **Queue** | Play next, add to end, add before/after a song, reorder, remove. The queue is what is playing now, separate from playlists |
| 2 | 📂 **Playlists** | A saved set of songs. One song can be in many playlists. A playlist can be loaded into the queue |
| 3 | 📚 **My library** | Tabs for playlists, albums, artists, podcasts, audiobooks (layout not designed yet). Albums with several artists are split so every artist gets credit, even when tags are messy. Albums live inside the artist page |
| 4 | 💎 **Lossless audio** | FLAC and WAV support, a lossless flag like Apple Music's, and "info for nerds" (bitrate, format, other file details, MusicBee-style) in the 3-dot menu |
| 5 | 🖤 **Modern minimalist UI** | Clean and fast |
| 6 | 🌗 **True black and white themes** | Real black for OLED, not grayish. Custom themes and templates come later |
| 7 | ❤️ **Favourites** | One tap to favourite a song |
| 8 | ⏯️ **Pick up where you left** | Music, podcasts and audiobooks resume from the last position, not from the start |
| 9 | 😴 **Sleep timer** | A normal timer in v1. Sensing sleep through a smartwatch or phone usage is an experiment for later |
| 10 | 🔀 **Real randomizer** | "Previous" goes back to the song that actually played before. No song repeats within one round |

---

## ⏳ Pace and Ownership

There is no deadline. Enmelo is built as time allows around studies and may be a lifelong project. Because the owner cannot promise long-term support alone, the project aims to be easy for others to pick up and carry forward.

---

## ❓ Open Questions

- [ ] Android language: Kotlin or Java (undecided)
- [ ] Library tab layout and overall UI design
- [ ] How far XML-style UI regeneration can go on Android
- [ ] Domain and handle availability for the name "Enmelo" (no clash found on web search; the owner must verify GitHub, handles, and domains directly)
- [ ] License version: GPL-3.0-only or GPL-3.0-or-later?

---

## 🔗 Related Projects

- **Encore**: the self-hosted companion server (library, serving, reports, wishes). Idea stage.