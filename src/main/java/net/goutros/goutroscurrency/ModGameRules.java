package net.goutros.goutroscurrency;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.Key;

public class ModGameRules {
   public static final Key<BooleanValue> DEBUG_ALWAYS_QUEST;

   public static void init() {
   }

   static {
      DEBUG_ALWAYS_QUEST = GameRules.register("debugAlwaysQuest", Category.MISC, BooleanValue.create(false));
   }
}
