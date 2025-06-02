package net.goutros.goutroscurrency.quest;

import net.goutros.goutroscurrency.ModAttachments;
import net.goutros.goutroscurrency.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.*;

public class QuestEvents {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        AttachmentType<PlayerQuestComponent> questType =
                (AttachmentType<PlayerQuestComponent>) NeoForgeRegistries.ATTACHMENT_TYPES.get(
                        ResourceLocation.fromNamespaceAndPath("goutroscurrency", ModAttachments.PLAYER_QUEST_ID));
        if (questType == null) return;

        PlayerQuestComponent questComp = player.getData(questType);
        if (questComp == null) return;

        boolean syncNeeded = false;
        boolean questClaimedOrRemoved = false;
        List<UUID> toRemove = new ArrayList<>();

        for (Quest quest : questComp.getQuests().values()) {
            if (quest == null || !"gather".equals(quest.type)) continue;

            int count = 0;
            for (ItemStack stack : player.getInventory().items) {
                String id = stack.getItem().builtInRegistryHolder().key().location().toString();
                if (id.equals(quest.material)) count += stack.getCount();
            }

            if (count != quest.progress) {
                quest.progress = count;
                syncNeeded = true;
            }

            if (quest.progress >= quest.amount && !quest.completed) {
                quest.completed = true;
                giveCoinsCompact(player, quest.coinReward);
                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Quest complete! You received " + quest.coinReward + " coins."));
                }
                syncNeeded = true;
            }

            if (quest.completed && quest.progress >= quest.amount) {
                toRemove.add(quest.getId());
                questClaimedOrRemoved = true;
            }
        }

        for (UUID id : toRemove) {
            questComp.removeQuest(id);
        }

        // Update preview and sync if needed
        if ((syncNeeded || questClaimedOrRemoved) && player instanceof ServerPlayer sp) {
            QuestManager.updatePlayerQuestPreview(sp);
        }
    }

    public static void giveCoinsCompact(Player player, int totalCoins) {
        if (totalCoins <= 0) return;

        int[] COIN_VALUES = {100, 50, 20, 10, 1};
        Item[] COIN_ITEMS = {
                ModItems.NETHERITE_COIN.get(),
                ModItems.DIAMOND_COIN.get(),
                ModItems.GOLD_COIN.get(),
                ModItems.IRON_COIN.get(),
                ModItems.COPPER_COIN.get()
        };

        Level level = player.level();
        double x = player.getX();
        double y = player.getY() + 0.25;
        double z = player.getZ();

        for (int i = 0; i < COIN_VALUES.length; i++) {
            int denom = COIN_VALUES[i];
            int count = totalCoins / denom;

            if (count > 0) {
                totalCoins -= count * denom;
                ItemStack coinStack = new ItemStack(COIN_ITEMS[i], count);
                ItemEntity drop = new ItemEntity(level, x, y, z, coinStack);
                drop.setPickUpDelay(10);
                level.addFreshEntity(drop);
            }
        }
    }
}
