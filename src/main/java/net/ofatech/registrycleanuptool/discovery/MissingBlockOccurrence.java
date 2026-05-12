package net.ofatech.registrycleanuptool.discovery;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record MissingBlockOccurrence(ResourceKey<Level> dimension, int chunkX, int sectionY, int chunkZ, ResourceLocation missingBlockId) {}
