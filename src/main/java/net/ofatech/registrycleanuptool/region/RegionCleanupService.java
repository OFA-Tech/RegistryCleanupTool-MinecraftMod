package net.ofatech.registrycleanuptool.region;

import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

public final class RegionCleanupService {
    private final Path regionDirectory;
    private final RegionStorageInfo storageInfo;

    public RegionCleanupService(ServerLevel level) {
        this.regionDirectory = RegionChunkNbtAccess.getRegionPath(level);
        this.storageInfo = new RegionStorageInfo("chunk", level.dimension(), "chunk");
    }

    public boolean clearChunkIfPresent(ChunkPos chunkPos) throws IOException {
        Path regionFilePath = RegionChunkNbtAccess.resolveRegionFilePath(regionDirectory, chunkPos);
        try (RegionFile regionFile = new RegionFile(storageInfo, regionFilePath, regionDirectory, false)) {
            if (!regionFile.hasChunk(chunkPos)) {
                return false;
            }

            regionFile.clear(chunkPos);
            regionFile.flush();
            return true;
        }
    }
}
