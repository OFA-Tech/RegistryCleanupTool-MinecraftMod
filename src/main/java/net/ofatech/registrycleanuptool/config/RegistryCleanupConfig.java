package net.ofatech.registrycleanuptool.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RegistryCleanupConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue MAX_RADIUS = BUILDER.defineInRange("maxRadius", 8, 0, 64);
    public static final ModConfigSpec.IntValue MAX_CHUNKS_PER_COMMAND = BUILDER.defineInRange("maxChunksPerCommand", 512, 1, 50000);
    public static final ModConfigSpec.ConfigValue<String> REPLACEMENT_BLOCK = BUILDER.define("replacementBlock", "minecraft:air");
    public static final ModConfigSpec.BooleanValue AUTO_START_WATCHER = BUILDER.define("autoStartWatcher", true);
    public static final ModConfigSpec.BooleanValue ATTEMPT_CHUNK_RELOAD_AFTER_PATCH = BUILDER.define("attemptChunkReloadAfterPatch", true);
    public static final ModConfigSpec.BooleanValue LOG_DETAILS = BUILDER.define("logDetails", true);
    public static final ModConfigSpec SPEC = BUILDER.build();
    private RegistryCleanupConfig() {}
}
