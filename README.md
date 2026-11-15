# TRUE DELTA:TALE (Forge 1.20.1)

Ruined Dark World buildings, Deltarune-inspired spells, and a fusion system.

## Spells

| Item | How you get it | What it does |
|---|---|---|
| Rude Buster | Common ruins | Heavy slash, big damage and knockback |
| Fireshock | Common ruins | Fire bolt, sets target alight |
| IceShock | Common ruins | Freezes the target; kills shatter into ice |
| Snowgrave | Rare ruins | Blizzard where you aim, heavy damage + freeze |
| Thunder Buster | Rude Buster + Fireshock | Slash that calls down lightning |
| Firegrave | Snowgrave + any magic item | Firestorm where you aim |
| Knight's Swoon | IceShock + Firegrave | Dash forward and stun everything ahead. 5 uses |

Fusions are shapeless crafting recipes, so JEI shows them automatically (no extra JEI code needed).

Knight's Swoon is not destroyed when spent. Put it in an anvil with any other magic item to restore all 5 uses.

## Ruins

- Common: 11x11, spawn roughly every 6 chunks (`structure_set/dark_ruins.json`), 16-block tower
- Rare: 15x15 with a lava moat around the chest, roughly every 22 chunks (`structure_set/dark_ruins_rare.json`), 26-block tower
- Traps are deliberately obvious: a stone pressure plate on a red concrete cross, with TNT under it, plus the lava moat in rare ruins

Tune frequency by editing `spacing` / `separation` in the structure_set JSON files.

## Build

You need JDK 17.

1. Get the Gradle wrapper: copy `gradlew`, `gradlew.bat` and the `gradle/` folder from the official Forge 1.20.1 MDK
   (or install Gradle 8.1.1 and run `gradle wrapper`).
2. `./gradlew build` -> the jar is in `build/libs/`
3. `./gradlew runClient` to test in a dev client

## Testing in game

```
/give @s truedeltatale:snowgrave
/locate structure truedeltatale:dark_ruins
/locate structure truedeltatale:dark_ruins_rare
```

## Placeholder art

The item icons in `assets/truedeltatale/textures/item/` are generated placeholders (a staff with a coloured orb). Replace them with real 16x16 PNGs using the same file names.
