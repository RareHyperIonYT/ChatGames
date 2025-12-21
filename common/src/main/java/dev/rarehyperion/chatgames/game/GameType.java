package dev.rarehyperion.chatgames.game;

public enum GameType {

    MATH("math"),
    TRIVIA("trivia"),
    UNSCRAMBLE("unscramble"),
    REACTION("reaction"),
    MULTIPLE_CHOICE("multiple-choice");

    private final String id;

    GameType(final String id) {
        this.id = id;
    }

    public static GameType fromId(final String id) {
        for(final GameType type : values()) {
            if(type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }

        return null;
    }

}
