package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformLogger;
import org.apache.logging.log4j.Logger;


public class SpongePlatformLogger implements PlatformLogger {

    private final Logger logger;

    public SpongePlatformLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(final String message) {
        this.logger.info(message);
    }

    @Override
    public void warn(final String message) {
        this.logger.warn(message);
    }

    @Override
    public void error(final String message) {
        this.logger.error(message);
    }

}
