package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformLogger;

import java.util.logging.Logger;

public class SpigotPlatformLogger implements PlatformLogger {

    private final Logger logger;

    public SpigotPlatformLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(final String message) {
        this.logger.info(message);
    }

    @Override
    public void warn(final String message) {
        this.logger.warning(message);
    }

    @Override
    public void error(final String message) {
        this.logger.severe(message);
    }

}
