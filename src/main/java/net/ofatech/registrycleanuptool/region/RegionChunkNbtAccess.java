package net.ofatech.registrycleanuptool.region;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

public final class RegionChunkNbtAccess {
    private final ServerLevel level;
    private final RegionFileStorage storage;

    public RegionChunkNbtAccess(ServerLevel level) {
        this.level = level;
        Path regionPath = level.getChunkSource().chunkMap.getStorageFolder().resolve("region");
        this.storage = new RegionFileStorage(new RegionStorageInfo(level.dimension().location().toString(), regionPath, "chunk"), regionPath, false);
    }

    public CompoundTag read(ChunkPos pos) throws IOException { return storage.read(pos); }
    public void write(ChunkPos pos, CompoundTag tag) throws IOException { storage.write(pos, tag); }
}
