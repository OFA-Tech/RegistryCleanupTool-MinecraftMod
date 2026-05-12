package net.ofatech.registrycleanuptool.cleanup;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.ofatech.registrycleanuptool.config.RegistryCleanupConfig;
import net.ofatech.registrycleanuptool.discovery.MissingBlockCaptureStore;
import net.ofatech.registrycleanuptool.discovery.MissingBlockOccurrence;
import net.ofatech.registrycleanuptool.logging.ChunkSerializerLogWatcher;
import net.ofatech.registrycleanuptool.region.ChunkNbtPatchService;
import net.ofatech.registrycleanuptool.region.RegionChunkNbtAccess;

public final class LogTriggeredCleanupService {
    public record CleanupSummary(int chunksScanned, int sectionsWithMissingIds, int chunksPatched, int paletteEntriesReplaced, int idsReplaced, int failedChunks, int chunksNeedReload) {}

    private final MissingBlockCaptureStore store;
    private final ChunkSerializerLogWatcher watcher;
    private CleanupSummary lastSummary = new CleanupSummary(0,0,0,0,0,0,0);

    public LogTriggeredCleanupService(MissingBlockCaptureStore store, ChunkSerializerLogWatcher watcher) { this.store = store; this.watcher = watcher; }
    public CleanupSummary lastSummary() { return lastSummary; }
    public MissingBlockCaptureStore store() { return store; }
    public ChunkSerializerLogWatcher watcher() { return watcher; }

    public Set<ChunkPos> forceLoadRadius(ServerLevel level, ChunkPos center, int radius, int maxChunks) {
        watcher.setContextLevel(level);
        int clamped = Math.min(radius, RegistryCleanupConfig.MAX_RADIUS.get());
        Set<ChunkPos> scanned = new HashSet<>();
        for (int dx = -clamped; dx <= clamped; dx++) for (int dz = -clamped; dz <= clamped; dz++) {
            if (scanned.size() >= maxChunks) return scanned;
            ChunkPos cp = new ChunkPos(center.x + dx, center.z + dz);
            scanned.add(cp);
            level.getChunk(cp.x, cp.z);
        }
        return scanned;
    }

    public CleanupSummary cleanCaptured(ServerLevel level, java.util.List<MissingBlockOccurrence> occurrences) {
        ChunkNbtPatchService.Result r = new ChunkNbtPatchService().patch(new RegionChunkNbtAccess(level), occurrences, ResourceLocation.parse(RegistryCleanupConfig.REPLACEMENT_BLOCK.get()));
        int sections = (int) occurrences.stream().map(o -> o.chunkX()+":"+o.sectionY()+":"+o.chunkZ()).distinct().count();
        lastSummary = new CleanupSummary(0, sections, r.chunksPatched(), r.entriesReplaced(), r.idsReplaced().size(), r.failedChunks().size(), r.chunksPatched());
        return lastSummary;
    }
}
