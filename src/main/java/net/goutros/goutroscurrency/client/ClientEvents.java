package net.goutros.goutroscurrency.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.goutros.goutroscurrency.block.ModBlockEntities;
import net.goutros.goutroscurrency.block.QuestBoardBlock;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.goutros.goutroscurrency.network.CoinPouchSelectPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.lwjgl.glfw.GLFW;

import static net.goutros.goutroscurrency.GoutrosCurrency.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {

    // Track last hovered pouch slot ID and screen ID
    private static boolean LAST_POUCH_OPEN = false;
    private static int LAST_OPEN_SCREEN_ID = -1;
    private static int LAST_OPEN_SLOT = -1;
    private static boolean LAST_OPEN_IN_HAND = false;

    // Updates hover state every frame in inventory screens
    @SubscribeEvent
    public static void onInventoryRender(ScreenEvent.Render.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        // --- Step 1: Always update currently hovered pouch info ---
        ModItemProperties.CURRENTLY_HOVERED_POUCH = ItemStack.EMPTY;
        ModItemProperties.CURRENTLY_HOVERED_SLOT = -1;
        ModItemProperties.CURRENT_SCREEN_ID = -1;

        boolean hoverPouch = false;
        int hoveredSlot = -1;
        int hoveredScreenId = -1;

        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            Slot slot = getHoveredSlot(screen, mouseX, mouseY);

            if (slot != null && slot.hasItem()) {
                ItemStack stack = slot.getItem();
                if (stack.getItem() instanceof CoinPouchItem) {
                    hoverPouch = true;
                    hoveredSlot = slot.index;
                    hoveredScreenId = screen.getMenu().containerId;

                    // --- Always update hover info! ---
                    ModItemProperties.CURRENTLY_HOVERED_POUCH = stack.copy();
                    ModItemProperties.CURRENTLY_HOVERED_SLOT = hoveredSlot;
                    ModItemProperties.CURRENT_SCREEN_ID = hoveredScreenId;
                }
            }
        }

        // --- Step 2: Calculate overall open state (held in hand or hovered) for SFX ---
        boolean inHand = false;
        if (player != null) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof CoinPouchItem) {
                inHand = true;
            }
        }
        boolean isOpenNow = inHand || hoverPouch;

        // --- Step 3: Play SFX on state transition (just like bundle) ---
        if (isOpenNow && !LAST_POUCH_OPEN) {
            playBundleOpenSound();
        } else if (!isOpenNow && LAST_POUCH_OPEN) {
            playBundleCloseSound();
        }
        LAST_POUCH_OPEN = isOpenNow;
    }

    // Hotbar scroll for coin pouch selection (when no GUI is open)
    @SubscribeEvent
    public static void onHotbarScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        long window = mc.getWindow().getWindow();
        boolean shiftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!shiftPressed) return;

        // Only allow in-hand scroll when NO GUI is open
        if (mc.screen == null) {
            Player player = mc.player;
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof CoinPouchItem) {
                sendSelectionPacket(player, held, event.getScrollDeltaY());
                event.setCanceled(true);
            }
        }
    }

    // Inventory screen scroll to change coin type
    @SubscribeEvent
    public static void onInventoryScroll(ScreenEvent.MouseScrolled.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            long window = mc.getWindow().getWindow();
            boolean shiftPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (!shiftPressed) return;

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

    // Decrements SCROLL_TIMER on all coin pouches in the player's inventory
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        for (ItemStack stack : mc.player.getInventory().items) {
            if (stack.getItem() instanceof CoinPouchItem && stack.has(ModDataComponents.SCROLL_TIMER.get())) {
                int timer = stack.get(ModDataComponents.SCROLL_TIMER.get());
                if (timer > 0) {
                    stack.set(ModDataComponents.SCROLL_TIMER.get(), timer - 1);
                } else if (timer == 0) {
                    // Optionally remove the tag for cleanliness
                    stack.remove(ModDataComponents.SCROLL_TIMER.get());
                }
            }
        }
    }

    // Sends a packet to change selected coin denomination
    private static void sendSelectionPacket(Player player, ItemStack stack, double scrollDelta) {
        int dir = scrollDelta > 0 ? 1 : -1;
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
            stack.set(ModDataComponents.SCROLL_TIMER.get(), 10); // Use 10 ticks for a slightly longer effect
            int value = CoinPouchItem.COIN_VALUES[after];
            player.displayClientMessage(Component.literal("Selected: " + value + "-coin"), true);
        }
    }

    // Returns the slot index of an itemstack in the player's inventory, or -1 if not found
    private static int getPlayerSlot(Player player, ItemStack stack) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i) == stack) return i;
        }
        return -1;
    }

    // Returns the Slot under the mouse, or null if none
    public static Slot getHoveredSlot(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        for (Slot slot : screen.getMenu().slots) {
            if (isMouseOverSlot(screen, slot, mouseX, mouseY)) {
                return slot;
            }
        }
        return null;
    }

    // Returns true if mouse is over the slot (16x16 px bounding box)
    public static boolean isMouseOverSlot(AbstractContainerScreen<?> screen, Slot slot, double mouseX, double mouseY) {
        int x = screen.getGuiLeft() + slot.x;
        int y = screen.getGuiTop() + slot.y;
        return mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16;
    }

    private static void playBundleOpenSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.7f, 1.0f);
        }
    }

    private static void playBundleCloseSound() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.BUNDLE_INSERT, 0.7f, 1.0f);
        }
    }
}