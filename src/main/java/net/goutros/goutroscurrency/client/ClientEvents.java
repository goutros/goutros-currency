package net.goutros.goutroscurrency.client;

import java.util.Iterator;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.goutros.goutroscurrency.network.CoinPouchSelectPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
import net.neoforged.neoforge.client.event.ScreenEvent.Render.Pre;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
   modid = "goutroscurrency",
   value = {Dist.CLIENT},
   bus = Bus.GAME
)
public class ClientEvents {
   private static boolean LAST_POUCH_OPEN = false;
   private static ItemStack lastSelectedItem;

   @SubscribeEvent
   public static void onInventoryRender(Pre event) {
      Minecraft mc = Minecraft.getInstance();
      Player player = mc.player;
      ModItemProperties.CURRENTLY_HOVERED_POUCH = ItemStack.EMPTY;
      ModItemProperties.CURRENTLY_HOVERED_SLOT = -1;
      ModItemProperties.CURRENT_SCREEN_ID = -1;
      boolean hoverPouch = false;
      int hoveredSlot = true;
      int hoveredScreenId = true;
      Screen var7 = event.getScreen();
      if (var7 instanceof AbstractContainerScreen) {
         AbstractContainerScreen<?> screen = (AbstractContainerScreen)var7;
         double mouseX = (double)event.getMouseX();
         double mouseY = (double)event.getMouseY();
         Slot slot = getHoveredSlot(screen, mouseX, mouseY);
         if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            if (stack.getItem() instanceof CoinPouchItem) {
               hoverPouch = true;
               int hoveredSlot = slot.index;
               int hoveredScreenId = screen.getMenu().containerId;
               ModItemProperties.CURRENTLY_HOVERED_POUCH = stack.copy();
               ModItemProperties.CURRENTLY_HOVERED_SLOT = hoveredSlot;
               ModItemProperties.CURRENT_SCREEN_ID = hoveredScreenId;
            }
         }
      }

      boolean inHand = false;
      if (player != null) {
         ItemStack mainHand = player.getMainHandItem();
         if (mainHand.getItem() instanceof CoinPouchItem) {
            inHand = true;
         }
      }

      boolean isOpenNow = inHand || hoverPouch;
      if (isOpenNow && !LAST_POUCH_OPEN) {
         playBundleOpenSound();
      } else if (!isOpenNow && LAST_POUCH_OPEN) {
         playBundleCloseSound();
      }

      LAST_POUCH_OPEN = isOpenNow;
   }

   @SubscribeEvent
   public static void onHotbarScroll(MouseScrollingEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         long window = mc.getWindow().getWindow();
         boolean shiftPressed = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
         if (shiftPressed) {
            if (mc.screen == null) {
               Player player = mc.player;
               ItemStack held = player.getMainHandItem();
               if (held.getItem() instanceof CoinPouchItem) {
                  sendSelectionPacket(player, held, event.getScrollDeltaY());
                  event.setCanceled(true);
               }
            }

         }
      }
   }

   @SubscribeEvent
   public static void onInventoryScroll(net.neoforged.neoforge.client.event.ScreenEvent.MouseScrolled.Pre event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         Screen var3 = event.getScreen();
         if (var3 instanceof AbstractContainerScreen) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen)var3;
            long window = mc.getWindow().getWindow();
            boolean shiftPressed = GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
            if (!shiftPressed) {
               return;
            }

            Slot hovered = getHoveredSlot(screen, event.getMouseX(), event.getMouseY());
            if (hovered != null && hovered.hasItem()) {
               ItemStack stack = hovered.getItem();
               if (stack.getItem() instanceof CoinPouchItem) {
                  sendSelectionPacket(mc.player, stack, event.getScrollDeltaY());
                  event.setCanceled(true);
               }
            }
         }

      }
   }

   @SubscribeEvent
   public static void onClientTick(Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null) {
         Iterator var2 = mc.player.getInventory().items.iterator();

         while(var2.hasNext()) {
            ItemStack stack = (ItemStack)var2.next();
            if (stack.getItem() instanceof CoinPouchItem && stack.has((DataComponentType)ModDataComponents.SCROLL_TIMER.get())) {
               int timer = (Integer)stack.get((DataComponentType)ModDataComponents.SCROLL_TIMER.get());
               if (timer > 0) {
                  stack.set((DataComponentType)ModDataComponents.SCROLL_TIMER.get(), timer - 1);
               } else if (timer == 0) {
                  stack.remove((DataComponentType)ModDataComponents.SCROLL_TIMER.get());
               }
            }
         }

         ItemStack current = mc.player.getInventory().getSelected();
         if (!ItemStack.matches(current, lastSelectedItem) && current.getItem() instanceof CoinPouchItem) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.7F, 1.2F, false);
         }

         lastSelectedItem = current.copy();
      }
   }

   private static void sendSelectionPacket(Player player, ItemStack stack, double scrollDelta) {
      int dir = scrollDelta > 0.0D ? 1 : -1;
      int before = CoinPouchItem.getSelectedCoinIndex(stack);
      CoinPouchItem.cycleSelectedCoin(stack, dir);
      int after = CoinPouchItem.getSelectedCoinIndex(stack);
      if (before != after) {
         int slot = getPlayerSlot(player, stack);
         if (slot >= 0) {
            CoinPouchSelectPacket pkt = new CoinPouchSelectPacket(slot, after);
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
               mc.getConnection().send(pkt.toVanillaServerbound());
            }
         }

         stack.set((DataComponentType)ModDataComponents.SCROLL_TIMER.get(), 10);
         int value = CoinPouchItem.COIN_VALUES[after];
         player.displayClientMessage(Component.literal("Withdrawal Amount: " + value).withStyle(ChatFormatting.YELLOW), true);
      }

   }

   private static int getPlayerSlot(Player player, ItemStack stack) {
      for(int i = 0; i < player.getInventory().getContainerSize(); ++i) {
         if (player.getInventory().getItem(i) == stack) {
            return i;
         }
      }

      return -1;
   }

   public static Slot getHoveredSlot(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
      Iterator var5 = screen.getMenu().slots.iterator();

      Slot slot;
      do {
         if (!var5.hasNext()) {
            return null;
         }

         slot = (Slot)var5.next();
      } while(!isMouseOverSlot(screen, slot, mouseX, mouseY));

      return slot;
   }

   public static boolean isMouseOverSlot(AbstractContainerScreen<?> screen, Slot slot, double mouseX, double mouseY) {
      int x = screen.getGuiLeft() + slot.x;
      int y = screen.getGuiTop() + slot.y;
      return mouseX >= (double)x && mouseX < (double)(x + 16) && mouseY >= (double)y && mouseY < (double)(y + 16);
   }

   private static void playBundleOpenSound() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         mc.player.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.7F, 1.0F);
      }

   }

   private static void playBundleCloseSound() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         mc.player.playSound(SoundEvents.BUNDLE_INSERT, 0.7F, 1.0F);
      }

   }

   static {
      lastSelectedItem = ItemStack.EMPTY;
   }
}
