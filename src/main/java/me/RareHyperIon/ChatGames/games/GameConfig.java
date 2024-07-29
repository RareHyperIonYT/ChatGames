package me.RareHyperIon.ChatGames.games;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameConfig {

    public final String name, descriptor;

    public final List<Map.Entry<String, String>> choices;
    public final List<String> commands;

    public final int timeout;

    public GameConfig(final FileConfiguration configuration) {
        this.name = configuration.getString("name");
        this.descriptor = configuration.getString("descriptor");
        this.commands = configuration.getStringList("reward-commands");
        this.timeout = configuration.getInt("timeout");

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
