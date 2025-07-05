package net.goutros.goutroscurrency.mixin;

import net.goutros.goutroscurrency.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ItemCost.class})
public class ItemCostMixin {
   @Inject(
      method = {"test"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void allowCounterfeit(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
      ItemCost self = (ItemCost)this;
      ItemStack expected = self.itemStack();
      if (stack.is((Item)ModItems.COUNTERFEIT_CASH.get()) && expected.is(Items.EMERALD)) {
         cir.setReturnValue(true);
      }

   }
}
