# TrafficCraft: Traffic Light Countdown

A client-side Fabric addon for [TrafficCraft](https://www.curseforge.com/minecraft/mc-mods/trafficcraft) by MrJulsen. Adds a countdown timer overlay on traffic lights, showing the remaining seconds until the next phase change — just like real-world traffic signals.

## Features

- **Three-section lights:** timer displayed on the yellow segment
- **Two-section lights (pedestrian):** timer on the currently unlit segment
- **Single-section lights:** no timer (nothing to count down)
- **Color-coded:** matches the currently active signal (red / green)
- **Hidden during yellow:** transition phase — no timer shown
- **7-segment display style:** looks like a real traffic light countdown
- **Supports both control modes:**
    - `Own Schedule`: reads directly from the light's schedule
    - `Remote` (via Traffic Light Controller): learns the schedule by observation (needs 1-2 full cycles)
- **Handles pauses:** shows blinking `-- --` when the game is paused
- **Overflow protection:** blinks `99` if the phase is longer than 99 seconds
- **Short phase filter:** doesn't show timer for phases shorter than 3 seconds

## Requirements

- Minecraft **1.20.1**
- Fabric Loader **0.15+**
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Architectury API](https://modrinth.com/mod/architectury-api) (Fabric)
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (Fabric)
- [DragonLib](https://www.curseforge.com/minecraft/mc-mods/dragonlib) (Fabric, 1.20.1)
- [TrafficCraft](https://www.curseforge.com/minecraft/mc-mods/trafficcraft) **1.2.0-beta.3+** (Fabric)

## Installation

1. Install Fabric Loader for Minecraft 1.20.1
2. Download all required dependencies (see above) and put them in your `mods/` folder
3. Download the latest release of this mod and put it in `mods/` too
4. Launch Minecraft

## Building from source

This project depends on **TrafficCraft** and **DragonLib** jars that are not included in this repository (respect their licenses).

1. Clone this repository
2. Download the Fabric versions of TrafficCraft and DragonLib for Minecraft 1.20.1
3. Put both jars into the `libs/` folder in the project root
4. Run `./gradlew build`
5. The compiled jar will be in `build/libs/`

## Compatibility

- ✅ Works with all standard TrafficCraft traffic lights (1, 2, 3 sections)
- ✅ Works with Traffic Light Controllers
- ✅ Works in singleplayer and multiplayer
- ⚠️ Client-side only — server does not need the mod installed

## License

[MIT](LICENSE)

## Credits

- **MrJulsen** — author of [TrafficCraft](https://github.com/MisterJulsen/TrafficCraft), the base mod this addon extends
- Timer visuals inspired by real-world traffic signal countdown displays