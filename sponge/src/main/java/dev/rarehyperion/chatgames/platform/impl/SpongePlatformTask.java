package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformTask;
import org.spongepowered.api.scheduler.ScheduledTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class SpongePlatformTask implements PlatformTask {

    private final ScheduledTask task;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public SpongePlatformTask(final ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        if(this.cancelled.compareAndSet(false, true)) {
            this.task.cancel();
        }
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled.get() || this.task.isCancelled();
    }

    @Override
    public long getTaskId() {
        return this.task.uniqueId().getMostSignificantBits();
    }

}
