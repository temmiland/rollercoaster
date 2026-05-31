# 🎢 Rollercoaster

[![Stars](https://img.shields.io/github/stars/temmiland/rollercoaster?style=social)](https://github.com/temmiland/rollercoaster/stargazers)
[![License](https://img.shields.io/github/license/temmiland/rollercoaster)](./LICENSE)

> **Rollercoaster** is a lightweight **LWJGL** foundation for graphical applications — it handles configuration, GLFW/OpenGL windowing, input and the main loop, so games and editors only need to implement their own `Game`.
> 🦚 Used as the base of [pxWorlds](https://github.com/temmiland/pxWorlds).

## ✨ Features

- Orchestrated subsystem bootstrapping (config → GLFW/window → main loop)
- Persistent JSON-based configuration storage
- GLFW window & input handling with fullscreen/windowed display modes
- Immediate-mode GUI primitives and widgets (buttons, text fields)
- Game state management and a built-in debug HUD

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/temmiland/rollercoaster.git
cd rollercoaster/
```

### 2. Build & install

Make sure you have Git and Java 21+ installed.

```bash
./mvnw clean install
```

This installs Rollercoaster into your local Maven repository so other projects can depend on it.

### 3. Use it as a dependency

```xml
<dependency>
    <groupId>temmiland</groupId>
    <artifactId>rollercoaster</artifactId>
    <version>0.0.1d</version>
</dependency>
```

### 4. Run a game

Implement a `Game` and hand it to Rollercoaster — it takes care of the rest:

```java
Rollercoaster.run(MyGame.class, configDirectory);
```

> 💡 On **macOS** you must launch the JVM with `-XstartOnFirstThread`, as required by GLFW.

This project is licensed under the MIT License.
Please see [`LICENSE`](https://github.com/temmiland/rollercoaster/blob/master/LICENSE) for more info.
