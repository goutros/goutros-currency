package net.goutros.goutroscurrency.client;

import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.item.ItemProperties;
import net.goutros.goutroscurrency.GoutrosCurrency;
import net.goutros.goutroscurrency.item.ModItems;
import net.goutros.goutroscurrency.core.ModDataComponents;

import static net.goutros.goutroscurrency.GoutrosCurrency.MOD_ID;

public class ModItemProperties {

    public static int CURRENTLY_HOVERED_SLOT = -1;
    public static int CURRENT_SCREEN_ID = -1;
    public static ItemStack CURRENTLY_HOVERED_POUCH = ItemStack.EMPTY;

    public static void registerAll() {
        ItemProperties.register(
                ModItems.COIN_POUCH.get(),
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "coin_texture_index"),
                (stack, level, entity, seed) -> {
                    int balance = stack.getOrDefault(ModDataComponents.COIN_BALANCE.get(), 0);
                    boolean isHeld = entity instanceof Player player && player.getMainHandItem() == stack;
                    boolean scrolling = stack.getOrDefault(ModDataComponents.SCROLL_TIMER.get(), 0) > 0;

                    boolean isHovered = false;
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.screen instanceof AbstractContainerScreen<?> screen) {
                        if (ModItemProperties.CURRENT_SCREEN_ID == screen.getMenu().containerId
                                && ModItemProperties.CURRENTLY_HOVERED_SLOT >= 0) {
                            Slot slot = null;
                            if (ModItemProperties.CURRENTLY_HOVERED_SLOT >= 0 &&
                                    ModItemProperties.CURRENTLY_HOVERED_SLOT < screen.getMenu().slots.size()) {
                                slot = screen.getMenu().slots.get(ModItemProperties.CURRENTLY_HOVERED_SLOT);
                            }
                            if (slot != null && !slot.getItem().isEmpty()) {
                                // Must match BOTH slot and contents
                                if (slot.getItem() == stack) {
                                    isHovered = true;
                                }
                            }
                        }
                    }


                    if (isHeld || scrolling || isHovered) {
                        if (balance == 0) return -2f;

                        int selected = stack.getOrDefault(ModDataComponents.SELECTED_COIN.get(), 0);
                        int value = CoinPouchItem.COIN_VALUES[Math.max(0, Math.min(selected, CoinPouchItem.COIN_VALUES.length - 1))];
                        if (balance >= value) return (float) selected;
                        return -2f;
                    }
                    return -1f;
                }
        );

    }
}
