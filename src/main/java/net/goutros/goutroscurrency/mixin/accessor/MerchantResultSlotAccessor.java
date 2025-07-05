package net.goutros.goutroscurrency.mixin.accessor;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({MerchantResultSlot.class})
public interface MerchantResultSlotAccessor {
   @Accessor("merchant")
   Merchant getMerchant();

   @Accessor("slots")
   MerchantContainer getSlots();
}
