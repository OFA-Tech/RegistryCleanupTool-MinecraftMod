package net.ofatech.registrycleanuptool.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.ofatech.registrycleanuptool.RegistryCleanupTool;
import net.ofatech.registrycleanuptool.cleanup.*;
import net.ofatech.registrycleanuptool.config.RegistryCleanupConfig;
import net.ofatech.registrycleanuptool.registry.PlaceholderRegistry;
import net.ofatech.registrycleanuptool.util.ResourceLocationUtil;

@EventBusSubscriber(modid = RegistryCleanupTool.MODID)
public final class RegistryCleanupCommands {
    private RegistryCleanupCommands() {}
    public static void registerEventHandlers() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(root());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("rct")
                .then(Commands.literal("status").requires(s -> s.hasPermission(2)).executes(RegistryCleanupCommands::status))
                .then(scanOrClean("scan", CleanupMode.SCAN, 2))
                .then(scanOrClean("clean", CleanupMode.CLEAN, 3))
                .then(Commands.literal("save").requires(s -> s.hasPermission(3)).executes(ctx -> { ctx.getSource().sendSuccess(() -> Component.literal("Run /save-all flush after verifying cleanup."), true); return 1; }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> scanOrClean(String name, CleanupMode mode, int perm) {
        return Commands.literal(name).requires(s -> s.hasPermission(perm))
                .then(Commands.literal("blocks").then(Commands.literal("radius").then(Commands.argument("chunks", IntegerArgumentType.integer(0)).executes(c -> runBlocksRadius(c, mode))))
                        .then(Commands.literal("chunk").then(Commands.argument("chunkX", IntegerArgumentType.integer()).then(Commands.argument("chunkZ", IntegerArgumentType.integer()).executes(c -> runBlocksChunk(c, mode))))))
                .then(Commands.literal("entities").then(Commands.literal("radius").then(Commands.argument("chunks", IntegerArgumentType.integer(0)).executes(c -> runEntitiesRadius(c, mode)))))
                .then(Commands.literal("all").then(Commands.literal("radius").then(Commands.argument("chunks", IntegerArgumentType.integer(0)).executes(c -> runAll(c, mode)))));
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        var s = ctx.getSource();
        s.sendSuccess(() -> Component.literal("Registry Cleanup Tool status:"), false);
        s.sendSuccess(() -> Component.literal("Replacement block: " + RegistryCleanupConfig.replacementBlockId()), false);
        s.sendSuccess(() -> Component.literal("Dry run default: " + RegistryCleanupConfig.DRY_RUN_BY_DEFAULT.get()), false);
        s.sendSuccess(() -> Component.literal("Max chunks radius: " + RegistryCleanupConfig.MAX_CHUNKS_RADIUS.get() + ", max blocks/command: " + RegistryCleanupConfig.MAX_BLOCKS_CHANGED_PER_COMMAND.get()), false);
        s.sendSuccess(() -> Component.literal("Backup your world before cleanup. Run /rct scan first."), false);
        for (String idS : RegistryCleanupConfig.BLOCKS_TO_CLEAN.get()) {
            ResourceLocation id = ResourceLocationUtil.parse(idS);
            boolean reg = id != null && BuiltInRegistries.BLOCK.containsKey(id);
            String marker = PlaceholderRegistry.isPlaceholder(id) ? "registered placeholder" : (reg ? "registered" : "not registered");
            s.sendSuccess(() -> Component.literal("- block " + idS + ": " + marker), false);
        }
        if (RegistryCleanupConfig.ENTITIES_TO_CLEAN.get().isEmpty()) s.sendSuccess(() -> Component.literal("- entities: none configured"), false);
        for (String idS : RegistryCleanupConfig.ENTITIES_TO_CLEAN.get()) {
            ResourceLocation id = ResourceLocationUtil.parse(idS);
            boolean reg = id != null && BuiltInRegistries.ENTITY_TYPE.containsKey(id);
            s.sendSuccess(() -> Component.literal("- entity " + idS + ": " + (reg ? "registered" : "not registered")), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int runBlocksRadius(CommandContext<CommandSourceStack> ctx, CleanupMode mode) { return reportBlocks(ctx.getSource(), BlockCleanupService.processRadius(level(ctx), new ChunkPos(ctx.getSource().getPosition()), clamp(IntegerArgumentType.getInteger(ctx, "chunks")), blockTargets(), replacement(), mode, RegistryCleanupConfig.MAX_BLOCKS_CHANGED_PER_COMMAND.get()), mode); }
    private static int runBlocksChunk(CommandContext<CommandSourceStack> ctx, CleanupMode mode) { return reportBlocks(ctx.getSource(), BlockCleanupService.processChunk(level(ctx), new ChunkPos(IntegerArgumentType.getInteger(ctx,"chunkX"), IntegerArgumentType.getInteger(ctx,"chunkZ")), blockTargets(), replacement(), mode, RegistryCleanupConfig.MAX_BLOCKS_CHANGED_PER_COMMAND.get()), mode); }
    private static int runEntitiesRadius(CommandContext<CommandSourceStack> ctx, CleanupMode mode) { return reportEntities(ctx.getSource(), EntityCleanupService.processRadius(level(ctx), new ChunkPos(ctx.getSource().getPosition()), clamp(IntegerArgumentType.getInteger(ctx, "chunks")), entityTargets(), mode, ctx.getSource().getEntity()), mode); }
    private static int runAll(CommandContext<CommandSourceStack> ctx, CleanupMode mode) { runBlocksRadius(ctx, mode); return runEntitiesRadius(ctx, mode); }

    private static int reportBlocks(CommandSourceStack s, CleanupStats st, CleanupMode mode) { if(mode==CleanupMode.CLEAN)s.sendSuccess(()->Component.literal("Backup your world before cleanup. Run /rct scan first."),true); s.sendSuccess(() -> Component.literal("Scanned " + st.chunksScanned + " chunks in " + s.getLevel().dimension().location()), true); st.byId.forEach((id,c)->s.sendSuccess(()->Component.literal("- " + id + ": " + c), true)); s.sendSuccess(() -> Component.literal((mode==CleanupMode.CLEAN?"Total replaced: ":"Total found: ") + st.total + (st.limitReached?" (limit reached)":"")), true); return st.total; }
    private static int reportEntities(CommandSourceStack s, CleanupStats st, CleanupMode mode) { if(mode==CleanupMode.CLEAN)s.sendSuccess(()->Component.literal("Backup your world before cleanup. Run /rct scan first."),true); s.sendSuccess(() -> Component.literal("Scanned entity area covering " + st.chunksScanned + " chunks in " + s.getLevel().dimension().location()), true); st.byId.forEach((id,c)->s.sendSuccess(()->Component.literal("- " + id + ": " + c), true)); s.sendSuccess(() -> Component.literal((mode==CleanupMode.CLEAN?"Total removed: ":"Total found: ") + st.total), true); return st.total; }

    private static int clamp(int radius) { return Math.min(radius, RegistryCleanupConfig.MAX_CHUNKS_RADIUS.get()); }
    private static ServerLevel level(CommandContext<CommandSourceStack> ctx) { return ctx.getSource().getLevel(); }
    private static Set<ResourceLocation> blockTargets() { Set<ResourceLocation> s = new LinkedHashSet<>(); for (String e: RegistryCleanupConfig.BLOCKS_TO_CLEAN.get()) { var rl = ResourceLocationUtil.parse(e); if (rl != null && BuiltInRegistries.BLOCK.containsKey(rl)) s.add(rl);} return s; }
    private static Set<ResourceLocation> entityTargets() { Set<ResourceLocation> s = new LinkedHashSet<>(); for (String e: RegistryCleanupConfig.ENTITIES_TO_CLEAN.get()) { var rl = ResourceLocationUtil.parse(e); if (rl != null && BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) s.add(rl);} return s; }
    private static Block replacement() { return BuiltInRegistries.BLOCK.get(RegistryCleanupConfig.replacementBlockId()); }
}
