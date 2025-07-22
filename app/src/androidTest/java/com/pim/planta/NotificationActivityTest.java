package com.pim.planta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.base.NotificationActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class NotificationActivityTest {

    @Rule
    public ActivityScenarioRule<DummyNotificationActivity> activityScenarioRule = new ActivityScenarioRule<>(DummyNotificationActivity.class);
    private Context context;
    private SharedPreferences prefs;
    private NotificationManager notificationManager;

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
    }

    @After
    public void tearDown() {
        // Clear SharedPreferences after each test
        prefs.edit().clear().apply();
        // Clear any existing notifications
        notificationManager.cancelAll();
    }
    @Test
    public void testOnResumeResetsTimersAndNotifications() {
        // Set some values in SharedPreferences
        prefs.edit().putLong(NotificationActivity.TOTAL_USAGE_TIME_KEY, 10000)
                .putBoolean(NotificationActivity.FIFTEEN_MINUTES_NOTIFICATION_SENT_KEY, true)
                .putBoolean(NotificationActivity.TWO_HOURS_NOTIFICATION_SENT_KEY, true)
                .apply();
        // Create a notification
        notificationManager.notify(NotificationActivity.NOTIFICATION_ID, new android.app.Notification.Builder(context, "channel_id").build());
        // Launch the activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            // Call onResume
            activity.onResume();
            // Check if the values are reset
            assertEquals(0, prefs.getLong(NotificationActivity.TOTAL_USAGE_TIME_KEY, -1));
            assertFalse(prefs.getBoolean(NotificationActivity.FIFTEEN_MINUTES_NOTIFICATION_SENT_KEY, true));
            assertFalse(prefs.getBoolean(NotificationActivity.TWO_HOURS_NOTIFICATION_SENT_KEY, true));
            // Check if the notification is canceled
            assertEquals(0, notificationManager.getActiveNotifications().length);
        });
    }
    @Test
    public void testOnPauseStoresLastExitTime() {
        // Launch the activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            // Call onPause
            activity.onPause();
            // Check if the last exit time is stored
            assertTrue(prefs.contains(NotificationActivity.LAST_EXIT_TIME_KEY));
        });
    }
    @Test
    public void testResetTimersAndNotifications() {
        // Set some values in SharedPreferences
        prefs.edit().putLong(NotificationActivity.TOTAL_USAGE_TIME_KEY, 10000)
                .putBoolean(NotificationActivity.FIFTEEN_MINUTES_NOTIFICATION_SENT_KEY, true)
                .putBoolean(NotificationActivity.TWO_HOURS_NOTIFICATION_SENT_KEY, true)
                .apply();
        // Create a notification
        notificationManager.notify(NotificationActivity.NOTIFICATION_ID, new android.app.Notification.Builder(context, "channel_id").build());
        // Launch the activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            // Call resetTimersAndNotifications
            activity.resetTimersAndNotifications();
            // Check if the values are reset
            assertEquals(0, prefs.getLong(NotificationActivity.TOTAL_USAGE_TIME_KEY, -1));
            assertFalse(prefs.getBoolean(NotificationActivity.FIFTEEN_MINUTES_NOTIFICATION_SENT_KEY, true));
            assertFalse(prefs.getBoolean(NotificationActivity.TWO_HOURS_NOTIFICATION_SENT_KEY, true));
            // Check if the notification is canceled
            assertEquals(0, notificationManager.getActiveNotifications().length);
        });
    }
}