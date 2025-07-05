package net.goutros.goutroscurrency.mixin;

import java.util.Iterator;
import net.goutros.goutroscurrency.Config;
import net.goutros.goutroscurrency.item.ModItems;
import net.goutros.goutroscurrency.mixin.accessor.MerchantMenuAccessor;
import net.goutros.goutroscurrency.mixin.accessor.MerchantResultSlotAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MerchantResultSlot.class})
public abstract class MerchantResultSlotMixin {
   @Inject(
      method = {"onTake"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void goutroscurrency$onCounterfeitAttempt(Player player, ItemStack stack, CallbackInfo ci) {
      if (!player.level().isClientSide) {
         AbstractContainerMenu menu = player.containerMenu;
         if (menu != null && menu.slots.size() >= 2) {
            ItemStack slot0 = menu.getSlot(0).getItem();
            ItemStack slot1 = menu.getSlot(1).getItem();
            boolean usedFakeCash = slot0.is((Item)ModItems.COUNTERFEIT_CASH.get()) || slot1.is((Item)ModItems.COUNTERFEIT_CASH.get());
            if (usedFakeCash) {
               Merchant merchant = ((MerchantResultSlotAccessor)this).getMerchant();
               if (merchant != null) {
                  MerchantOffer offer = null;
                  if (menu instanceof MerchantMenu) {
                     MerchantMenu merchantMenu = (MerchantMenu)menu;
                     MerchantContainer container = ((MerchantMenuAccessor)merchantMenu).goutroscurrency$getTradeContainer();
                     offer = container.getActiveOffer();
                  }

                  if (offer.getUses() >= offer.getMaxUses()) {
                     ci.cancel();
                  } else {
                     float roll = player.getRandom().nextFloat();
                     if ((double)roll > Config.counterfeitSuccessChance) {
                        menu.setCarried(ItemStack.EMPTY);
                        ci.cancel();
                        int requiredAmount;
                        if (slot0.is((Item)ModItems.COUNTERFEIT_CASH.get())) {
                           requiredAmount = offer.getCostA().getCount();
                           slot0.shrink(requiredAmount);
                        } else if (slot1.is((Item)ModItems.COUNTERFEIT_CASH.get())) {
                           requiredAmount = offer.getCostB().getCount();
                           slot1.shrink(requiredAmount);
                        }

                        if (merchant instanceof AbstractVillager) {
                           AbstractVillager villager = (AbstractVillager)merchant;
                           villager.setUnhappyCounter(40);
                           villager.getBrain().setMemory(MemoryModuleType.ANGRY_AT, player.getUUID());
                           Iterator var12 = villager.getOffers().iterator();

                           while(var12.hasNext()) {
                              MerchantOffer offerItem = (MerchantOffer)var12.next();
                              offerItem.addToSpecialPriceDiff(5);
                           }

                           villager.level().playSound((Player)null, villager.getX(), villager.getY(), villager.getZ(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 1.0F);

                           for(int i = 0; i < 4; ++i) {
                              if (player instanceof ServerPlayer) {
                                 ServerPlayer serverPlayer = (ServerPlayer)player;
                                 serverPlayer.connection.send(new ClientboundLevelParticlesPacket(ParticleTypes.ANGRY_VILLAGER, true, villager.getX(), villager.getY() + 1.5D, villager.getZ(), 0.5F, 0.5F, 0.5F, 0.1F, 10));
                              }
                           }
                        }

                        player.closeContainer();
                     } else {
                        player.giveExperiencePoints(1);
                        player.level().playSound((Player)null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.2F);
                     }

                  }
               }
            }
         }
      }
   }
}
