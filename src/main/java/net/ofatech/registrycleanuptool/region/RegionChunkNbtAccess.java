package net.ofatech.registrycleanuptool.region;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.storage.LevelResource;

public final class RegionChunkNbtAccess implements Closeable {
    private final Path regionPath;
    private final RegionStorageInfo storageInfo;

    public RegionChunkNbtAccess(ServerLevel level) {
        this.regionPath = getRegionPath(level);
        this.storageInfo = new RegionStorageInfo("chunk", level.dimension(), "chunk");
    }

    public CompoundTag read(ChunkPos pos) throws IOException {
        Path regionFilePath = resolveRegionFilePath(regionPath, pos);
        try (RegionFile regionFile = new RegionFile(storageInfo, regionFilePath, regionPath, false)) {
            try (DataInputStream input = regionFile.getChunkDataInputStream(pos)) {
                return input == null ? null : NbtIo.read(input);
            }
        }
    }

    public void write(ChunkPos pos, CompoundTag tag) throws IOException {
        Path regionFilePath = resolveRegionFilePath(regionPath, pos);
        try (RegionFile regionFile = new RegionFile(storageInfo, regionFilePath, regionPath, false)) {
            try (DataOutputStream output = regionFile.getChunkDataOutputStream(pos)) {
                NbtIo.write(tag, output);
            }
            regionFile.flush();
        }
    }

    @Override
    public void close() {
        // No-op: RegionFile instances are opened per-operation and closed via try-with-resources.
    }

    public static Path resolveRegionFilePath(Path regionDirectory, ChunkPos chunkPos) {
        int regionX = chunkPos.x >> 5;
        int regionZ = chunkPos.z >> 5;
        String filename = "r." + regionX + "." + regionZ + ".mca";
        return regionDirectory.resolve(filename);
    }

    static Path getRegionPath(ServerLevel level) {
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
}
