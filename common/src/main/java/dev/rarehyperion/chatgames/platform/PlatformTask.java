package dev.rarehyperion.chatgames.platform;

/**
 * Represents a scheduled task on the platform.
 *
 * <p>
 *     Provides methods for cancelling tasks, checking status, and retrieving the task ID.
 * </p>
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public interface PlatformTask {

    /**
     * Cancels the task.
     */
    void cancel();

    /**
     * Checks if the task has been cancelled.
     * @return {@code true} if the task is cancelled, {@code false} otherwise.
     */
    boolean isCancelled();

    /**
     * Returns the platform-specific task ID.
     * @return The task ID.
     */
    long getTaskId();

}
