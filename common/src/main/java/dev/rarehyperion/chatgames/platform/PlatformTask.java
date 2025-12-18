package dev.rarehyperion.chatgames.platform;

public interface PlatformTask {

    void cancel();
    boolean isCancelled();
    long getTaskId();

}
