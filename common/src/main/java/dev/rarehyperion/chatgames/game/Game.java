package dev.rarehyperion.chatgames.game;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public interface Game {

    GameType getType();

    void start();

    boolean checkAnswer(final String answer);

    void onWin(final Player winner);
    void onTimeout();

    Component getQuestion();
    Optional<String> getCorrectAnswer();
    List<String> getAnswerOptions();

}
