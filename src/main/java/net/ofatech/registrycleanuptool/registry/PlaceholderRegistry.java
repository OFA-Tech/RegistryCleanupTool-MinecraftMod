package net.ofatech.registrycleanuptool.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.ofatech.registrycleanuptool.RegistryCleanupTool;
import net.ofatech.registrycleanuptool.config.RegistryCleanupConfig;

@EventBusSubscriber(modid = RegistryCleanupTool.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class PlaceholderRegistry {
    public static final List<ResourceLocation> DEFAULT_PLACEHOLDER_BLOCK_IDS = List.of(
            ResourceLocation.fromNamespaceAndPath("dwm", "titanium_ore"),
            ResourceLocation.fromNamespaceAndPath("rftoolsbase", "dimensionalshard_overworld")
    );
    private static final Map<ResourceLocation, Block> PLACEHOLDERS = new HashMap<>();

    private PlaceholderRegistry() {}
    public static void register(IEventBus bus) {}

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.BLOCK) || !RegistryCleanupConfig.INCLUDE_KNOWN_PLACEHOLDER_BLOCKS.get()) return;
        for (ResourceLocation id : DEFAULT_PLACEHOLDER_BLOCK_IDS) {
            if (PLACEHOLDERS.containsKey(id)) continue;
            Block block = new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(0.1F).sound(SoundType.STONE).noLootTable());
            event.register(Registries.BLOCK, id, () -> block);
            PLACEHOLDERS.put(id, block);
            RegistryCleanupTool.LOGGER.info("Registered placeholder block {}", id);
        }
    }

    public static boolean isPlaceholder(ResourceLocation id) { return PLACEHOLDERS.containsKey(id); }
}
