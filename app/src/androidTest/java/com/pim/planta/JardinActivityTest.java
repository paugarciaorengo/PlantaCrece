package com.pim.planta;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabasePlantoo;
import com.pim.planta.helpers.CooldownManager;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.ui.activity.JardinActivity;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class JardinActivityTest {

    @Rule
    public ActivityScenarioRule<JardinActivity> activityScenarioRule = new ActivityScenarioRule<>(JardinActivity.class);
    private DatabasePlantoo db;
    private PlantRepository plantRepo;
    private Context context;
    private User user;
    private DAO dao;
    private Plant plant;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, DatabasePlantoo.class).allowMainThreadQueries().build();
        plantRepo = PlantRepository.getInstance(context);
        dao = db.DAO();
        user = new User("testUser","testEmail", "testPassword");
        dao.insert(user);
        // Get the user id from the database
        List<User> users = dao.getAllUsuarios();
        for (User u : users) {
            if (u.getUsername().equals("testUser")) {
                user.setId(u.getId());
                break;
            }
        }
        UserLogged.getInstance().setCurrentUser(user);
        // Add some plants to the database
        plant = new Plant("Rosatest","image_rosa",1,1,1,"This is a rose","image_rosa_");
        plant.setBasePath("image_rosa_");
        plant.setXpMax(1000);
        plant.setDescription("This is a rose");
        Plant girasol = new Plant("Girasol","image_girasol",2,2,2,"This is a sunflower","image_girasol_");
        dao.insert(plant);
        dao.insert(girasol);
        // Add some relations to the database
        dao.insertUserPlantRelation(user.getId(), plant.getId());
        dao.insertUserPlantRelation(user.getId(), girasol.getId());
        // Add the selected plant to the SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedPlant", plant.getName());
        editor.apply();
    }

    @After
    public void tearDown() {
        db.close();
    }
    @Test
    public void testWateringButtonClick() {
        // Click on the watering button
        Espresso.onView(ViewMatchers.withId(R.id.icon_water)).perform(ViewActions.click());
        // Check if the plant's XP has increased
        activityScenarioRule.getScenario().onActivity(activity -> {
            Plant updatedPlant = activity.plant;
            Assert.assertNotNull(updatedPlant);
            Assert.assertEquals(300, updatedPlant.getXp());
        });
    }
    @Test
    public void testPaddingButtonClick() {
        // Click on the padding button
        Espresso.onView(ViewMatchers.withId(R.id.icon_gesture)).perform(ViewActions.click());
        // Check if the plant's XP has increased
        activityScenarioRule.getScenario().onActivity(activity -> {
            Plant updatedPlant = activity.plant;
            Assert.assertNotNull(updatedPlant);
            Assert.assertEquals(5, updatedPlant.getXp());
        });
    }
    @Test
    public void testShowPlantGrownPopup() {
        // Set the plant's XP to the maximum
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.plant.setXp(activity.plant.getXpMax());
            activity.updatePlantFromDB();
        });
        // Check if the popup is displayed
        Espresso.onView(ViewMatchers.withId(R.id.popup_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testGetPlantFromDB() {
        // Get the plant from the database
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.getPlantFromDB();
            Assert.assertNotNull(activity.plant);
            Assert.assertEquals(plant.getName(), activity.plant.getName());
        });
    }
    @Test
    public void testUpdatePlantFromDB() {
        // Update the plant's XP
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.plant.addXp(100);
            activity.updatePlantFromDB();
        });
        // Check if the plant's XP has been updated in the database
        activityScenarioRule.getScenario().onActivity(activity -> {
            Plant updatedPlant = dao.getPlantaById(plant.getId());
            Assert.assertNotNull(updatedPlant);
            Assert.assertEquals(100, updatedPlant.getXp());
        });
    }
    @Test
    public void testCalculateUsageSinceLastResume() {
        // Set the last resume time
        long lastResumeTime = System.currentTimeMillis() - 60000; // 1 minute ago
        // Calculate the usage since the last resume
        activityScenarioRule.getScenario().onActivity(activity -> {
            long usage = activity.calculateUsageSinceLastResume(lastResumeTime, System.currentTimeMillis());
            // Check if the usage is greater than 0
            // This is a basic check, you might need to add more specific checks
            // depending on your usage calculation logic
            assertTrue(usage > 0);
        });
    }
    @Test
    public void testPenalizeIfUsageIncreased() {
        // Penalize the plant
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.penalizeIfUsageIncreased(-100);
            // Check if the plant's XP has decreased
            Assert.assertEquals(0, activity.plant.getXp());
        });
    }
    @Test
    public void testSharedPreferencesUsage() {
        // Get the SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        // Check if the SharedPreferences is not null
        Assert.assertNotNull(sharedPreferences);
    }
    @Test
    public void testCooldownManager() {
        // Get the CooldownManager
        activityScenarioRule.getScenario().onActivity(activity -> {
            CooldownManager cooldownManager = activity.cooldownManager;
            // Check if the CooldownManager is not null
            Assert.assertNotNull(cooldownManager);
        });
    }
    @Test
    public void testOnKeyDownWatering() {
        // Simulate pressing the 'O' key
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.onKeyDown(android.view.KeyEvent.KEYCODE_O, new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_O));
            // Check if the plant's XP has increased
            Assert.assertEquals(300, activity.plant.getXp());
        });
    }
    @Test
    public void testOnKeyDownPenalize() {
        // Simulate pressing the 'P' key
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.onKeyDown(android.view.KeyEvent.KEYCODE_P, new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_P));
            // Check if the plant's XP has decreased
            Assert.assertEquals(-300, activity.plant.getXp());
        });
    }
    @Test
    public void testOnKeyDownAddInstagramTime() {
        // Simulate pressing the 'U' key
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.onKeyDown(android.view.KeyEvent.KEYCODE_U, new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_U));
            // Check if the Instagram time has increased
            SharedPreferences sharedPreferences = context.getSharedPreferences("AppUsageData", Context.MODE_PRIVATE);
            long instagramTime = sharedPreferences.getLong(new java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(new java.util.Date()) + "_Instagram", 0);
            Assert.assertEquals(60000, instagramTime);
        });
    }
}