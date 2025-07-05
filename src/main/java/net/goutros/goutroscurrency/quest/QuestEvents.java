package net.goutros.goutroscurrency.quest;

import java.util.Iterator;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

@EventBusSubscriber(
   modid = "goutroscurrency",
   bus = Bus.GAME
)
public class QuestEvents {
   @SubscribeEvent
   public static void onPlayerTick(Post event) {
      Player player = event.getEntity();
      if (!player.level().isClientSide) {
         for(int i = 0; i < player.getInventory().items.size(); ++i) {
            ItemStack stack = (ItemStack)player.getInventory().items.get(i);
            Quest quest = (Quest)stack.get(ModDataComponents.QUEST_DATA);
            if (quest != null && quest.type.equals("walk") && !quest.isComplete()) {
               BlockPos lastPos = (BlockPos)stack.get(ModDataComponents.LAST_WALK_BLOCKPOS);
               BlockPos currentPos = player.blockPosition();
               if (lastPos == null) {
                  stack.set(ModDataComponents.LAST_WALK_BLOCKPOS, currentPos);
               } else {
                  int dx = currentPos.getX() - lastPos.getX();
                  int dz = currentPos.getZ() - lastPos.getZ();
                  int blockDistance = Math.abs(dx) + Math.abs(dz);
                  if (blockDistance != 0) {
                     int newProgress = quest.progress + blockDistance;
                     ItemStack newStack = stack.copy();
                     newStack.set(ModDataComponents.LAST_WALK_BLOCKPOS, currentPos);
                     if (newProgress >= quest.amount) {
                        Quest completed = quest.copyWithProgress(quest.amount).copyWithCompletion(true);
                        newStack.set(ModDataComponents.QUEST_DATA, completed);
                        QuestManager.giveCoins(player, completed.coinReward);
                        player.displayClientMessage(Component.literal("Quest Complete! +" + completed.coinReward + " Coins").withStyle(ChatFormatting.GREEN), true);
                        newStack.shrink(1);
                        player.getInventory().setItem(i, newStack);
                     } else {
                        newStack.set(ModDataComponents.QUEST_DATA, quest.copyWithProgress(newProgress));
                        player.getInventory().setItem(i, newStack);
                     }
                  }
               }
            }
         }

      }
   }

   @SubscribeEvent
   public static void onEntityKilled(LivingDeathEvent event) {
      Entity var2 = event.getSource().getEntity();
      if (var2 instanceof Player) {
         Player killer = (Player)var2;
         if (!killer.level().isClientSide) {
            LivingEntity var4 = event.getEntity();
            String killedId;
            if (var4 instanceof Player) {
               Player killedPlayer = (Player)var4;
               killedId = killedPlayer.getUUID().toString();
            } else {
               killedId = event.getEntity().getType().builtInRegistryHolder().key().location().toString();
            }

            Iterator var6 = killer.getInventory().items.iterator();

            while(var6.hasNext()) {
               ItemStack stack = (ItemStack)var6.next();
               if (!stack.isEmpty() && stack.has(ModDataComponents.QUEST_DATA)) {
                  stack.set(ModDataComponents.LAST_KILL_ID, killedId);
               }
            }

         }
      }
   }
}
