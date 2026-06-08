package dev.rarehyperion.chatgames.game;

public interface SnapshotSource {

    GameSnapshot createSnapshot(final String gameId, final long startedAtMillis, final long expiresAtMillis);

}
