package net.goutros.goutroscurrency.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class TagUtils {
   public static List<Item> resolveTag(MinecraftServer server, String tagName) {
      String[] parts = tagName.split(":", 2);
      ResourceLocation tagId = parts.length == 2 ? ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]) : ResourceLocation.fromNamespaceAndPath("minecraft", parts[0]);
      TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
      List<Item> items = new ArrayList();
      Registry<Item> registry = server.registryAccess().registryOrThrow(BuiltInRegistries.ITEM.key());
      registry.getTag(tagKey).ifPresent((tag) -> {
         tag.forEach((holder) -> {
            items.add((Item)holder.value());
         });
      });
      return items;
   }
}
