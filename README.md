# Registry Cleanup Tool (NeoForge 1.21.1)

Server-side utility mod for worlds that contain orphaned registry content from removed mods.

## What it does
- Adds `/rct` admin commands to scan and clean blocks/entities in loaded chunk areas.
- Registers temporary placeholder blocks under exact IDs (`dwm:titanium_ore`, `rftoolsbase:dimensionalshard_overworld`) so affected chunks can deserialize, then be cleaned.

## What it does not do
- Not a client ghost-block visual fix.
- Not offline region/NBT surgery.
- Missing entity IDs already dropped during load are out of scope for v1.

## Why placeholders are needed
Vanilla commands cannot target unregistered block IDs. If the ID is missing from registries, chunk load substitutes defaults and direct command targeting is impossible. This mod can temporarily register known IDs exactly, then replace them safely.

## Safety first
Back up world before cleanup. Run scan first.

## Installation
1. Build jar with `./gradlew build`.
2. Place jar on dedicated server `mods/`.
3. Start server and run `/rct status`.

## Example config (common)
`blocksToClean=["dwm:titanium_ore","rftoolsbase:dimensionalshard_overworld"]`
`entitiesToClean=[]`
`replacementBlock="minecraft:air"`
`dryRunByDefault=true`
`maxChunksRadius=8`
`maxBlocksChangedPerCommand=500000`
`includeKnownPlaceholderBlocks=true`
`logCleanupDetails=true`

## Commands
- `/rct status`
- `/rct scan blocks radius <chunks>`
- `/rct clean blocks radius <chunks>`
- `/rct scan entities radius <chunks>`
- `/rct clean entities radius <chunks>`
- `/rct scan all radius <chunks>`
- `/rct clean all radius <chunks>`
- `/rct scan blocks chunk <chunkX> <chunkZ>`
- `/rct clean blocks chunk <chunkX> <chunkZ>`
- `/rct save`

## Suggested workflow
1. Back up world
2. Install mod
3. Start server
4. `/rct status`
5. `/rct scan blocks radius 8`
6. `/rct clean blocks radius 8`
7. `/save-all flush`
8. Stop server
9. Remove mod if cleanup is complete
