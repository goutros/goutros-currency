package net.goutros.goutroscurrency.item;

import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.core.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;

import java.util.List;

public class CoinPouchItem extends Item {

    // Coin values and items must match order!
    public static final int[] COIN_VALUES = {100, 50, 20, 10, 1};
    public static final Item[] COIN_ITEMS = {
            ModItems.NETHERITE_COIN.get(),
            ModItems.DIAMOND_COIN.get(),
            ModItems.GOLD_COIN.get(),
            ModItems.IRON_COIN.get(),
            ModItems.COPPER_COIN.get()
    };

    public CoinPouchItem(Properties props) {
        super(props.stacksTo(1));
    }

    // Helper: Always play coin SFX when coins move
    private static void playCoinsSound(Player player) {
        if (player != null && !player.level().isClientSide) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.COIN_POUCH_INSERT.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pouch, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || pouch.getCount() != 1) return false;
        if (!player.containerMenu.getCarried().isEmpty()) return false;

        int selected = getSelectedCoinIndex(pouch);
        int value = COIN_VALUES[safeIndex(selected)];
        int balance = getBalance(pouch);
        Item coinItem = COIN_ITEMS[safeIndex(selected)];

        if (balance < value) return false;

        if (!player.level().isClientSide) {
            player.containerMenu.setCarried(new ItemStack(coinItem, 1));
            setBalance(pouch, balance - value);
            playCoinsSound(player);
        }
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pouch, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        // Deposit: Right-click a coin stack onto the pouch
        int depositValue = getCoinValue(other);
        if (depositValue > 0 && action == ClickAction.SECONDARY && pouch.getCount() == 1 && slot.allowModification(player)) {
            int count = other.getCount();
            setBalance(pouch, getBalance(pouch) + (depositValue * count));
            playCoinsSound(player);
            other.setCount(0); // remove deposited coins
            return true;
        }

        // Withdraw: Pouch in cursor, empty slot under cursor
        if (action == ClickAction.SECONDARY && pouch.getCount() == 1 && other.isEmpty() && slot.allowModification(player)) {
            int selected = getSelectedCoinIndex(pouch);
            int value = COIN_VALUES[safeIndex(selected)];
            int balance = getBalance(pouch);
            Item coinItem = COIN_ITEMS[safeIndex(selected)];
            if (balance < value) return false;

            if (!player.level().isClientSide) {
                ItemStack coin = new ItemStack(coinItem, 1);
                access.set(coin);
                setBalance(pouch, balance - value);
                playCoinsSound(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        int selected = getSelectedCoinIndex(pouch);
        int value = COIN_VALUES[safeIndex(selected)];
        int balance = getBalance(pouch);
        Item coinItem = COIN_ITEMS[safeIndex(selected)];

        if (player.isShiftKeyDown()) {
            // SHIFT: Drop all coins, spit each stack forwards
            if (!level.isClientSide && balance > 0) {
                int remaining = balance;
                for (int i = 0; i < COIN_VALUES.length; i++) {
                    int denomValue = COIN_VALUES[i];
                    int num = remaining / denomValue;
                    if (num > 0) {
                        ItemStack coinStack = new ItemStack(COIN_ITEMS[i], num);
                        player.drop(coinStack, false, false); // Q-spit style
                        remaining -= num * denomValue;
                    }
                }
                setBalance(pouch, 0);
                playCoinsSound(player);
            }
            return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide());
        } else {
            // No shift: drop a single coin forward (selected denomination)
            if (balance < value) return InteractionResultHolder.fail(pouch);

            if (!level.isClientSide) {
                ItemStack coin = new ItemStack(coinItem, 1);
                player.drop(coin, false, false); // Q-like spit
                setBalance(pouch, balance - value);
                playCoinsSound(player);
            }
            return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide());
        }
    }

    public static void cycleSelectedCoin(ItemStack pouch, int direction) {
        int sel = getSelectedCoinIndex(pouch);
        int attempts = COIN_VALUES.length;
        int balance = getBalance(pouch);
        do {
            sel = (sel + direction + COIN_VALUES.length) % COIN_VALUES.length;
            attempts--;
        } while (balance < COIN_VALUES[safeIndex(sel)] && attempts > 0);
        setSelectedCoinIndex(pouch, sel);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int balance = getBalance(stack);
        tooltip.add(Component.literal("Balance: " + balance + " coins").withStyle(ChatFormatting.GRAY));
        if (balance == 0) return;

        int selected = getSelectedCoinIndex(stack);
        int value = COIN_VALUES[safeIndex(selected)];

        // Only show withdraw tooltip if SHIFT is held
        if (Screen.hasShiftDown()) {
            if (balance >= value) {
                tooltip.add(Component.literal("Withdraw: " + value + "-coin").withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.add(Component.literal("Not enough balance for " + value + "-coin!").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    // Drop all coins on pouch destroy (death, fire, etc)
    @Override
    public void onDestroyed(ItemEntity entity) {
        if (!entity.level().isClientSide) {
            int balance = getBalance(entity.getItem());
            dropAsCoins(entity.level(), entity.getX(), entity.getY(), entity.getZ(), balance);
            setBalance(entity.getItem(), 0);
        }
    }

    private void dropAsCoins(Level level, double x, double y, double z, int total) {
        for (int i = 0; i < COIN_VALUES.length; i++) {
            int count = total / COIN_VALUES[i];
            total = total % COIN_VALUES[i];
            for (int j = 0; j < count; j++) {
                ItemStack coin = new ItemStack(COIN_ITEMS[i]);
                level.addFreshEntity(new ItemEntity(level, x, y, z, coin));
            }
        }
    }

    // --- Helpers for NBT/component access ---
    public static int getBalance(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.COIN_BALANCE, 0);
    }
    public static void setBalance(ItemStack stack, int value) {
        stack.set(ModDataComponents.COIN_BALANCE, value);
    }
    public static int getSelectedCoinIndex(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.SELECTED_COIN, 4);
    }
    public static void setSelectedCoinIndex(ItemStack stack, int idx) {
        stack.set(ModDataComponents.SELECTED_COIN, safeIndex(idx));
    }
    private static int getCoinValue(ItemStack coin) {
        for (int i = 0; i < COIN_ITEMS.length; i++) {
            if (coin.is(COIN_ITEMS[i])) return COIN_VALUES[i];
        }
        return 0;
    }
    private static int safeIndex(int idx) {
        return Mth.clamp(idx, 0, COIN_VALUES.length - 1);
    }

}
