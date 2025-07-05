package net.goutros.goutroscurrency;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

@EventBusSubscriber(
   modid = "goutroscurrency",
   bus = Bus.MOD
)
public class Config {
   private static final Builder BUILDER = new Builder();
   public static final IntValue QUEST_TIMER_SECONDS;
   public static final IntValue QUEST_PLAYER_COOLDOWN;
   public static final DoubleValue COUNTERFEIT_SUCCESS_CHANCE;
   public static int questPlayerCooldownSeconds;
   public static int questBoardCooldownSeconds;
   public static double counterfeitSuccessChance;
   static final ModConfigSpec SPEC;
   public static Set<Item> items;

   private static boolean validateItemName(Object obj) {
      boolean var10000;
      if (obj instanceof String) {
         String itemName = (String)obj;
         if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName))) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   @SubscribeEvent
   static void onLoad(ModConfigEvent event) {
      questBoardCooldownSeconds = (Integer)QUEST_TIMER_SECONDS.get();
      questPlayerCooldownSeconds = (Integer)QUEST_PLAYER_COOLDOWN.get();
      counterfeitSuccessChance = (Double)COUNTERFEIT_SUCCESS_CHANCE.get();
   }

   static {
      QUEST_TIMER_SECONDS = BUILDER.comment("Seconds before a quest can regenerate on a quest board").defineInRange("questBoardCooldownSeconds", 600, 0, 128000);
      QUEST_PLAYER_COOLDOWN = BUILDER.comment("Cooldown in seconds before a player can claim another quest").defineInRange("questPlayerCooldownSeconds", 600, 0, 128000);
      COUNTERFEIT_SUCCESS_CHANCE = BUILDER.comment("Chance [0.0 - 1.0] that a trade using counterfeit cash succeeds").defineInRange("counterfeitSuccessChance", 0.1D, 0.0D, 1.0D);
      SPEC = BUILDER.build();
   }
}
