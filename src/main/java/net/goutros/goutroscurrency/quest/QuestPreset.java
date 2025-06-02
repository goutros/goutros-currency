package net.goutros.goutroscurrency.quest;

import java.util.List;

public class QuestPreset {
    public final String type;
    public final int amountMin, amountMax;
    public final int coinMin, coinMax;
    public final List<String> items;
    public final List<String> tags;

    public QuestPreset(String type, int amountMin, int amountMax, int coinMin, int coinMax, List<String> items, List<String> tags) {
        this.type = type;
        this.amountMin = amountMin;
        this.amountMax = amountMax;
        this.coinMin = coinMin;
        this.coinMax = coinMax;
        this.items = items;
        this.tags = tags;
    }
}
