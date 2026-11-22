# TrafficCraft: Traffic Light Countdown

A client-side addon for [TrafficCraft](https://www.curseforge.com/minecraft/mc-mods/trafficcraft) by MrJulsen. It adds a countdown timer overlay on traffic lights, showing the remaining seconds until the next phase change — just like real-world traffic signals.

Available for **Fabric** and **NeoForge** on Minecraft **1.21.1**.

## Features

- **Three-section lights:** timer displayed on the yellow segment
- **Two-section lights (pedestrian):** timer on the currently unlit segment
- **Single-section lights:** no timer (nothing to count down)
- **Color-coded:** matches the currently active signal (red / green)
- **Hidden during yellow:** transition phase — no timer shown
- **7-segment display style:** looks like a real traffic light countdown
- **Supports both control modes:**
    - `Own Schedule`: reads directly from the light's schedule
    - `Remote` (via Traffic Light Controller): learns the schedule by observation (needs 1–2 full cycles)
- **Handles pauses:** shows blinking `-- --` when the game is paused
- **Overflow protection:** blinks `99` if the phase is longer than 99 seconds
- **Short phase filter:** doesn't show timer for phases shorter than 3 seconds

## Requirements

- Minecraft **1.21.1**
- [DragonLib](https://www.curseforge.com/minecraft/mc-mods/dragonlib) (1.21.1)
- [TrafficCraft](https://www.curseforge.com/minecraft/mc-mods/trafficcraft) (1.21.1)

**Fabric:**

- Fabric Loader **0.19+**
- [Fabric API](https://modrinth.com/mod/fabric-api)

**NeoForge:**

- NeoForge **21.1+**

## Installation

1. Install Fabric or NeoForge for Minecraft 1.21.1
2. Download DragonLib and TrafficCraft and put them in your `mods/` folder
3. Download the matching release of this mod (fabric or neoforge) and put it in `mods/`
4. Launch Minecraft

## Compatibility

- ✅ Works with all standard TrafficCraft traffic lights (1, 2, 3 sections)
- ✅ Works with Traffic Light Controllers
- ✅ Works in singleplayer and multiplayer
- ⚠️ Client-side only — the server does not need the mod installed

## Building from source

Requires **JDK 21**.

1. Clone this repository
2. Run `./gradlew build`
3. The compiled jars will be in `fabric/build/libs/` and `neoforge/build/libs/`

## License

[MIT](LICENSE)

## Credits

- **MrJulsen** — author of [TrafficCraft](https://github.com/MisterJulsen/TrafficCraft), the base mod this addon extends
- Timer visuals inspired by real-world traffic signal countdown displays
