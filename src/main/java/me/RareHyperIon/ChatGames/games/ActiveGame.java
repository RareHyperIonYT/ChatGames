package me.RareHyperIon.ChatGames.games;

import me.RareHyperIon.ChatGames.ChatGames;
import me.RareHyperIon.ChatGames.games.types.MathGame;
import me.RareHyperIon.ChatGames.games.types.ReactionGame;
import me.RareHyperIon.ChatGames.games.types.TriviaGame;
import me.RareHyperIon.ChatGames.games.types.UnscrambleGame;
import me.RareHyperIon.ChatGames.handlers.LanguageHandler;
import org.bukkit.entity.Player;

public class ActiveGame {

    private final Game game;

    public ActiveGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        this.game = this.createGame(plugin, config, language);
        this.game.onStart();
    }

    private Game createGame(final ChatGames plugin, final GameConfig config, final LanguageHandler language) {
        return switch (config.name.toLowerCase()) {
            case "reaction" -> new ReactionGame(plugin, config, language);
            case "trivia" -> new TriviaGame(plugin, config, language);
            case "math" -> new MathGame(plugin, config, language);
            case "unscramble" -> new UnscrambleGame(plugin, config, language);
            default -> throw new IllegalArgumentException("Unknown game type: " + config.name);
        };
    }

    public void win(final Player player) {
        this.game.onWin(player);
    }

    public void end() {
        this.game.onEnd();
    }

    public Game getGame() {
        return this.game;
    }

}
