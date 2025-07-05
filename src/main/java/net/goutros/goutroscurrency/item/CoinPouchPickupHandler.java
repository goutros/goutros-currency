package net.goutros.goutroscurrency.item;

import java.util.Iterator;
import java.util.List;
import net.goutros.goutroscurrency.core.ModSounds;
import net.goutros.goutroscurrency.enchantment.effect.CoinPickupEffect;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

public class CoinPouchPickupHandler {
   @SubscribeEvent
   public static void onPlayerTick(Post event) {
      Player player = event.getEntity();
      Level level = player.level();
      if (!level.isClientSide) {
         ItemStack pouch = player.getMainHandItem();
         if (pouch.getItem() instanceof CoinPouchItem) {
            if (CoinPickupEffect.hasPickupEnchantment(player)) {
               List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2.5D));
               Iterator var5 = nearby.iterator();

               while(var5.hasNext()) {
                  ItemEntity item = (ItemEntity)var5.next();
                  ItemStack stack = item.getItem();
                  int value = CoinPouchItem.getCoinValue(stack);
                  if (value > 0) {
                     int amount = stack.getCount();
                     int total = value * amount;
                     CoinPouchItem.setBalance(pouch, CoinPouchItem.getBalance(pouch) + total);
                     CoinPouchItem.setSelectedCoinIndex(pouch, CoinPouchItem.getCoinIndex(stack));
                     item.discard();
                     level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.COIN_POUCH_INSERT.get(), SoundSource.PLAYERS, 1.0F, 1.2F);
                     break;
                  }
               }
            }

         }
      }
   }
}
