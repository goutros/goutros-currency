package net.goutros.goutroscurrency.quest;

import java.util.Iterator;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class QuestUtils {
   public static int countMatchingItems(Inventory inventory, String materialId) {
      int count = 0;
      boolean isTag = materialId.startsWith("#");
      Iterator var4 = inventory.items.iterator();

      while(var4.hasNext()) {
         ItemStack stack = (ItemStack)var4.next();
         if (!stack.isEmpty()) {
            ResourceLocation itemId = stack.getItem().builtInRegistryHolder().key().location();
            if (!isTag) {
               if (itemId.toString().equals(materialId)) {
                  count += stack.getCount();
               }
            } else {
               String tagName = materialId.substring(1);
               ResourceLocation tagId = ResourceLocation.tryParse(tagName);
               if (tagId != null) {
                  TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                  if (stack.is(tagKey)) {
                     count += stack.getCount();
                  }
               }
            }
         }
      }

      return count;
   }

   public static String getEntityNameFor(String id) {
      EntityType<?> type = (EntityType)BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(id));
      return type != null ? type.getDescription().getString() : id;
   }

   public static String getPlayerNameFor(String uuidString) {
      try {
         UUID uuid = UUID.fromString(uuidString);
         String var10000 = uuid.toString();
         return "Player (" + var10000.substring(0, 8) + ")";
      } catch (IllegalArgumentException var2) {
         return "Unknown Player";
      }
   }

   public static String getItemNameFor(String materialId) {
      if (materialId == null) {
         return "Unknown";
      } else {
         boolean isTag = materialId.startsWith("#");
         if (!isTag) {
            Item item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(materialId));
            return item != null ? item.getDescription().getString() : "Invalid Item";
         } else {
            return "Any " + materialId.substring(1);
         }
      }
   }
}
