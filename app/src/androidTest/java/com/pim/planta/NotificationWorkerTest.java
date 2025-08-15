package com.pim.planta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.TestListenableWorkerBuilder;
import androidx.work.testing.WorkManagerTestInitHelper;

import com.pim.planta.base.NotificationActivity;
import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabasePlantoo;
import com.pim.planta.models.Plant;
import com.pim.planta.workers.NotificationWorker;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class NotificationWorkerTest {

    private Context context;
    private SharedPreferences prefs;
    private NotificationManager notificationManager;
    private DatabasePlantoo db;
    private PlantRepository plantRepo;
    private DAO dao;
    private Plant plant;
    private WorkManager workManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences(NotificationActivity.PREFS_NAME, Context.MODE_PRIVATE);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Clear SharedPreferences before each test
        prefs.edit().clear().apply();
        // Clear any existing notifications
        notificationManager.cancelAll();
        db = Room.inMemoryDatabaseBuilder(context, DatabasePlantoo.class).allowMainThreadQueries().build();
        plantRepo = PlantRepository.getInstance(context);
        dao = db.DAO();
        // Add some plants to the database
        plant = new Plant("Rosatest","",1,1,1,"","");
        plant.setBasePath("image_rosa_");
        plant.setXpMax(1000);
        plant.setDescription("This is a rose");
        plant.setNickname("Rosita");
        dao.insert(plant);
        // Add the selected plant to the SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedPlant", plant.getName());
        editor.apply();
        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(
                context, new androidx.work.Configuration.Builder()
                        .setExecutor(new SynchronousExecutor())
                        .build()
        );
        workManager = WorkManager.getInstance(context);
    }

    @After
    public void tearDown() {
        // Clear SharedPreferences after each test
        prefs.edit().clear().apply();
        // Clear any existing notifications
        notificationManager.cancelAll();
        db.close();
    }

    @Test
    public void testDoWorkNoNotificationSentIfTimeLessThanTwoHours() throws InterruptedException, ExecutionException {
        // Set the last exit time to 1 hour ago
        long lastExitTime = System.currentTimeMillis() - (60 * 60 * 1000);
        prefs.edit().putLong(NotificationActivity.LAST_EXIT_TIME_KEY, lastExitTime).apply();
        // Create a work request
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
        // Enqueue the work request
        workManager.enqueue(workRequest).getResult().get();
        // Get the work info
        WorkInfo workInfo = workManager.getWorkInfoById(workRequest.getId()).get();
        // Check if the work is successful
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
        // Check if no notification was sent
        assertEquals(0, notificationManager.getActiveNotifications().length);
    }

    @Test
    public void testDoWorkNotificationSentIfTimeMoreThanTwoHours() throws InterruptedException, ExecutionException {
        // Set the last exit time to 3 hours ago
        long lastExitTime = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        prefs.edit().putLong(NotificationActivity.LAST_EXIT_TIME_KEY, lastExitTime).apply();
        // Set the last resume time to 1 hour ago
        long lastResumeTime = System.currentTimeMillis() - (1 * 60 * 60 * 1000);
        prefs.edit().putLong("last_resume_time", lastResumeTime).apply();
        // Create a work request
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
        // Enqueue the work request
        workManager.enqueue(workRequest).getResult().get();
        // Get the work info
        WorkInfo workInfo = workManager.getWorkInfoById(workRequest.getId()).get();
        // Check if the work is successful
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
        // Check if a notification was sent
        assertEquals(1, notificationManager.getActiveNotifications().length);
        // Check if the notification has the correct content
        Notification notification = notificationManager.getActiveNotifications()[0].getNotification();
        assertEquals("Alerta", notification.extras.getString(Notification.EXTRA_TITLE));
        assertTrue(notification.extras.getString(Notification.EXTRA_TEXT).contains("Rosita"));
        // Check if the last exit time is updated
        assertTrue(prefs.getLong(NotificationActivity.LAST_EXIT_TIME_KEY, 0) > lastExitTime);
        // Check if the last xp to deduct is updated
        assertTrue(prefs.getLong("last_xp_to_deduct", 0) > 0);
    }

    @Test
    public void testDoWorkNoNotificationSentIfNoSelectedPlant() throws InterruptedException, ExecutionException {
        // Clear the selected plant from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("selectedPlant");
        editor.apply();
        // Set the last exit time to 3 hours ago
        long lastExitTime = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        prefs.edit().putLong(NotificationActivity.LAST_EXIT_TIME_KEY, lastExitTime).apply();
        // Set the last resume time to 1 hour ago
        long lastResumeTime = System.currentTimeMillis() - (1 * 60 * 60 * 1000);
        prefs.edit().putLong("last_resume_time", lastResumeTime).apply();
        // Create a work request
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
        // Enqueue the work request
        workManager.enqueue(workRequest).getResult().get();
        // Get the work info
        WorkInfo workInfo = workManager.getWorkInfoById(workRequest.getId()).get();
        // Check if the work is successful
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
        // Check if no notification was sent
        assertEquals(0, notificationManager.getActiveNotifications().length);
    }
    @Test
    public void testDoWorkNoNotificationSentIfXpToDeductIsNotGreaterThanLastXpToDeduct() throws InterruptedException, ExecutionException {
        // Set the last exit time to 3 hours ago
        long lastExitTime = System.currentTimeMillis() - (3 * 60 * 60 * 1000);
        prefs.edit().putLong(NotificationActivity.LAST_EXIT_TIME_KEY, lastExitTime).apply();
        // Set the last resume time to 1 hour ago
        long lastResumeTime = System.currentTimeMillis() - (1 * 60 * 60 * 1000);
            prefs.edit().putLong("last_resume_time", lastResumeTime).apply();
        // Set the last xp to deduct to 100
            prefs.edit().putLong("last_xp_to_deduct", 100).apply();
        // Create a work request
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
        // Enqueue the work request
            workManager.enqueue(workRequest).getResult().get();
        // Get the work info
        WorkInfo workInfo = workManager.getWorkInfoById(workRequest.getId()).get();
        // Check if the work is successful
        assertEquals(WorkInfo.State.SUCCEEDED, workInfo.getState());
        // Check if no notification was sent
        assertEquals(0, notificationManager.getActiveNotifications().length);
    }
    
    @Test
    public void testSendUsageNotification() {
        // Create the worker
        NotificationWorker worker = TestListenableWorkerBuilder.from(context, NotificationWorker.class).build();
        // Send a notification
        worker.sendUsageNotification(100, "Rosita");
        // Check if a notification was sent
        Assert.assertEquals(1, notificationManager.getActiveNotifications().length);
        // Check if the notification has the correct content
        Notification notification = notificationManager.getActiveNotifications()[0].getNotification();
        assertEquals("Alerta", notification.extras.getString(Notification.EXTRA_TITLE));
        assertTrue(notification.extras.getString(Notification.EXTRA_TEXT).contains("Rosita"));
        assertTrue(notification.extras.getString(Notification.EXTRA_TEXT).contains("100"));
    }
    
    @Test
    public void testCreateNotificationChannel() {
        // Create the worker
        NotificationWorker worker = TestListenableWorkerBuilder.from(context, NotificationWorker.class).build();
        // Create the notification channel
        worker.createNotificationChannel();
        // Check if the channel was created
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel("notification_channel");
            assertNotNull(channel);
            assertEquals("Notificaciones de la App", channel.getName());
        }
    }
}