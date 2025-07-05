package net.goutros.goutroscurrency.quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.goutros.goutroscurrency.data.QuestPresetReloadListener;
import net.goutros.goutroscurrency.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class QuestManager {
   private static final boolean DEBUG = true;

   public static Quest generateRandomGatherQuest(MinecraftServer server, RandomSource random) {
      System.out.println("[DEBUG] Generating GATHER quest...");
      List<QuestPreset> presets = (new ArrayList(QuestPresetReloadListener.LOADED_PRESETS.values())).stream().filter(QuestPreset::isGather).toList();
      if (presets.isEmpty()) {
         System.out.println("[DEBUG] No gather presets found!");
         return null;
      } else {
         QuestPreset preset = (QuestPreset)presets.get(random.nextInt(presets.size()));
         System.out.println("[DEBUG] Chosen preset: " + String.valueOf(preset));
         Set<String> candidates = new HashSet();
         Iterator var5 = preset.whitelistItems.iterator();

         String chosen;
         ResourceLocation tagId;
         while(var5.hasNext()) {
            chosen = (String)var5.next();
            tagId = ResourceLocation.tryParse(chosen);
            if (tagId != null) {
               Item item = (Item)BuiltInRegistries.ITEM.get(tagId);
               if (item != Items.AIR) {
                  candidates.add(tagId.toString());
                  System.out.println("[DEBUG] Whitelisted item added: " + String.valueOf(tagId));
               }
            }
         }

         var5 = preset.whitelistTags.iterator();

         while(var5.hasNext()) {
            chosen = (String)var5.next();
            tagId = ResourceLocation.tryParse(chosen);
            if (tagId != null) {
               TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
               server.registryAccess().registryOrThrow(Registries.ITEM).getTag(tagKey).ifPresent((holderSet) -> {
                  Iterator var2 = holderSet.iterator();

                  while(var2.hasNext()) {
                     Holder<Item> holder = (Holder)var2.next();
                     holder.unwrapKey().ifPresent((key) -> {
                        ResourceLocation loc = key.location();
                        Item item = (Item)BuiltInRegistries.ITEM.get(loc);
                        if (item != Items.AIR) {
                           candidates.add(loc.toString());
                           System.out.println("[DEBUG] Whitelisted tag item added: " + String.valueOf(loc));
                        }

                     });
                  }

               });
            }
         }

         candidates.removeIf((id) -> {
            if (preset.blacklistItems.contains(id)) {
               System.out.println("[DEBUG] Removing blacklisted item: " + id);
               return true;
            } else {
               ResourceLocation loc = ResourceLocation.tryParse(id);
               if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                  Item item = (Item)BuiltInRegistries.ITEM.get(loc);
                  if (item == Items.AIR) {
                     System.out.println("[DEBUG] Removing AIR: " + id);
                     return true;
                  } else {
                     Iterator var4 = preset.blacklistTags.iterator();

                     while(var4.hasNext()) {
                        String tag = (String)var4.next();
                        ResourceLocation tagId = ResourceLocation.tryParse(tag);
                        if (tagId != null) {
                           TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                           if (item.builtInRegistryHolder().is(tagKey)) {
                              System.out.println("[DEBUG] Removing item " + id + " due to blacklisted tag: " + tag);
                              return true;
                           }
                        }
                     }

                     return false;
                  }
               } else {
                  return true;
               }
            }
         });
         if (candidates.isEmpty()) {
            System.out.println("[DEBUG] No valid candidates after filtering!");
            return null;
         } else {
            List<String> candidateList = new ArrayList(candidates);
            chosen = (String)candidateList.get(random.nextInt(candidateList.size()));
            int amount = preset.amountMin + random.nextInt(preset.amountMax - preset.amountMin + 1);
            int coins = preset.coinMin + random.nextInt(preset.coinMax - preset.coinMin + 1);
            String desc = "Collect " + amount + "x " + QuestUtils.getItemNameFor(chosen);
            System.out.println("[DEBUG] Final chosen item: " + chosen);
            System.out.println("[DEBUG] Amount: " + amount + ", Coins: " + coins);
            System.out.println("[DEBUG] Description: " + desc);
            return new Quest(preset.type, chosen, amount, desc, coins, 0, UUID.randomUUID(), false);
         }
      }
   }

   public static Quest generateRandomKillQuest(MinecraftServer server, RandomSource random) {
      System.out.println("[DEBUG] Generating KILL quest...");
      List<QuestPreset> presets = (new ArrayList(QuestPresetReloadListener.LOADED_PRESETS.values())).stream().filter(QuestPreset::isKill).toList();
      if (presets.isEmpty()) {
         System.out.println("[DEBUG] No kill presets found!");
         return null;
      } else {
         QuestPreset preset = (QuestPreset)presets.get(random.nextInt(presets.size()));
         System.out.println("[DEBUG] Chosen preset: " + String.valueOf(preset));
         List<String> candidates = new ArrayList(preset.whitelistEntities);
         List var10001 = preset.blacklistEntities;
         Objects.requireNonNull(var10001);
         candidates.removeIf(var10001::contains);
         if (candidates.isEmpty()) {
            System.out.println("[DEBUG] No valid entities after filtering!");
            return null;
         } else {
            String chosen = (String)candidates.get(random.nextInt(candidates.size()));
            int amount = preset.amountMin + random.nextInt(preset.amountMax - preset.amountMin + 1);
            int coins = preset.coinMin + random.nextInt(preset.coinMax - preset.coinMin + 1);
            System.out.println("[DEBUG] Final chosen entity: " + chosen);
            System.out.println("[DEBUG] Amount: " + amount + ", Coins: " + coins);
            return new Quest(preset.type, chosen, amount, "Defeat " + amount + "x " + chosen, coins, 0, UUID.randomUUID(), false);
         }
      }
   }

   public static Quest generateRandomWalkQuest(MinecraftServer server, RandomSource random) {
      System.out.println("[DEBUG] Generating WALK quest...");
      List<QuestPreset> presets = (new ArrayList(QuestPresetReloadListener.LOADED_PRESETS.values())).stream().filter(QuestPreset::isWalk).toList();
      if (presets.isEmpty()) {
         System.out.println("[DEBUG] No walk presets found!");
         return null;
      } else {
         QuestPreset preset = (QuestPreset)presets.get(random.nextInt(presets.size()));
         int range = Math.max(1, preset.distanceMax - preset.distanceMin + 1);
         int distance = preset.distanceMin + random.nextInt(range);
         System.out.println("[DEBUG] Walk preset: min=" + preset.distanceMin + ", max=" + preset.distanceMax);
         int coins = preset.coinMin + random.nextInt(preset.coinMax - preset.coinMin + 1);
         System.out.println("[DEBUG] Distance: " + distance + ", Coins: " + coins);
         return new Quest(preset.type, "walk", distance, "Travel " + distance + " blocks by foot", coins, 0, UUID.randomUUID(), false);
      }
   }

   public static Quest generateRandomHuntQuest(MinecraftServer server, ServerPlayer self, RandomSource random) {
      if (self == null) {
         return null;
      } else {
         List<QuestPreset> presets = QuestPresetReloadListener.LOADED_PRESETS.values().stream().filter(QuestPreset::isHunt).toList();
         if (presets.isEmpty()) {
            return null;
         } else {
            List<ServerPlayer> otherPlayers = server.getPlayerList().getPlayers().stream().filter((p) -> {
               return !p.getUUID().equals(self.getUUID());
            }).toList();
            if (otherPlayers.isEmpty()) {
               return null;
            } else {
               ServerPlayer target = (ServerPlayer)otherPlayers.get(random.nextInt(otherPlayers.size()));
               QuestPreset preset = (QuestPreset)presets.get(random.nextInt(presets.size()));
               int coins = preset.coinMin + random.nextInt(preset.coinMax - preset.coinMin + 1);
               return new Quest("hunt", target.getUUID().toString(), 1, "Eliminate player: " + target.getGameProfile().getName(), coins, 0, UUID.randomUUID(), false, target.getGameProfile().getName());
            }
         }
      }
   }

   public static Quest generateRandomQuest(MinecraftServer server, ServerPlayer player, RandomSource random) {
      List<String> types = new ArrayList();
      List<QuestPreset> allPresets = new ArrayList(QuestPresetReloadListener.LOADED_PRESETS.values());
      if (allPresets.stream().anyMatch(QuestPreset::isGather)) {
         types.add("gather");
      }

      if (allPresets.stream().anyMatch(QuestPreset::isKill)) {
         types.add("kill");
      }

      if (allPresets.stream().anyMatch(QuestPreset::isWalk)) {
         types.add("walk");
      }

      if (player != null && allPresets.stream().anyMatch(QuestPreset::isHunt)) {
         types.add("hunt");
      }

      if (types.isEmpty()) {
         return null;
      } else {
         String chosenType = (String)types.get(random.nextInt(types.size()));
         byte var7 = -1;
         switch(chosenType.hashCode()) {
         case -1253024261:
            if (chosenType.equals("gather")) {
               var7 = 0;
            }
            break;
         case 3214227:
            if (chosenType.equals("hunt")) {
               var7 = 3;
            }
            break;
         case 3291998:
            if (chosenType.equals("kill")) {
               var7 = 1;
            }
            break;
         case 3641801:
            if (chosenType.equals("walk")) {
               var7 = 2;
            }
         }

         Quest var10000;
         switch(var7) {
         case 0:
            var10000 = generateRandomGatherQuest(server, random);
            break;
         case 1:
            var10000 = generateRandomKillQuest(server, random);
            break;
         case 2:
            var10000 = generateRandomWalkQuest(server, random);
            break;
         case 3:
            var10000 = generateRandomHuntQuest(server, player, random);
            break;
         default:
            var10000 = null;
         }

         return var10000;
      }
   }

   public static Quest generateRandomQuest(MinecraftServer server, RandomSource random) {
      return generateRandomQuest(server, (ServerPlayer)null, random);
   }

   public static void giveCoins(Player player, int totalCoins) {
      if (totalCoins > 0) {
         int[] COIN_VALUES = new int[]{100, 50, 20, 10, 1};
         Item[] COIN_ITEMS = new Item[]{(Item)ModItems.NETHERITE_COIN.get(), (Item)ModItems.DIAMOND_COIN.get(), (Item)ModItems.GOLD_COIN.get(), (Item)ModItems.IRON_COIN.get(), (Item)ModItems.COPPER_COIN.get()};
         Level level = player.level();
         double x = player.getX();
         double y = player.getY() + 0.5D;
         double z = player.getZ();

         for(int i = 0; i < COIN_VALUES.length; ++i) {
            int denom = COIN_VALUES[i];
            int count = totalCoins / denom;
            if (count > 0) {
               totalCoins -= count * denom;
               ItemStack stack = new ItemStack(COIN_ITEMS[i], count);
               ItemEntity drop = new ItemEntity(level, x, y, z, stack);
               drop.setPickUpDelay(10);
               level.addFreshEntity(drop);
            }
         }

         level.playSound((Player)null, x, y, z, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.2F);
      }
   }
}
