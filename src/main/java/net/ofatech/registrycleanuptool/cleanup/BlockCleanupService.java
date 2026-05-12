package net.ofatech.registrycleanuptool.cleanup;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class BlockCleanupService {
    private BlockCleanupService() {}

    public static CleanupStats processRadius(ServerLevel level, ChunkPos center, int radius, Set<ResourceLocation> targets, Block replacement, CleanupMode mode, int maxChanges) {
        CleanupStats stats = new CleanupStats();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = center.x - radius; cx <= center.x + radius; cx++) {
            for (int cz = center.z - radius; cz <= center.z + radius; cz++) {
                processChunk(level, new ChunkPos(cx, cz), targets, replacement, mode, maxChanges, stats, pos);
                if (stats.limitReached) return stats;
            }
        }
        return stats;
    }

    public static CleanupStats processChunk(ServerLevel level, ChunkPos cp, Set<ResourceLocation> targets, Block replacement, CleanupMode mode, int maxChanges) {
        CleanupStats stats = new CleanupStats();
        processChunk(level, cp, targets, replacement, mode, maxChanges, stats, new BlockPos.MutableBlockPos());
        return stats;
    }

    private static void processChunk(ServerLevel level, ChunkPos cp, Set<ResourceLocation> targets, Block replacement, CleanupMode mode, int maxChanges, CleanupStats stats, BlockPos.MutableBlockPos pos) {
        ChunkAccess chunk = level.getChunk(cp.x, cp.z);
        stats.chunksScanned++;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        for (int x = cp.getMinBlockX(); x <= cp.getMaxBlockX(); x++) for (int z = cp.getMinBlockZ(); z <= cp.getMaxBlockZ(); z++) for (int y = minY; y < maxY; y++) {
            pos.set(x,y,z);
            Block b = chunk.getBlockState(pos).getBlock();
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(b);
            if (targets.contains(id)) {
                stats.add(id, 1);
                if (mode == CleanupMode.CLEAN) {
                    level.setBlock(pos, replacement.defaultBlockState(), 3);
                    if (stats.total >= maxChanges) { stats.limitReached = true; return; }
                }
            }
        }
    }
}
