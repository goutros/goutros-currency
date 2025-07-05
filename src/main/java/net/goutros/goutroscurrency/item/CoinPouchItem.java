package net.goutros.goutroscurrency.item;

import java.util.List;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.core.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;

public class CoinPouchItem extends Item {
   public static final int[] COIN_VALUES = new int[]{100, 50, 20, 10, 1};
   public static final Item[] COIN_ITEMS;
   private static final String[] COIN_SUFFIXES;

   public CoinPouchItem(Properties props) {
      super(props.stacksTo(1));
   }

   private static void playInsertSound(Player player) {
      if (!player.level().isClientSide) {
         player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.COIN_POUCH_INSERT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      }

   }

   private static void playWithdrawSound(Player player) {
      if (!player.level().isClientSide) {
         player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.COIN_POUCH_INSERT.get(), SoundSource.PLAYERS, 1.0F, 0.8F);
      }

   }

   public boolean overrideStackedOnOther(ItemStack pouch, Slot slot, ClickAction action, Player player) {
      if (action == ClickAction.SECONDARY && pouch.getCount() == 1 && player.containerMenu.getCarried().isEmpty()) {
         int selected = getSelectedCoinIndex(pouch);
         int value = COIN_VALUES[safeIndex(selected)];
         int balance = getBalance(pouch);
         if (balance < value) {
            return false;
         } else {
            if (!player.level().isClientSide) {
               player.containerMenu.setCarried(new ItemStack(COIN_ITEMS[safeIndex(selected)], 1));
               setBalance(pouch, balance - value);
               playWithdrawSound(player);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public boolean overrideOtherStackedOnMe(ItemStack targetPouch, ItemStack draggedPouch, Slot slot, ClickAction action, Player player, SlotAccess access) {
      int draggedBalance;
      int selected;
      int value;
      if (draggedPouch.getItem() instanceof CoinPouchItem && action == ClickAction.SECONDARY && targetPouch.getCount() == 1 && draggedPouch.getCount() == 1 && slot.allowModification(player)) {
         if (!player.level().isClientSide) {
            draggedBalance = getBalance(draggedPouch);
            if (draggedBalance > 0) {
               selected = getBalance(targetPouch);
               value = Mth.clamp(selected + draggedBalance, 0, 10000000);
               setBalance(targetPouch, value);
               setBalance(draggedPouch, 0);
               playInsertSound(player);
               player.displayClientMessage(Component.literal("Pouches merged!").withStyle(ChatFormatting.GRAY), true);
            } else {
               player.displayClientMessage(Component.literal("Dragged pouch is empty.").withStyle(ChatFormatting.RED), true);
            }
         }

         return true;
      } else {
         draggedBalance = getCoinValue(draggedPouch);
         if (draggedBalance > 0 && action == ClickAction.SECONDARY && targetPouch.getCount() == 1 && slot.allowModification(player)) {
            selected = draggedPouch.getCount();
            setBalance(targetPouch, getBalance(targetPouch) + draggedBalance * selected);
            setSelectedCoinIndex(targetPouch, getCoinIndex(draggedPouch));
            playInsertSound(player);
            player.level().playSound((Player)null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.COIN_POUCH_INSERT.get(), SoundSource.PLAYERS, 0.5F, 1.5F);
            draggedPouch.setCount(0);
            return true;
         } else if (action == ClickAction.SECONDARY && targetPouch.getCount() == 1 && draggedPouch.isEmpty() && slot.allowModification(player)) {
            selected = getSelectedCoinIndex(targetPouch);
            value = COIN_VALUES[safeIndex(selected)];
            int balance = getBalance(targetPouch);
            Item coinItem = COIN_ITEMS[safeIndex(selected)];
            if (balance < value) {
               return false;
            } else {
               if (!player.level().isClientSide) {
                  ItemStack coin = new ItemStack(coinItem, 1);
                  access.set(coin);
                  setBalance(targetPouch, balance - value);
                  playInsertSound(player);
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack pouch = player.getItemInHand(hand);
      int selected = getSelectedCoinIndex(pouch);
      int value = COIN_VALUES[safeIndex(selected)];
      int balance = getBalance(pouch);
      if (player.isShiftKeyDown()) {
         if (balance <= 0) {
            return InteractionResultHolder.fail(pouch);
         } else {
            if (!level.isClientSide) {
               int remaining = balance;

               for(int i = 0; i < COIN_VALUES.length; ++i) {
                  int denom = COIN_VALUES[i];
                  int num = remaining / denom;
                  if (num > 0) {
                     player.drop(new ItemStack(COIN_ITEMS[i], num), false, false);
                     remaining -= num * denom;
                  }
               }

               setBalance(pouch, 0);
               level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), (SoundEvent)ModSounds.COIN_POUCH_INSERT.get(), SoundSource.PLAYERS, 0.8F, 0.9F);
            }

            return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide());
         }
      } else if (balance < value) {
         return InteractionResultHolder.fail(pouch);
      } else {
         if (!level.isClientSide) {
            player.drop(new ItemStack(COIN_ITEMS[safeIndex(selected)], 1), false, false);
            setBalance(pouch, balance - value);
            playWithdrawSound(player);
         }

         return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide());
      }
   }

   public static int getBalance(ItemStack stack) {
      return (Integer)stack.getOrDefault(ModDataComponents.COIN_BALANCE, 0);
   }

   public static void setBalance(ItemStack stack, int value) {
      stack.set(ModDataComponents.COIN_BALANCE, Mth.clamp(value, 0, 10000000));
   }

   public static int getSelectedCoinIndex(ItemStack stack) {
      return (Integer)stack.getOrDefault(ModDataComponents.SELECTED_COIN, 4);
   }

   public static void setSelectedCoinIndex(ItemStack stack, int index) {
      stack.set(ModDataComponents.SELECTED_COIN, safeIndex(index));
   }

   public static void cycleSelectedCoin(ItemStack stack, int direction) {
      int sel = getSelectedCoinIndex(stack);
      int attempts = COIN_VALUES.length;
      int balance = getBalance(stack);

      do {
         sel = (sel + direction + COIN_VALUES.length) % COIN_VALUES.length;
         --attempts;
      } while(balance < COIN_VALUES[safeIndex(sel)] && attempts > 0);

      setSelectedCoinIndex(stack, sel);
   }

   public static int getCoinValue(ItemStack coin) {
      for(int i = 0; i < COIN_ITEMS.length; ++i) {
         if (coin.is(COIN_ITEMS[i])) {
            return COIN_VALUES[i];
         }
      }

      return 0;
   }

   public static int getCoinIndex(ItemStack coin) {
      for(int i = 0; i < COIN_ITEMS.length; ++i) {
         if (coin.is(COIN_ITEMS[i])) {
            return i;
         }
      }

      return safeIndex(4);
   }

   private static int safeIndex(int idx) {
      return Mth.clamp(idx, 0, COIN_VALUES.length - 1);
   }

   public boolean isEnchantable(ItemStack stack) {
      return true;
   }

   public int getEnchantmentValue() {
      return 1000;
   }

   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      int balance = getBalance(stack);
      int selected = getSelectedCoinIndex(stack);
      int value = COIN_VALUES[safeIndex(selected)];
      if (!Screen.hasShiftDown()) {
         tooltip.add(Component.literal("Hold [").append(Component.literal("Shift").withStyle(ChatFormatting.GRAY)).append("] for Summary").withStyle(ChatFormatting.DARK_GRAY));
      } else {
         tooltip.add(Component.literal("Hold [").append(Component.literal("Shift").withStyle(ChatFormatting.WHITE)).append("] for Summary").withStyle(ChatFormatting.DARK_GRAY));
         tooltip.add(Component.empty());
         tooltip.add(Component.literal("Used to store, compact and withdraw currency easily.").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.empty());
         tooltip.add(Component.literal("Current Balance: ").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.literal(" " + formatCoinAmount((long)balance) + " Coins").withStyle(ChatFormatting.GOLD));
         if (balance >= value) {
            tooltip.add(Component.literal("Withdraw Amount:").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(" " + value + " Coins").withStyle(ChatFormatting.YELLOW));
         }

         tooltip.add(Component.empty());
         tooltip.add(Component.literal("While Holding Shift").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.literal(" Scroll to change Withdrawal Amount.").withStyle(ChatFormatting.YELLOW));
         tooltip.add(Component.literal("When Shift Right-Clicked").withStyle(ChatFormatting.GRAY));
         tooltip.add(Component.literal(" Drops all coins in Most Compact form.").withStyle(ChatFormatting.YELLOW));
      }
   }

   private static String formatCoinAmount(long amount) {
      if (amount < 1000L) {
         return String.valueOf(amount);
      } else {
         int index = 0;

         double value;
         for(value = (double)amount; value >= 1000.0D && index < COIN_SUFFIXES.length - 1; ++index) {
            value /= 1000.0D;
         }

         String formatted = String.format("%.1f", value);
         if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
         }

         return formatted + COIN_SUFFIXES[index];
      }
   }

   public void onDestroyed(ItemEntity entity) {
      if (!entity.level().isClientSide) {
         int balance = getBalance(entity.getItem());

         for(int i = 0; i < COIN_VALUES.length; ++i) {
            int count = balance / COIN_VALUES[i];
            balance %= COIN_VALUES[i];

            for(int j = 0; j < count; ++j) {
               entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(COIN_ITEMS[i])));
            }
         }

         setBalance(entity.getItem(), 0);
      }

   }

   static {
      COIN_ITEMS = new Item[]{(Item)ModItems.NETHERITE_COIN.get(), (Item)ModItems.DIAMOND_COIN.get(), (Item)ModItems.GOLD_COIN.get(), (Item)ModItems.IRON_COIN.get(), (Item)ModItems.COPPER_COIN.get()};
      COIN_SUFFIXES = new String[]{"", "K", "M", "B", "T", "Q", "Qn", "Sx", "Sp", "Oc", "No", "Dc", "Ud", "Dd", "Td", "Qd", "Qnd", "Sxd", "Spd", "Od", "Nd", "Vg", "Uv", "Dv", "Tv", "Qv", "Qnv", "Sxv", "Spv", "Ov", "Nv", "Tr", "Utr", "Dtr", "Ttr", "Qtr", "Qntr", "Sxtr", "Sptr", "Otr", "Ntr", "INF"};
   }
}
