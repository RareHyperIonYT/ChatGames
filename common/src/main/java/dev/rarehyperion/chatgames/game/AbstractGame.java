package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.AbstractChatGames;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public abstract class AbstractGame implements Game {

    protected final AbstractChatGames plugin;
    protected final GameConfig config;
    protected final GameType type;

    protected AbstractGame(final AbstractChatGames plugin, final GameConfig config, GameType type) {
        this.plugin = plugin;
        this.config = config;
        this.type = type;
    }

    @Override
    public GameType getType() {
        return this.type;
    }

    @Override
    public void onWin(final Player winner) {
        final Component message = this.config.getWinMessage(winner.getName(), this.getCorrectAnswer().orElse(""));
        this.plugin.broadcast(message);

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            for(final String command : this.config.getRewardCommands()) {
                final String processed = command.replace("{player}", winner.getName());
                this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), processed);
            }
        });
    }

    @Override
    public void onTimeout() {
        final Component message = this.config.getTimeoutMessage(this.getCorrectAnswer().orElse(""));
        this.plugin.broadcast(message);
    }

    @Override
    public List<String> getAnswerOptions() {
        return List.of();
    }

    protected Component createStartMessage() {
        return this.config.getStartMessage(this.getQuestion());
    }

    protected  <T> T selectRandom(List<T> list) {
        return list.get(new Random().nextInt(list.size()));
    }

}
