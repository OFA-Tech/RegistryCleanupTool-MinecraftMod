package net.ofatech.registrycleanuptool.cleanup;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class CleanupStats {
    public int chunksScanned;
    public int total;
    public boolean limitReached;
    public final Map<ResourceLocation, Integer> byId = new LinkedHashMap<>();

    public void add(ResourceLocation id, int amount) {
        byId.merge(id, amount, Integer::sum);
        total += amount;
    }
}
