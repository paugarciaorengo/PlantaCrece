package com.pim.planta.test;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

public class TestApplication extends Application implements Configuration.Provider, TestLifecycleApplication {
    public TestApplication() {
        super();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(new TestWorkerFactory())
                .build();
    }

    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object test) {

    }

    @Override
    public void afterTest(Method method) {

    }
}