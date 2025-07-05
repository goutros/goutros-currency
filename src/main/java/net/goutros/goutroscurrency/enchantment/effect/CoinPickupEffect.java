package net.goutros.goutroscurrency.enchantment.effect;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.goutros.goutroscurrency.core.ModEnchantmentComponents;
import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record CoinPickupEffect() {
   public static final Codec<CoinPickupEffect> CODEC = Codec.unit(new CoinPickupEffect());

   public static boolean hasPickupEnchantment(Player player) {
      if (!player.isCrouching()) {
         return false;
      } else {
         ItemStack pouch = ItemStack.EMPTY;
         if (player.getMainHandItem().getItem() instanceof CoinPouchItem) {
            pouch = player.getMainHandItem();
         } else if (player.getOffhandItem().getItem() instanceof CoinPouchItem) {
            pouch = player.getOffhandItem();
         }

         if (pouch.isEmpty()) {
            return false;
         } else {
            LootParams params = (new Builder((ServerLevel)player.level())).withParameter(LootContextParams.TOOL, pouch).withParameter(LootContextParams.ENCHANTMENT_LEVEL, 1).create(LootContextParamSets.ENCHANTED_ITEM);
            LootContext context = (new net.minecraft.world.level.storage.loot.LootContext.Builder(params)).withOptionalRandomSeed(player.getRandom().nextLong()).create(Optional.empty());
            AtomicBoolean found = new AtomicBoolean(false);
            EnchantmentHelper.runIterationOnItem(pouch, (holder, lvl) -> {
               List<ConditionalEffect<CoinPickupEffect>> effects = (List)((Enchantment)holder.value()).effects().get((DataComponentType)ModEnchantmentComponents.COIN_PICKUP.get());
               if (effects != null) {
                  Iterator var5 = effects.iterator();

                  while(var5.hasNext()) {
                     ConditionalEffect<CoinPickupEffect> effect = (ConditionalEffect)var5.next();
                     if (effect.matches(context)) {
                        found.set(true);
                        break;
                     }
                  }
               }

            });
            return found.get();
         }
      }
   }
}
