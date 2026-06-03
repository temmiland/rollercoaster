# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-06-03

### Added

- **Platform / Window** — GLFW window lifecycle management including creation, cleanup, vsync, fullscreen toggle, and display mode switching
- **Input** — `InputHandler` registers GLFW callbacks and queues raw `InputEvent`s; `InputProcessor` maintains per-tick pressed/released/held state; `Key` enum covers all GLFW key codes and mouse buttons
- **Game loop** — Fixed-timestep accumulator with spiral-of-death guard, adaptive frame pacing (VSync and software frame limiter), hybrid park-then-spin sleep for precise frame timing, and a JVM shutdown hook
- **Game state** — `GameStateManager` with push/pop/swap transitions; `GameState` base class covering update, render, GUI render, debug, and resize lifecycle
- **Graphics** — `Shader` (GLSL compile/link/uniform helpers), `Camera` (orthographic/perspective with JOML), `Renderer` (base draw interface), `SpriteBatch` (batched quad rendering), `Sprite`, `TextureAtlas`, `Animation`, `Model`
- **Physics** — `AABB` axis-aligned bounding box with overlap, intersection, and sweep tests; `Collision` response helpers
- **World** — `TileWorld` chunk-based tile map; `TileChunk` (fixed-size tile storage with dirty tracking); `WorldListener` callback interface
- **GUI** — `GuiContext`, `GuiPrimitives`, `GuiRenderer`; `TextField` and `ButtonSkin`/`ButtonState` widgets; `GuiInput` event routing
- **Config** — JSON-backed `Configuration` / `ConfigurationStorage` with screen resolution and window type (`ScreenResolutions`, `ScreenTypes`, `ScreenConfiguration`)
- **Debug** — `DebugHud` overlay with FPS counter and toggleable detailed view
- **CI** — GitHub Actions workflows for cross-platform artifact builds (Linux, macOS ARM64, Windows), GitHub Packages publishing, and GitHub release creation; mirrored Gitea Actions workflows for Linux builds and release sync

### Changed

- Renamed package `configuration` → `config` and `platform.io` → `platform.input`
- Moved `debug` and `rendering` out of the `game` sub-package into dedicated top-level packages

[Unreleased]: https://github.com/temmiland/rollercoaster/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/temmiland/rollercoaster/releases/tag/v0.1.0
