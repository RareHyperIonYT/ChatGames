package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformTask;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpigotPlatformTask implements PlatformTask {

    private final BukkitTask task;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public SpigotPlatformTask(final BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        if(this.cancelled.compareAndSet(false, true)) {
            try {
                this.task.cancel();
            } catch (final Exception exception) {
                exception.printStackTrace(System.err);
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled.get();
    }

    @Override
    public long getTaskId() {
        return this.task.getTaskId();
    }

}
