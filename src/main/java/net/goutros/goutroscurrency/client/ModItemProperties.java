package net.goutros.goutroscurrency.client;

import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.goutros.goutroscurrency.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModItemProperties {
   public static int CURRENTLY_HOVERED_SLOT = -1;
   public static int CURRENT_SCREEN_ID = -1;
   public static ItemStack CURRENTLY_HOVERED_POUCH;

   public static void registerAll() {
      ItemProperties.register((Item)ModItems.COIN_POUCH.get(), ResourceLocation.fromNamespaceAndPath("goutroscurrency", "coin_texture_index"), (stack, level, entity, seed) -> {
         int balance;
         boolean var10000;
         label53: {
            balance = (Integer)stack.getOrDefault((DataComponentType)ModDataComponents.COIN_BALANCE.get(), 0);
            if (entity instanceof Player) {
               Player player = (Player)entity;
               if (player.getMainHandItem() == stack) {
                  var10000 = true;
                  break label53;
               }
            }

            var10000 = false;
         }

         boolean isHeld = var10000;
         boolean scrolling = (Integer)stack.getOrDefault((DataComponentType)ModDataComponents.SCROLL_TIMER.get(), 0) > 0;
         boolean isHovered = false;
         Minecraft mc = Minecraft.getInstance();
         Screen patt0$temp = mc.screen;
         if (patt0$temp instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen)patt0$temp;
            if (CURRENT_SCREEN_ID == screen.getMenu().containerId && CURRENTLY_HOVERED_SLOT >= 0) {
               Slot slot = null;
               if (CURRENTLY_HOVERED_SLOT >= 0 && CURRENTLY_HOVERED_SLOT < screen.getMenu().slots.size()) {
                  slot = (Slot)screen.getMenu().slots.get(CURRENTLY_HOVERED_SLOT);
               }

               if (slot != null && !slot.getItem().isEmpty() && slot.getItem() == stack) {
                  isHovered = true;
               }
            }
         }

         if (!isHeld && !scrolling && !isHovered) {
            return -1.0F;
         } else if (balance == 0) {
            return -2.0F;
         } else {
            int selected = (Integer)stack.getOrDefault((DataComponentType)ModDataComponents.SELECTED_COIN.get(), 0);
            int value = CoinPouchItem.COIN_VALUES[Math.max(0, Math.min(selected, CoinPouchItem.COIN_VALUES.length - 1))];
            return balance >= value ? (float)selected : -2.0F;
         }
      });
   }

   static {
      CURRENTLY_HOVERED_POUCH = ItemStack.EMPTY;
   }
}
