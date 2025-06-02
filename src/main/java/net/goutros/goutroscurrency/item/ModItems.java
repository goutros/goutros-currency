package net.goutros.goutroscurrency.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.goutros.goutroscurrency.GoutrosCurrency;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoutrosCurrency.MOD_ID);

    public static final DeferredItem<Item> COPPER_COIN = ITEMS.register("copper_coin",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> IRON_COIN = ITEMS.register("iron_coin",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> GOLD_COIN = ITEMS.register("gold_coin",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> DIAMOND_COIN = ITEMS.register("diamond_coin",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> NETHERITE_COIN = ITEMS.register("netherite_coin",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COUNTERFEIT_CASH = ITEMS.register("counterfeit_cash",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> COIN_POUCH = ITEMS.register("coin_pouch",
            () -> new CoinPouchItem(new Item.Properties()));



    public static Item getCoinByValue(int value) {
        return switch (value) {
            case 1 -> ModItems.COPPER_COIN.get();
            case 5 -> ModItems.IRON_COIN.get();
            case 10 -> ModItems.GOLD_COIN.get();
            case 25 -> ModItems.DIAMOND_COIN.get();
            case 100 -> ModItems.NETHERITE_COIN.get();
            default -> ModItems.COPPER_COIN.get(); // fallback/default
        };
    }


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
