# TrafficCraft: Traffic Light Countdown

A client-side addon for [TrafficCraft](https://www.curseforge.com/minecraft/mc-mods/trafficcraft) by MrJulsen. It adds a countdown timer overlay on traffic lights, showing the remaining seconds until the next phase change — just like real-world traffic signals.

Available for **Fabric** and **Forge** on Minecraft **1.20.1**.

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

- Minecraft **1.20.1**
- [DragonLib](https://www.curseforge.com/minecraft/mc-mods/dragonlib) (1.20.1)
- [TrafficCraft](https://www.curseforge.com/minecraft/mc-mods/trafficcraft) **1.2.0-beta.3+** (1.20.1)

**Fabric:**

- Fabric Loader **0.15+**
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Architectury API](https://modrinth.com/mod/architectury-api) (Fabric)
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (Fabric)

**Forge:**

- Forge **47+**

## Installation

1. Install Fabric or Forge for Minecraft 1.20.1
2. Put DragonLib and TrafficCraft (and, on Fabric, Fabric API + Architectury API + Forge Config API Port) into your `mods/` folder
3. Download the matching release of this mod (fabric or forge) and put it in `mods/`
4. Launch Minecraft

## Compatibility

- ✅ Works with all standard TrafficCraft traffic lights (1, 2, 3 sections)
- ✅ Works with Traffic Light Controllers
- ✅ Works in singleplayer and multiplayer
- ⚠️ Client-side only — the server does not need the mod installed

## Building from source

Requires **JDK 17**. This project depends on **TrafficCraft** jars that are not included in this repository (respect their licenses).

1. Clone this repository
2. Put the Fabric and Forge TrafficCraft jars for Minecraft 1.20.1 into the `libs/` folder in the project root
3. Run `./gradlew build`
4. The compiled jars will be in `fabric/build/libs/` and `forge/build/libs/`

## License

[MIT](LICENSE)

## Credits

- **MrJulsen** — author of [TrafficCraft](https://github.com/MisterJulsen/TrafficCraft), the base mod this addon extends
- Timer visuals inspired by real-world traffic signal countdown displays
