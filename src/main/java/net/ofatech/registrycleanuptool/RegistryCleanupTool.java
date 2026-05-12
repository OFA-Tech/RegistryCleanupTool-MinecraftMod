package net.ofatech.registrycleanuptool;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.ofatech.registrycleanuptool.command.RegistryCleanupCommands;
import net.ofatech.registrycleanuptool.config.RegistryCleanupConfig;
import net.ofatech.registrycleanuptool.registry.PlaceholderRegistry;
import org.slf4j.Logger;

@Mod(RegistryCleanupTool.MODID)
public class RegistryCleanupTool {
    public static final String MODID = "registrycleanuptool";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RegistryCleanupTool(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RegistryCleanupConfig.SPEC);
        PlaceholderRegistry.register(modEventBus);
        RegistryCleanupCommands.registerEventHandlers();
    }
}
