package net.ofatech.registrycleanuptool.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.ofatech.registrycleanuptool.discovery.MissingBlockOccurrence;

public final class MissingRegistryLogParser {
    private static final Pattern SECTION = Pattern.compile("Recoverable errors when loading section \\[(\\-?\\d+),\\s*(\\-?\\d+),\\s*(\\-?\\d+)]");
    private static final Pattern MISSING_BLOCK = Pattern.compile("Unknown registry key in ResourceKey\\[minecraft:root / minecraft:block]: ([a-z0-9_.-]+:[a-z0-9_./-]+) -> using default");

    public List<MissingBlockOccurrence> parse(ResourceKey<Level> dimension, String line) {
        Matcher sectionMatcher = SECTION.matcher(line);
        if (!sectionMatcher.find()) return List.of();
        int chunkX = Integer.parseInt(sectionMatcher.group(1));
        int sectionY = Integer.parseInt(sectionMatcher.group(2));
        int chunkZ = Integer.parseInt(sectionMatcher.group(3));

        Matcher missingMatcher = MISSING_BLOCK.matcher(line);
        List<MissingBlockOccurrence> out = new ArrayList<>();
        while (missingMatcher.find()) {
            ResourceLocation id = ResourceLocation.tryParse(missingMatcher.group(1));
            if (id != null) out.add(new MissingBlockOccurrence(dimension, chunkX, sectionY, chunkZ, id));
        }
        return out;
    }
}
