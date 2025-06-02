package net.goutros.goutroscurrency.quest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ItemLike;

import java.util.UUID;

public class Quest {
    public final String type;
    public final String material;
    public final int amount;
    public final String description;
    public final int coinReward;
    public int progress;
    public final UUID id;
    public boolean completed;  // <-- NEW

    public Quest(String type, String material, int amount, String description, int coinReward, int progress, UUID id, boolean completed) {
        this.type = type;
        this.material = material;
        this.amount = amount;
        this.description = description;
        this.coinReward = coinReward;
        this.progress = progress;
        this.id = id;
        this.completed = completed;
    }

    public Quest(String type, String material, int amount, String description, int coinReward) {
        this(type, material, amount, description, coinReward, 0, UUID.randomUUID(), false);
    }

    public UUID getId() {
        return id;
    }

    public boolean isComplete() {
        return completed || progress >= amount;
    }

    public String getDescription() {
        return description != null ? description : material;
    }

    public Quest copyWithProgress(int newProgress) {
        return new Quest(type, material, amount, description, coinReward, newProgress, id, completed);
    }

    public ItemStack getTargetItemStack(Level level) {
        if (material == null || material.isEmpty() || material.startsWith("#")) return ItemStack.EMPTY;
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(material));
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("type", type);
        tag.putString("material", material);
        tag.putInt("amount", amount);
        tag.putInt("progress", progress);
        tag.putString("description", description);
        tag.putInt("coinReward", coinReward);
        tag.putBoolean("completed", completed);  // <-- NEW
        return tag;
    }

    public static Quest fromTag(CompoundTag tag) {
        UUID id = UUID.fromString(tag.getString("id"));
        String type = tag.getString("type");
        String material = tag.getString("material");
        int amount = tag.getInt("amount");
        int progress = tag.getInt("progress");
        String description = tag.getString("description");
        int coinReward = tag.getInt("coinReward");
        boolean completed = tag.getBoolean("completed");  // <-- NEW
        return new Quest(type, material, amount, description, coinReward, progress, id, completed);
    }
}
