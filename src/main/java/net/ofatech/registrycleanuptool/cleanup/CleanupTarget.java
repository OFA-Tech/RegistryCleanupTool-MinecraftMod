package net.ofatech.registrycleanuptool.cleanup;

import net.minecraft.resources.ResourceLocation;

public record CleanupTarget(ResourceLocation id, boolean registered, boolean placeholder) {}
