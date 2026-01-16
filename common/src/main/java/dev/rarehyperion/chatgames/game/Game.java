package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public interface Game {

    GameType getType();

    void start();
    boolean checkAnswer(final String answer);

    void onEnd();
    void onStart();
    void onWin(final PlatformPlayer winner);
    void onTimeout();

    Component getQuestion();
    Optional<String> getCorrectAnswer();
    List<String> getAnswerOptions();

}
