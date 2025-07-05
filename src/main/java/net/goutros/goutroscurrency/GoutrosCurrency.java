package net.goutros.goutroscurrency;

import com.mojang.logging.LogUtils;
import net.goutros.goutroscurrency.block.ModBlockEntities;
import net.goutros.goutroscurrency.block.ModBlocks;
import net.goutros.goutroscurrency.client.ClientEvents;
import net.goutros.goutroscurrency.client.ClientSetup;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.core.ModEnchantmentComponents;
import net.goutros.goutroscurrency.core.ModEnchantments;
import net.goutros.goutroscurrency.core.ModSounds;
import net.goutros.goutroscurrency.data.QuestPresetReloadListener;
import net.goutros.goutroscurrency.event.CounterfeitTradeHandler;
import net.goutros.goutroscurrency.item.CoinPouchPickupHandler;
import net.goutros.goutroscurrency.item.ModItems;
import net.goutros.goutroscurrency.network.ModNetworking;
import net.goutros.goutroscurrency.quest.QuestEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod("goutroscurrency")
public class GoutrosCurrency {
   public static final String MOD_ID = "goutroscurrency";
   private static final Logger LOGGER = LogUtils.getLogger();

   public GoutrosCurrency(IEventBus modEventBus, ModContainer modContainer) {
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(this::addCreative);
      modEventBus.register(Config.class);
      modEventBus.addListener(ClientSetup::onClientSetup);
      modEventBus.addListener(ModNetworking::register);
      ModItems.register(modEventBus);
      ModBlocks.register(modEventBus);
      ModBlockEntities.register(modEventBus);
      ModDataComponents.register(modEventBus);
      ModEnchantments.register(modEventBus);
      ModEnchantmentComponents.register(modEventBus);
      ModSounds.register(modEventBus);
      ModAttachments.register(modEventBus);
      ModGameRules.init();
      modContainer.registerConfig(Type.COMMON, Config.SPEC);
      NeoForge.EVENT_BUS.register(this);
      NeoForge.EVENT_BUS.register(QuestEvents.class);
      NeoForge.EVENT_BUS.register(CounterfeitTradeHandler.class);
      NeoForge.EVENT_BUS.register(CoinPouchPickupHandler.class);
      NeoForge.EVENT_BUS.addListener(this::onAddReloadListener);
      if (FMLEnvironment.dist == Dist.CLIENT) {
         NeoForge.EVENT_BUS.addListener(ClientEvents::onHotbarScroll);
         NeoForge.EVENT_BUS.addListener(ClientEvents::onInventoryScroll);
      }

   }

   private void commonSetup(FMLCommonSetupEvent event) {
   }

   private void addCreative(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
         event.accept(ModItems.COUNTERFEIT_CASH);
      }

      if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
         event.accept(ModBlocks.QUEST_BOARD_ITEM);
      }

      if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
         event.accept(ModItems.COIN_POUCH);
         event.accept(ModItems.COPPER_COIN);
         event.accept(ModItems.IRON_COIN);
         event.accept(ModItems.GOLD_COIN);
         event.accept(ModItems.DIAMOND_COIN);
         event.accept(ModItems.NETHERITE_COIN);
      }

   }

   @SubscribeEvent
   public void onServerStarting(ServerStartingEvent event) {
   }

   @SubscribeEvent
   public void onAddReloadListener(AddReloadListenerEvent event) {
      LOGGER.info("[GoutrosCurrency] Registering QuestPresetReloadListener...");
      event.addListener(new QuestPresetReloadListener());
   }
}
