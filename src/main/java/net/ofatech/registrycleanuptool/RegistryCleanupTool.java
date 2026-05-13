package net.ofatech.registrycleanuptool;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.ofatech.registrycleanuptool.cleanup.LogTriggeredCleanupService;
import net.ofatech.registrycleanuptool.command.RegistryCleanupCommands;
import net.ofatech.registrycleanuptool.config.RegistryCleanupConfig;
import net.ofatech.registrycleanuptool.discovery.MissingBlockCaptureStore;
import net.ofatech.registrycleanuptool.logging.ChunkSerializerLogWatcher;
import org.slf4j.Logger;

@Mod(RegistryCleanupTool.MODID)
public class RegistryCleanupTool {
    public static final String MODID = "registrycleanuptool";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final MissingBlockCaptureStore STORE = new MissingBlockCaptureStore();
    public static final LogTriggeredCleanupService SERVICE = new LogTriggeredCleanupService(STORE, new ChunkSerializerLogWatcher(STORE));
    private static boolean watcherStartedFromConfig;

    public RegistryCleanupTool(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RegistryCleanupConfig.SPEC);
        modEventBus.addListener(this::onConfigLoaded);
        modEventBus.addListener(this::onConfigReloaded);
        RegistryCleanupCommands.registerEventHandlers();
    }

    private void onConfigLoaded(ModConfigEvent.Loading event) {
        applyLoadedConfig(event);
    }

    private void onConfigReloaded(ModConfigEvent.Reloading event) {
        applyLoadedConfig(event);
    }

    private void applyLoadedConfig(ModConfigEvent event) {
        if (!MODID.equals(event.getConfig().getModId())) {
            return;
        }
        if (event.getConfig().getType() != ModConfig.Type.COMMON) {
            return;
        }

        if (RegistryCleanupConfig.AUTO_START_WATCHER.get()) {
            startWatcherOnce();
        }
    }

    private static synchronized void startWatcherOnce() {
        if (watcherStartedFromConfig || SERVICE.watcher().isActive()) {
            watcherStartedFromConfig = true;
            return;
        }
        SERVICE.watcher().start();
        watcherStartedFromConfig = true;
    }
}
