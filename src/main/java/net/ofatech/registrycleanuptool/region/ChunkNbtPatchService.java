package net.ofatech.registrycleanuptool.region;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.ofatech.registrycleanuptool.discovery.MissingBlockOccurrence;

public final class ChunkNbtPatchService {
    public record Result(int chunksPatched, int entriesReplaced, Set<ResourceLocation> idsReplaced, Set<ChunkPos> failedChunks) {}

    public Result patch(RegionChunkNbtAccess access, List<MissingBlockOccurrence> occurrences, ResourceLocation replacement) {
        int patched = 0, replaced = 0;
        Set<ResourceLocation> ids = new HashSet<>();
        Set<ChunkPos> failed = new HashSet<>();
        var byChunk = occurrences.stream().collect(java.util.stream.Collectors.groupingBy(o -> new ChunkPos(o.chunkX(), o.chunkZ())));
        for (var e : byChunk.entrySet()) {
            ChunkPos cp = e.getKey();
            try {
                CompoundTag chunk = access.read(cp);
                if (chunk == null) continue;
                boolean changed = false;
                ListTag sections = chunk.getCompound("sections") != null ? chunk.getList("sections", Tag.TAG_COMPOUND) : chunk.getCompound("Level").getList("sections", Tag.TAG_COMPOUND);
                for (Tag sectionTag : sections) {
                    CompoundTag section = (CompoundTag) sectionTag;
                    int y = section.getByte("Y");
                    Set<String> missingIds = e.getValue().stream().filter(o -> o.sectionY() == y).map(o -> o.missingBlockId().toString()).collect(java.util.stream.Collectors.toSet());
                    if (missingIds.isEmpty()) continue;
                    CompoundTag blockStates = section.getCompound("block_states");
                    ListTag palette = blockStates.getList("palette", Tag.TAG_COMPOUND);
                    for (Tag paletteTag : palette) {
                        CompoundTag pal = (CompoundTag) paletteTag;
                        String name = pal.getString("Name");
                        if (missingIds.contains(name)) {
                            pal.putString("Name", replacement.toString());
                            pal.remove("Properties");
                            replaced++;
                            ids.add(ResourceLocation.parse(name));
                            changed = true;
                        }
                    }
                }
                if (changed) { access.write(cp, chunk); patched++; }
            } catch (IOException ex) { failed.add(cp); }
        }
        return new Result(patched, replaced, ids, failed);
    }
}
