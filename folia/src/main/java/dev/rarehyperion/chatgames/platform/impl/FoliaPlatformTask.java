package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class FoliaPlatformTask implements PlatformTask {

    private final ScheduledTask task;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public FoliaPlatformTask(final ScheduledTask task) {
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
        return this.cancelled.get();
    }

    @Override
    public long getTaskId() {
        return -1L; // Isn't utilized so it doesn't matter if this is invalid.
    }

}
