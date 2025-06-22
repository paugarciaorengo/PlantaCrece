package com.pim.planta.db;

import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DatabaseExecutor {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void execute(Runnable command) {
        executorService.execute(command);
    }

    public static void executeAndWait(Runnable command) {
        Future<?> future = executorService.submit(command);
        try {
            future.get(5, TimeUnit.SECONDS); // Timeout para evitar deadlocks
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("DBExecutor", "Thread interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            Log.e("DBExecutor", "Execution failed", e);
        } finally {
            if (!future.isDone()) future.cancel(true);
        }
    }
}