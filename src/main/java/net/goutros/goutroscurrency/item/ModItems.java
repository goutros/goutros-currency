package net.goutros.goutroscurrency.item;

import net.goutros.goutroscurrency.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public class ModItems {
   public static final Items ITEMS = DeferredRegister.createItems("goutroscurrency");
   public static final DeferredItem<Item> COPPER_COIN;
   public static final DeferredItem<Item> IRON_COIN;
   public static final DeferredItem<Item> GOLD_COIN;
   public static final DeferredItem<Item> DIAMOND_COIN;
   public static final DeferredItem<Item> NETHERITE_COIN;
   public static final DeferredItem<Item> COUNTERFEIT_CASH;
   public static final DeferredItem<Item> COIN_POUCH;
   public static final DeferredItem<Item> QUEST_NOTE;

   public static Item getCoinByValue(int value) {
      Item var10000;
      switch(value) {
      case 1:
         var10000 = (Item)COPPER_COIN.get();
         break;
      case 5:
         var10000 = (Item)IRON_COIN.get();
         break;
      case 10:
         var10000 = (Item)GOLD_COIN.get();
         break;
      case 25:
         var10000 = (Item)DIAMOND_COIN.get();
         break;
      case 100:
         var10000 = (Item)NETHERITE_COIN.get();
         break;
      default:
         var10000 = (Item)COPPER_COIN.get();
      }

      return var10000;
   }

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }

   static {
      COPPER_COIN = ITEMS.register("copper_coin", () -> {
         return new Item(new Properties());
      });
      IRON_COIN = ITEMS.register("iron_coin", () -> {
         return new Item(new Properties());
      });
      GOLD_COIN = ITEMS.register("gold_coin", () -> {
         return new Item(new Properties());
      });
      DIAMOND_COIN = ITEMS.register("diamond_coin", () -> {
         return new Item(new Properties());
      });
      NETHERITE_COIN = ITEMS.register("netherite_coin", () -> {
         return new Item(new Properties());
      });
      COUNTERFEIT_CASH = ITEMS.register("counterfeit_cash", () -> {
         return new BlockItem((Block)ModBlocks.CASH_LAYER.get(), new Properties());
      });
      COIN_POUCH = ITEMS.register("coin_pouch", () -> {
         return new CoinPouchItem((new Properties()).stacksTo(1));
      });
      QUEST_NOTE = ITEMS.register("quest_note", () -> {
         return new QuestNoteItem((new Properties()).stacksTo(1).rarity(Rarity.UNCOMMON));
      });
   }
}
