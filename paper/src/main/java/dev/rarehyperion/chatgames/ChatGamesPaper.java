package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.platform.impl.PaperPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatGamesPaper extends JavaPlugin {

    private final ChatGamesCore core;

    public ChatGamesPaper() {
        this.core = new ChatGamesCore(new PaperPlatform(this));
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