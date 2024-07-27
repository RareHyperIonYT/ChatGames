package me.RareHyperIon.ChatGames.games;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameConfig {

    private final String name, description, winMessage;

    private final List<Map.Entry<String, String>> choices;
    private final List<String> commands;

    public GameConfig(final FileConfiguration configuration) {
        this.name = configuration.getString("name");
        this.description = configuration.getString("description");
        this.winMessage = configuration.getString("reward.message");
        this.commands = configuration.getStringList("reward.commands");

        this.choices = this.parse(configuration.getList("questions"));
    }

    private List<Map.Entry<String, String>> parse(final List<?> list) {
        final List<Map.Entry<String, String>> choices = new ArrayList<>();

        if(list == null) throw new IllegalArgumentException("Game \"" + this.name + "\" does not contain questions.");

        for(final Object object : list) {
            if(!(object instanceof List<?> choice)) continue;
            if(choice.size() < 2) continue;

            choices.add(new AbstractMap.SimpleEntry<>((String) choice.get(0), (String) choice.get(1)));
        }

        return choices;
    }

}
