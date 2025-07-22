package com.pim.planta;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.pim.planta.base.PlantaApplication;
import com.pim.planta.workers.NotificationWorker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class PlantaApplicationTest {

    private PlantaApplication plantaApplication;
    private WorkManager mockWorkManager;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        plantaApplication = new PlantaApplication();
        mockWorkManager = mock(WorkManager.class);
        WorkManager.initialize(context, new androidx.work.Configuration.Builder().build());
        when(WorkManager.getInstance(context)).thenReturn(mockWorkManager);
    }

    @Test
    public void testNotificationWorkerIsScheduled() {
        // Act
        plantaApplication.onCreate();

        // Assert
        PeriodicWorkRequest expectedWorkRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 5, TimeUnit.MINUTES).build();
        verify(mockWorkManager).enqueue(org.mockito.ArgumentMatchers.any(PeriodicWorkRequest.class));
    }
}