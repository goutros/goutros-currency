package net.goutros.goutroscurrency.item;

import java.util.List;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.quest.Quest;
import net.goutros.goutroscurrency.quest.QuestManager;
import net.goutros.goutroscurrency.quest.QuestUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

public class QuestNoteItem extends Item {
   public QuestNoteItem(Properties props) {
      super(props);
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
      if (!level.isClientSide && entity instanceof Player) {
         Player player = (Player)entity;
         Quest quest = (Quest)stack.get(ModDataComponents.QUEST_DATA);
         if (quest != null && !quest.isComplete()) {
            Quest completed;
            if (quest.type.equals("gather")) {
               int count = QuestUtils.countMatchingItems(player.getInventory(), quest.targetId);
               if (count >= quest.amount) {
                  completed = quest.copyWithProgress(quest.amount).copyWithCompletion(true);
                  stack.set(ModDataComponents.QUEST_DATA, completed);
                  if (quest.isComplete()) {
                     return;
                  }

                  QuestManager.giveCoins(player, completed.coinReward);
                  player.displayClientMessage(Component.literal("Quest Complete! +" + completed.coinReward + " Coins").withStyle(ChatFormatting.GREEN), true);
                  stack.shrink(1);
               } else if (count != quest.progress) {
                  stack.set(ModDataComponents.QUEST_DATA, quest.copyWithProgress(count));
               }
            }

            String killedId;
            if (quest.type.equals("kill")) {
               if (!stack.has(ModDataComponents.LAST_KILL_ID)) {
                  return;
               }

               killedId = (String)stack.get(ModDataComponents.LAST_KILL_ID);
               stack.remove(ModDataComponents.LAST_KILL_ID);
               if (killedId != null && killedId.equals(quest.targetId)) {
                  int newProgress = quest.progress + 1;
                  if (newProgress >= quest.amount) {
                     Quest completed = quest.copyWithProgress(quest.amount).copyWithCompletion(true);
                     stack.set(ModDataComponents.QUEST_DATA, completed);
                     if (quest.isComplete()) {
                        return;
                     }

                     QuestManager.giveCoins(player, completed.coinReward);
                     player.displayClientMessage(Component.literal("Quest Complete! +" + completed.coinReward + " Coins").withStyle(ChatFormatting.GREEN), true);
                     stack.shrink(1);
                  } else {
                     stack.set(ModDataComponents.QUEST_DATA, quest.copyWithProgress(newProgress));
                  }
               }
            }

            if (quest.type.equals("hunt")) {
               if (!stack.has(ModDataComponents.LAST_KILL_ID)) {
                  return;
               }

               killedId = (String)stack.get(ModDataComponents.LAST_KILL_ID);
               stack.remove(ModDataComponents.LAST_KILL_ID);
               if (killedId != null && killedId.equals(quest.targetId)) {
                  completed = quest.copyWithProgress(quest.amount).copyWithCompletion(true);
                  stack.set(ModDataComponents.QUEST_DATA, completed);
                  if (quest.isComplete()) {
                     return;
                  }

                  QuestManager.giveCoins(player, completed.coinReward);
                  player.displayClientMessage(Component.literal("Quest Complete! +" + completed.coinReward + " Coins").withStyle(ChatFormatting.GREEN), true);
                  stack.shrink(1);
               }
            }

         }
      }
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      Quest quest = (Quest)stack.get(ModDataComponents.QUEST_DATA);
      if (quest == null) {
         tooltip.add(Component.literal("Invalid Quest").withStyle(ChatFormatting.RED));
      } else if (!Screen.hasShiftDown()) {
         tooltip.add(Component.literal("Hold [").append(Component.literal("Shift").withStyle(ChatFormatting.GRAY)).append("] for Quest Details").withStyle(ChatFormatting.DARK_GRAY));
      } else {
         tooltip.add(Component.literal("Hold [").append(Component.literal("Shift").withStyle(ChatFormatting.WHITE)).append("] for Quest Details").withStyle(ChatFormatting.DARK_GRAY));
         tooltip.add(Component.empty());
         tooltip.add(Component.literal("A note with a task yet to complete!").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.empty());
         String var6 = quest.type;
         byte var7 = -1;
         switch(var6.hashCode()) {
         case -1253024261:
            if (var6.equals("gather")) {
               var7 = 0;
            }
            break;
         case 3214227:
            if (var6.equals("hunt")) {
               var7 = 2;
            }
            break;
         case 3291998:
            if (var6.equals("kill")) {
               var7 = 1;
            }
            break;
         case 3641801:
            if (var6.equals("walk")) {
               var7 = 3;
            }
         }

         String name;
         switch(var7) {
         case 0:
            int var10001 = quest.amount;
            tooltip.add(Component.literal("Collect " + var10001 + "x " + QuestUtils.getItemNameFor(quest.targetId)).withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal(" Progress: " + quest.progress + " / " + quest.amount).withStyle(ChatFormatting.GRAY));
            break;
         case 1:
            name = QuestUtils.getEntityNameFor(quest.targetId);
            tooltip.add(Component.literal("Defeat " + quest.amount + "x " + name).withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal(" Kills: " + quest.progress + " / " + quest.amount).withStyle(ChatFormatting.GRAY));
            break;
         case 2:
            name = quest.playerName != null ? quest.playerName : QuestUtils.getPlayerNameFor(quest.targetId);
            tooltip.add(Component.literal("Eliminate " + name).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal(" Target: " + name).withStyle(ChatFormatting.DARK_GRAY));
            break;
         case 3:
            tooltip.add(Component.literal("Travel " + quest.amount + " blocks").withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(Component.literal(" Distance: " + quest.progress + " / " + quest.amount).withStyle(ChatFormatting.GRAY));
            break;
         default:
            tooltip.add(Component.literal("Unknown quest type").withStyle(ChatFormatting.DARK_RED));
         }

         tooltip.add(Component.empty());
         tooltip.add(Component.literal("When Completed").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.literal(" Reward: " + quest.coinReward + " Coins").withStyle(ChatFormatting.AQUA));
      }
   }

   public boolean isFoil(ItemStack stack) {
      Quest quest = (Quest)stack.get(ModDataComponents.QUEST_DATA);
      return quest != null && quest.isComplete();
   }

   public static void giveCoins(Player player, int totalCoins) {
      if (totalCoins > 0) {
         int[] COIN_VALUES = new int[]{100, 25, 10, 5, 1};
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

      }
   }
}
