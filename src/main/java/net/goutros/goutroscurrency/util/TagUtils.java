package net.goutros.goutroscurrency.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import java.util.*;

public class TagUtils {
    public static List<Item> resolveTag(MinecraftServer server, String tagName) {
        String[] parts = tagName.split(":", 2);
        ResourceLocation tagId = (parts.length == 2)
                ? ResourceLocation.fromNamespaceAndPath(parts[0], parts[1])
                : ResourceLocation.fromNamespaceAndPath("minecraft", parts[0]);

        TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);

        List<Item> items = new ArrayList<>();
        var registry = server.registryAccess().registryOrThrow(BuiltInRegistries.ITEM.key());
        registry.getTag(tagKey).ifPresent(tag -> tag.forEach(holder -> items.add(holder.value())));
        return items;
    }
}
