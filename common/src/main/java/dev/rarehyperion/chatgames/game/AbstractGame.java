package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Random;

public abstract class AbstractGame implements Game {

    protected final ChatGamesCore plugin;
    protected final GameConfig config;
    protected final GameType type;

    protected AbstractGame(final ChatGamesCore plugin, final GameConfig config, GameType type) {
        this.plugin = plugin;
        this.config = config;
        this.type = type;
    }

    @Override
    public GameType getType() {
        return this.type;
    }

    @Override
    public void onWin(final PlatformPlayer winner) {
        final Component message = this.config.getWinMessage(winner.name(), this.getCorrectAnswer().orElse(""));
        this.plugin.broadcast(message);

        this.plugin.platform().runTask(() -> {
            for(final String command : this.config.getRewardCommands()) {
                final String processed = command.replace("{player}", winner.name());
                this.plugin.platform().dispatchCommand(processed);
//                this.plugin.platform().dispatchCommand(this.plugin.getServer().getConsoleSender(), processed);
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

    protected <T> T selectRandom(final List<T> list) {
        return list.get(new Random().nextInt(list.size()));
    }

}
