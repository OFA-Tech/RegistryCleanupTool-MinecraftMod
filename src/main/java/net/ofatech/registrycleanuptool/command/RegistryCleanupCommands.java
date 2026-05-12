package net.ofatech.registrycleanuptool.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.ofatech.registrycleanuptool.RegistryCleanupTool;
import net.ofatech.registrycleanuptool.cleanup.LogTriggeredCleanupService;
import net.ofatech.registrycleanuptool.config.RegistryCleanupConfig;

@EventBusSubscriber(modid = RegistryCleanupTool.MODID)
public final class RegistryCleanupCommands {
    private RegistryCleanupCommands() {}
    public static void registerEventHandlers() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("rct")
            .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
            .then(Commands.literal("watch").then(Commands.literal("start").requires(s->s.hasPermission(3)).executes(ctx -> {RegistryCleanupTool.SERVICE.watcher().start(); msg(ctx.getSource(),"Watcher started."); return 1;}))
                .then(Commands.literal("stop").requires(s->s.hasPermission(3)).executes(ctx -> {RegistryCleanupTool.SERVICE.watcher().stop(); msg(ctx.getSource(),"Watcher stopped."); return 1;}))
                .then(Commands.literal("list").requires(s->s.hasPermission(2)).executes(ctx -> list(ctx.getSource()))))
            .then(Commands.literal("scan").then(Commands.literal("radius").requires(s->s.hasPermission(2)).then(Commands.argument("chunks", IntegerArgumentType.integer(0)).executes(ctx -> scanRadius(ctx.getSource(), IntegerArgumentType.getInteger(ctx,"chunks"))))))
            .then(Commands.literal("cleanlog").then(Commands.literal("radius").requires(s->s.hasPermission(3)).then(Commands.argument("chunks", IntegerArgumentType.integer(0)).executes(ctx -> cleanRadius(ctx.getSource(), IntegerArgumentType.getInteger(ctx,"chunks")))))
                .then(Commands.literal("captured").requires(s->s.hasPermission(3)).executes(ctx -> cleanCaptured(ctx.getSource()))))
            .then(Commands.literal("captured").then(Commands.literal("clear").requires(s->s.hasPermission(3)).executes(ctx->{RegistryCleanupTool.SERVICE.store().clear(); msg(ctx.getSource(),"Captured log results cleared."); return 1;}))));
    }

    private static int status(CommandSourceStack s){
        var st=RegistryCleanupTool.SERVICE;
        msg(s,"Watcher active: "+st.watcher().isActive());
        msg(s,"Replacement block: "+RegistryCleanupConfig.REPLACEMENT_BLOCK.get());
        msg(s,"Max radius: "+RegistryCleanupConfig.MAX_RADIUS.get());
        msg(s,"Captured missing IDs count: "+st.store().uniqueMissingIdsCount());
        msg(s,"Captured affected chunks count: "+st.store().uniqueChunksCount());
        msg(s,"Last cleanup summary: "+st.lastSummary());
        msg(s,"WARNING: Back up your world before cleanup.");
        return Command.SINGLE_SUCCESS;
    }
    private static int list(CommandSourceStack s){ RegistryCleanupTool.SERVICE.store().snapshot(s.getLevel().dimension()).forEach(o->msg(s,o.toString())); return 1; }
    private static int scanRadius(CommandSourceStack s, int radius){
        LogTriggeredCleanupService srv=RegistryCleanupTool.SERVICE;
        if(!srv.watcher().isActive()) srv.watcher().start();
        var scanned=srv.forceLoadRadius(s.getLevel(), new ChunkPos(s.getPosition()), radius, RegistryCleanupConfig.MAX_CHUNKS_PER_COMMAND.get());
        var occ=srv.store().snapshot(s.getLevel().dimension(), scanned);
        msg(s,"Chunks loaded/scanned: "+scanned.size());
        msg(s,"Missing block IDs: "+occ.stream().map(o->o.missingBlockId().toString()).distinct().count());
        msg(s,"Affected chunk sections: "+occ.stream().map(o->o.chunkX()+":"+o.sectionY()+":"+o.chunkZ()).distinct().count());
        return 1;
    }
    private static int cleanRadius(CommandSourceStack s, int radius){
        scanRadius(s,radius);
        return cleanCaptured(s);
    }
    private static int cleanCaptured(CommandSourceStack s){
        var occ=RegistryCleanupTool.SERVICE.store().snapshot(s.getLevel().dimension());
        var summary=RegistryCleanupTool.SERVICE.cleanCaptured(s.getLevel(), occ);
        msg(s,"chunks scanned: "+summary.chunksScanned());
        msg(s,"sections with missing IDs: "+summary.sectionsWithMissingIds());
        msg(s,"chunks patched: "+summary.chunksPatched());
        msg(s,"block palette entries replaced: "+summary.paletteEntriesReplaced());
        msg(s,"IDs replaced: "+summary.idsReplaced());
        msg(s,"chunks that could not be patched: "+summary.failedChunks());
        msg(s,"chunks that need unload/reload: "+summary.chunksNeedReload());
        msg(s,"Patched raw chunk NBT. Move away and unload/reload these chunks or restart the server if the old fallback blocks still appear.");
        return 1;
    }
    private static void msg(CommandSourceStack s,String m){ s.sendSuccess(()-> Component.literal(m), true); }
}
