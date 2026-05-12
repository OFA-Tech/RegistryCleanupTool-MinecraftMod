package net.ofatech.registrycleanuptool.util;

import net.minecraft.resources.ResourceLocation;

public final class ResourceLocationUtil {
    private ResourceLocationUtil() {}

    public static ResourceLocation parse(String id) {
        return ResourceLocation.tryParse(id);
    }
}
