package net.ofatech.registrycleanuptool.config;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.ofatech.registrycleanuptool.util.ResourceLocationUtil;

public final class RegistryCleanupConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKS_TO_CLEAN = BUILDER.defineListAllowEmpty(
            "blocksToClean", List.of("dwm:titanium_ore", "rftoolsbase:dimensionalshard_overworld"), () -> "", RegistryCleanupConfig::validRL);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITIES_TO_CLEAN = BUILDER.defineListAllowEmpty(
            "entitiesToClean", List.of(), () -> "", RegistryCleanupConfig::validRL);
    public static final ModConfigSpec.ConfigValue<String> REPLACEMENT_BLOCK = BUILDER.define("replacementBlock", "minecraft:air");
    public static final ModConfigSpec.BooleanValue DRY_RUN_BY_DEFAULT = BUILDER.define("dryRunByDefault", true);
    public static final ModConfigSpec.IntValue MAX_CHUNKS_RADIUS = BUILDER.defineInRange("maxChunksRadius", 8, 0, 64);
    public static final ModConfigSpec.IntValue MAX_BLOCKS_CHANGED_PER_COMMAND = BUILDER.defineInRange("maxBlocksChangedPerCommand", 500000, 1, Integer.MAX_VALUE);
    public static final ModConfigSpec.BooleanValue INCLUDE_KNOWN_PLACEHOLDER_BLOCKS = BUILDER.define("includeKnownPlaceholderBlocks", true);
    public static final ModConfigSpec.BooleanValue LOG_CLEANUP_DETAILS = BUILDER.define("logCleanupDetails", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validRL(Object o) { return o instanceof String s && ResourceLocationUtil.parse(s) != null; }

    public static ResourceLocation replacementBlockId() {
        var rl = ResourceLocationUtil.parse(REPLACEMENT_BLOCK.get());
        if (rl == null || !BuiltInRegistries.BLOCK.containsKey(rl)) {
            return ResourceLocation.withDefaultNamespace("air");
        }
        return rl;
    }
}
