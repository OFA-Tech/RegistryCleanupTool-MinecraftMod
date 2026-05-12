# Registry Cleanup Tool (NeoForge 1.21.1)

Registry Cleanup Tool now uses **same-session, log-triggered raw chunk NBT cleanup**.

## What it does
- Does **not** register placeholder blocks/entities.
- Does **not** mutate registries at runtime (registries are frozen after startup).
- Watches `ChunkSerializer` recoverable errors for unknown block registry keys while chunks load.
- Captures section coordinates `[chunkX, sectionY, chunkZ]` plus missing block IDs.
- Patches raw chunk NBT `sections[].block_states.palette[]` entries, replacing matching missing IDs with `minecraft:air`.

## Safety
- **Backup is mandatory** before cleanup.
- If a chunk is already loaded, in-memory fallback state may remain visible until unload/reload.
- Restart is not required in the ideal path, but if unload/reload is not possible, restart may still be needed to force re-read from disk.
- Do not use `/save-all` before patched chunks are safely reloaded.

## Commands
- `/rct status`
- `/rct watch start`
- `/rct watch stop`
- `/rct watch list`
- `/rct scan radius <chunks>`
- `/rct cleanlog radius <chunks>`
- `/rct cleanlog captured`
- `/rct captured clear`

