package net.goutros.goutroscurrency.event;

import java.util.Iterator;
import net.goutros.goutroscurrency.Config;
import net.goutros.goutroscurrency.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

public class CounterfeitTradeHandler {
   @SubscribeEvent
   public static void onVillagerTrade(TradeWithVillagerEvent event) {
      Player player = event.getEntity();
      MerchantOffer offer = event.getMerchantOffer();
      AbstractVillager abstractVillager = event.getAbstractVillager();
      ItemStack inputA = offer.getCostA();
      ItemStack inputB = offer.getCostB();
      boolean usedCounterfeit = inputA.is((Item)ModItems.COUNTERFEIT_CASH.get()) || inputB.is((Item)ModItems.COUNTERFEIT_CASH.get());
      if (usedCounterfeit) {
         float chance = player.getRandom().nextFloat();
         if ((double)chance > Config.counterfeitSuccessChance) {
            if (!player.getInventory().add(new ItemStack((ItemLike)ModItems.COUNTERFEIT_CASH.get(), inputA.getCount()))) {
               player.drop(new ItemStack((ItemLike)ModItems.COUNTERFEIT_CASH.get(), inputA.getCount()), false);
            }

            ItemStack result = offer.getResult();
            player.getInventory().removeItem(result);
            if (abstractVillager instanceof Villager) {
               Villager villager = (Villager)abstractVillager;
               villager.getBrain().setMemory(MemoryModuleType.ANGRY_AT, player.getUUID());
               villager.setUnhappyCounter(40);
               Iterator var10 = villager.getOffers().iterator();

               while(var10.hasNext()) {
                  MerchantOffer otherOffer = (MerchantOffer)var10.next();
                  otherOffer.addToSpecialPriceDiff(5);
               }

               villager.level().playSound((Player)null, villager.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            player.sendSystemMessage(Component.literal("The villager caught your COUNTERFEIT cash! They're furious!"));
         } else {
            player.sendSystemMessage(Component.literal("You slipped them the fake cash without a hitch..."));
            player.giveExperiencePoints(1);
            player.level().playSound((Player)null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
         }

      }
   }
}
