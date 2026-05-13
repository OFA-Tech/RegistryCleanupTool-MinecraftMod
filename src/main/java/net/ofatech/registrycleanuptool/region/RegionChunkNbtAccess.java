package net.ofatech.registrycleanuptool.region;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.storage.LevelResource;

public final class RegionChunkNbtAccess implements Closeable {
    private final WritableRegionFileStorage storage;

    public RegionChunkNbtAccess(ServerLevel level) {
        Path regionPath = getRegionPath(level);
        this.storage = new WritableRegionFileStorage(
                new RegionStorageInfo("chunk", level.dimension(), "chunk"),
                regionPath,
                false
        );
    }

    public CompoundTag read(ChunkPos pos) throws IOException {
        return storage.read(pos);
    }

    public void write(ChunkPos pos, CompoundTag tag) throws IOException {
        storage.writePublic(pos, tag);
    }

    @Override
    public void close() throws IOException {
        storage.close();
    }

    private static Path getRegionPath(ServerLevel level) {
        ResourceKey<Level> dimension = level.dimension();
        Path worldRoot = level.getServer().getWorldPath(LevelResource.ROOT);

        if (dimension == Level.OVERWORLD) {
            return worldRoot.resolve("region");
        }

        if (dimension == Level.NETHER) {
            return worldRoot.resolve("DIM-1").resolve("region");
        }

        if (dimension == Level.END) {
            return worldRoot.resolve("DIM1").resolve("region");
        }

        return worldRoot
                .resolve("dimensions")
                .resolve(dimension.location().getNamespace())
                .resolve(dimension.location().getPath())
                .resolve("region");
    }

    private static final class WritableRegionFileStorage extends RegionFileStorage {
        private WritableRegionFileStorage(RegionStorageInfo info, Path folder, boolean sync) {
            super(info, folder, sync);
        }

        private void writePublic(ChunkPos pos, CompoundTag tag) throws IOException {
            super.write(pos, tag);
        }
    }
}
