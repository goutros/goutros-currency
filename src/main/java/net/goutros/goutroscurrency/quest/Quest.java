package net.goutros.goutroscurrency.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class Quest {
   public final String type;
   public final String targetId;
   public final int amount;
   public final String description;
   public final int coinReward;
   public final int progress;
   public final UUID id;
   public final boolean completed;
   @Nullable
   public final String playerName;
   public static final Codec<Quest> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.STRING.fieldOf("type").forGetter((q) -> {
         return q.type;
      }), Codec.STRING.fieldOf("targetId").forGetter((q) -> {
         return q.targetId;
      }), Codec.INT.fieldOf("amount").forGetter((q) -> {
         return q.amount;
      }), Codec.STRING.fieldOf("description").forGetter((q) -> {
         return q.description;
      }), Codec.INT.fieldOf("coinReward").forGetter((q) -> {
         return q.coinReward;
      }), Codec.INT.fieldOf("progress").forGetter((q) -> {
         return q.progress;
      }), Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("id").forGetter((q) -> {
         return q.id;
      }), Codec.BOOL.fieldOf("completed").forGetter((q) -> {
         return q.completed;
      }), Codec.STRING.optionalFieldOf("playerName").forGetter((q) -> {
         return Optional.ofNullable(q.playerName);
      })).apply(instance, (type, targetId, amount, desc, coins, prog, id, complete, nameOpt) -> {
         return new Quest(type, targetId, amount, desc, coins, prog, id, complete, (String)nameOpt.orElse((Object)null));
      });
   });

   public Quest(String type, String targetId, int amount, String description, int coinReward, int progress, UUID id, boolean completed, @Nullable String playerName) {
      this.type = type;
      this.targetId = targetId;
      this.amount = amount;
      this.description = description;
      this.coinReward = coinReward;
      this.progress = progress;
      this.id = id;
      this.completed = completed;
      this.playerName = playerName;
   }

   public Quest(String type, String targetId, int amount, String description, int coinReward) {
      this(type, targetId, amount, description, coinReward, 0, UUID.randomUUID(), false, (String)null);
   }

   public Quest(String type, String targetId, int amount, String description, int coinReward, int progress, UUID id, boolean completed) {
      this(type, targetId, amount, description, coinReward, progress, id, completed, (String)null);
   }

   public UUID getId() {
      return this.id;
   }

   public boolean isComplete() {
      return this.completed || this.progress >= this.amount;
   }

   public boolean isHunt() {
      return this.type.equalsIgnoreCase("hunt");
   }

   public String getDescription() {
      return this.description != null ? this.description : this.targetId;
   }

   public Quest copyWithProgress(int newProgress) {
      return new Quest(this.type, this.targetId, this.amount, this.description, this.coinReward, newProgress, this.id, this.completed, this.playerName);
   }

   public Quest copyWithCompletion(boolean newCompleted) {
      return new Quest(this.type, this.targetId, this.amount, this.description, this.coinReward, this.progress, this.id, newCompleted, this.playerName);
   }

   public ItemStack getTargetItemStack(Level level) {
      if (this.targetId != null && !this.targetId.isEmpty() && !this.targetId.startsWith("#")) {
         Item item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(this.targetId));
         return item != null ? new ItemStack(item) : ItemStack.EMPTY;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static Quest fromItemStack(ItemStack stack) {
      return (Quest)stack.getOrDefault(ModDataComponents.QUEST_DATA, (Object)null);
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         Quest quest = (Quest)obj;
         return this.amount == quest.amount && this.coinReward == quest.coinReward && this.progress == quest.progress && this.completed == quest.completed && Objects.equals(this.type, quest.type) && Objects.equals(this.targetId, quest.targetId) && Objects.equals(this.description, quest.description) && Objects.equals(this.id, quest.id);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.targetId, this.amount, this.description, this.coinReward, this.progress, this.id, this.completed});
   }
}
