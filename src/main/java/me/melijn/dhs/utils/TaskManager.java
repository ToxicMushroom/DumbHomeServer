package me.melijn.dhs.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;
import java.util.function.Function;

public class TaskManager {

    private final Function<String, ThreadFactory> threadFactory = name -> new ThreadFactoryBuilder().setNameFormat("[" + name + "-Pool-%d] ").build();
    private final ExecutorService executorService = Executors.newCachedThreadPool(threadFactory.apply("Task"));
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10, threadFactory.apply("Rep"));


    public void scheduleRepeating(final Runnable runnable, final long period) {
        scheduledExecutorService.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS);
    }

    public void scheduleRepeating(final Runnable runnable, final long initialDelay, final long period) {
        scheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public void async(final Runnable runnable) {
        executorService.submit(runnable);
    }

    public void async(final Runnable runnable, final long after) {
        scheduledExecutorService.schedule(runnable, after, TimeUnit.MILLISECONDS);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }
}
