package dev.rarehyperion.chatgames;

import dev.rarehyperion.chatgames.platform.impl.FoliaPlatform;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatGamesFolia extends JavaPlugin {

    private final ChatGamesCore core;

    public ChatGamesFolia() {
        this.core = new ChatGamesCore(new FoliaPlatform(this));
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