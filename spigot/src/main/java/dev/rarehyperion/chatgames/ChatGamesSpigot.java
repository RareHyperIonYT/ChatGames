package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.platform.impl.SpigotPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatGamesSpigot extends JavaPlugin {

    private final ChatGamesCore core;

    public ChatGamesSpigot() {
        this.core = new ChatGamesCore(new SpigotPlatform(this));
    }

    @Override
    public void onLoad() {
        this.core.load();
    }

    @Override
    public void onEnable() {
        this.core.enable();
    }

    @Override
    public void onDisable() {
        this.core.disable();
    }


}
