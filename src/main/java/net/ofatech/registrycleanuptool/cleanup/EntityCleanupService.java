package net.ofatech.registrycleanuptool.cleanup;

import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public final class EntityCleanupService {
    private EntityCleanupService() {}

    public static CleanupStats processRadius(ServerLevel level, ChunkPos center, int radius, Set<ResourceLocation> targets, CleanupMode mode, Entity sourceEntity) {
        CleanupStats stats = new CleanupStats();
        int minX = (center.x - radius) << 4;
        int minZ = (center.z - radius) << 4;
        int maxX = ((center.x + radius) << 4) + 15;
        int maxZ = ((center.z + radius) << 4) + 15;
        AABB box = new AABB(minX, level.getMinBuildHeight(), minZ, maxX + 1D, level.getMaxBuildHeight(), maxZ + 1D);
        List<Entity> entities = level.getEntities(null, box);
        stats.chunksScanned = (radius * 2 + 1) * (radius * 2 + 1);
        for (Entity entity : entities) {
            if (entity instanceof Player || entity == sourceEntity) continue;
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (targets.contains(id)) {
                stats.add(id, 1);
                if (mode == CleanupMode.CLEAN) entity.discard();
            }
        }
        return stats;
    }
}
