package net.ofatech.registrycleanuptool.discovery;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class MissingBlockCaptureStore {
    private final Set<MissingBlockOccurrence> captures = new LinkedHashSet<>();
    public synchronized void addAll(List<MissingBlockOccurrence> occurrences) { captures.addAll(occurrences); }
    public synchronized void clear() { captures.clear(); }
    public synchronized List<MissingBlockOccurrence> snapshot() { return List.copyOf(captures); }
    public synchronized List<MissingBlockOccurrence> snapshot(ResourceKey<Level> dim) { return captures.stream().filter(c -> c.dimension().equals(dim)).toList(); }
    public synchronized List<MissingBlockOccurrence> snapshot(ResourceKey<Level> dim, Set<ChunkPos> chunks) { return captures.stream().filter(c -> c.dimension().equals(dim) && chunks.contains(new ChunkPos(c.chunkX(), c.chunkZ()))).toList(); }
    public synchronized long uniqueMissingIdsCount() { return captures.stream().map(MissingBlockOccurrence::missingBlockId).distinct().count(); }
    public synchronized long uniqueChunksCount() { return captures.stream().map(c -> c.dimension().location() + ":" + c.chunkX() + "," + c.chunkZ()).distinct().count(); }
    public synchronized String summary() { return "captures=" + captures.size() + ", ids=" + uniqueMissingIdsCount() + ", chunks=" + uniqueChunksCount(); }
}
