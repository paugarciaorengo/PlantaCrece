package com.pim.planta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.helpers.CooldownManager;
import com.pim.planta.test.TestApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1}, application = TestApplication.class)
public class CooldownManagerTest {

    private CooldownManager cooldownManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        sharedPreferences = context.getSharedPreferences("cooldown_prefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cooldownManager = new CooldownManager(context);
        sharedPreferences.edit().clear().commit();
    }

    @Test
    public void testRecordPadUsage() {
        cooldownManager.recordPadUsage();
        long lastUsedTimestamp = sharedPreferences.getLong("last_pad_used_timestamp", 0);
        assertTrue(lastUsedTimestamp > 0);
    }

    @Test
    public void testIsPadCooldownActiveNeverUsed() {
        assertFalse(cooldownManager.isPadCooldownActive());
    }

    @Test
    public void testIsPadCooldownActiveWithinCooldown() {
        long currentTime = System.currentTimeMillis();
        editor.putLong("last_pad_used_timestamp", currentTime - (4 * 60 * 1000)); // 4 minutes ago
        editor.apply();
        assertTrue(cooldownManager.isPadCooldownActive());
    }

    @Test
    public void testIsPadCooldownActiveOutsideCooldown() {
        long currentTime = System.currentTimeMillis();
        editor.putLong("last_pad_used_timestamp", currentTime - (6 * 60 * 1000)); // 6 minutes ago
        editor.apply();
        assertFalse(cooldownManager.isPadCooldownActive());
    }

    @Test
    public void testGetRemainingPadCooldownTimeNeverUsed() {
        assertNull(cooldownManager.getRemainingPadCooldownTime());
    }

    @Test
    public void testGetRemainingPadCooldownTimeWithinCooldown() {
        long currentTime = System.currentTimeMillis();
        long pastTime = currentTime - (2 * 60 * 1000); // 2 minutes ago
        editor.putLong("last_pad_used_timestamp", pastTime);
        editor.apply();
        String remainingTime = cooldownManager.getRemainingPadCooldownTime();
        long remainingTimeMillis = CooldownManager.parseTime(remainingTime);
        long expectedRemainingTimeMillis = 3 * 60 * 1000;
        long lowerBound = expectedRemainingTimeMillis - 1000;
        long upperBound = expectedRemainingTimeMillis + 1000;
        assertTrue(remainingTimeMillis >= lowerBound && remainingTimeMillis <= upperBound);
    }

    @Test
    public void testGetRemainingPadCooldownTimeOutsideCooldown() {
        long currentTime = System.currentTimeMillis();
        editor.putLong("last_pad_used_timestamp", currentTime - (6 * 60 * 1000)); // 6 minutes ago
        editor.apply();
        assertEquals("00:00:00", cooldownManager.getRemainingPadCooldownTime());
    }

    @Test
    public void testRecordWateringUsage() {
        cooldownManager.recordWateringUsage();
        long lastUsedTimestamp = sharedPreferences.getLong("last_watering_used_timestamp", 0);
        assertTrue(lastUsedTimestamp > 0);
    }

    @Test
    public void testIsWateringCooldownActiveNeverUsed() {
        assertFalse(cooldownManager.isWateringCooldownActive());
    }

    @Test
    public void testIsWateringCooldownActiveWithinCooldown() {
        long currentTime = System.currentTimeMillis();
        editor.putLong("last_watering_used_timestamp", currentTime - (19 * 60 * 60 * 1000)); // 19 hours ago
        editor.apply();
        assertTrue(cooldownManager.isWateringCooldownActive());
    }

    @Test
    public void testIsWateringCooldownActiveOutsideCooldown() {
        long currentTime = System.currentTimeMillis();
        editor.putLong("last_watering_used_timestamp", currentTime - (21 * 60 * 60 * 1000)); // 21 hours ago
        editor.apply();
        assertFalse(cooldownManager.isWateringCooldownActive());
    }

    @Test
    public void testGetRemainingWateringCooldownTimeNeverUsed() {
        assertNull(cooldownManager.getRemainingWateringCooldownTime());
    }

    @Test
    public void testGetRemainingWateringCooldownTimeWithinCooldown() {
        long currentTime = System.currentTimeMillis();
        long pastTime = currentTime - (10 * 60 * 60 * 1000); // 10 hours ago
        editor.putLong("last_watering_used_timestamp", pastTime);
        editor.apply();
        String remainingTime = cooldownManager.getRemainingWateringCooldownTime();
        long remainingTimeMillis = CooldownManager.parseTime(remainingTime);
        long expectedRemainingTimeMillis = 10 * 60 * 60 * 1000;
        long lowerBound = expectedRemainingTimeMillis - 1000;
        long upperBound = expectedRemainingTimeMillis + 1000;
        assertTrue(remainingTimeMillis >= lowerBound && remainingTimeMillis <= upperBound);
    }

    @Test
    public void testGetRemainingWateringCooldownTimeOutsideCooldown() {
        long currentTime = System.currentTimeMillis();
        editor.putLong("last_watering_used_timestamp", currentTime - (21 * 60 * 60 * 1000)); // 21 hours ago
        editor.apply();
        assertEquals("00:00:00", cooldownManager.getRemainingWateringCooldownTime());
    }

    @Test
    public void testFormatRemainingTime() {
        assertEquals("00:00:00", cooldownManager.formatRemainingTime(0));
        assertEquals("00:00:01", cooldownManager.formatRemainingTime(1000));
        assertEquals("00:01:00", cooldownManager.formatRemainingTime(60 * 1000));
        assertEquals("01:00:00", cooldownManager.formatRemainingTime(60 * 60 * 1000));
        assertEquals("01:01:01", cooldownManager.formatRemainingTime(3661000));
        assertEquals("23:59:59", cooldownManager.formatRemainingTime(86399000));
    }
}