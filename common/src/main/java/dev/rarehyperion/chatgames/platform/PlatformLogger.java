package dev.rarehyperion.chatgames.platform;

/**
 * Logger abstraction for the platform.
 *
 * <p>
 *     Provides methods for logging messages at different levels (info, warn, error).
 * </p>
 *
 * @author RareHyperIon
 * @since 20/12/2025
 */
public interface PlatformLogger {

    /**
     * Logs an informational message.
     * @param message The message to log.
     */
    void info(final String message);

    /**
     * Logs a warning message.
     * @param message The message to log.
     */
    void warn(final String message);

    /**
     * Logs an error message.
     * @param message The message to log.
     */
    void error(final String message);

    /**
     * Logs a warning message with an exception.
     * @param message The message to log.
     * @param throwable The exception to log.
     */
    void warn(final String message, final Throwable throwable);

    /**
     * Logs an error message with an exception.
     * @param message The message to log.
     * @param throwable The exception to log.
     */
    void error(final String message, final Throwable throwable);

}
