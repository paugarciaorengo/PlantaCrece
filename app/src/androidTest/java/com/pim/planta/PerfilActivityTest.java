package com.pim.planta;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabasePlantoo;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.UserLogged;
import com.pim.planta.ui.activity.PerfilActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class PerfilActivityTest {

    @Rule
    public ActivityScenarioRule<PerfilActivity> activityScenarioRule = new ActivityScenarioRule<>(PerfilActivity.class);

    @Rule
    public GrantPermissionRule permissionRuleRead = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionRuleUsage = GrantPermissionRule.grant(Manifest.permission.PACKAGE_USAGE_STATS);

    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences plantPrefs;
    private Plant plant;
    private DAO dao;
    private PlantRepository plantRepo;

    @Mock
    private UsageStatsManager mockUsageStatsManager;
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences("AppUsageData", Context.MODE_PRIVATE);
        plantPrefs = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        // Clear SharedPreferences before each test
        prefs.edit().clear().apply();
        plantPrefs.edit().clear().apply();
        // Initialize the database
        DatabasePlantoo db = androidx.room.Room.inMemoryDatabaseBuilder(context, DatabasePlantoo.class).allowMainThreadQueries().build();
        plantRepo = PlantRepository.getInstance(context);
        dao = db.DAO();
        // Add some plants to the database
        plant = new Plant("Rosatest","",1,1,1,"","");
        plant.setBasePath("image_rosa_");
        plant.setXpMax(1000);
        plant.setDescription("This is a rose");
        plant.setNickname("Rosita");
        plant.setScientificName("Rosa sp.");
        dao.insert(plant);
        // Add the selected plant to the SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedPlant", plant.getName());
        editor.apply();
        // Initialize Intents for testing
        Intents.init();
        // Mock the UsageStatsManager
        mockUsageStatsManager = mock(UsageStatsManager.class);
        // Set the mock UsageStatsManager in the context
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("appops set " + context.getPackageName() + " android:get_usage_stats allow");
    }
    @After
    public void tearDown() {
        // Clear SharedPreferences after each test
        prefs.edit().clear().apply();
        plantPrefs.edit().clear().apply();
        // Release Intents
        Intents.release();
    }
    @Test
    public void testProfileImageClickOpensGallery() {
        // Arrange
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.profile_image)).perform(ViewActions.click());
        // Assert
        intended(hasAction(Intent.ACTION_PICK));
    }
    @Test
    public void testUserNameDisplayedCorrectly() {
        // Arrange
        // Act
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.user_name)).check(ViewAssertions.matches(ViewMatchers.withText(UserLogged.getInstance().getCurrentUser().getUsername())));
    }
    @Test
    public void testGraphIsInitialized() {
        // Arrange
        // Act
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.bar_chart)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testAppUsageSummaryIsUpdated() {
        // Arrange
        // Create mock UsageStats objects
        List<UsageStats> usageStatsList = new ArrayList<>();
        usageStatsList.add(createMockUsageStats("com.instagram.android", 10000));
        usageStatsList.add(createMockUsageStats("com.zhiliaoapp.musically", 20000));
        usageStatsList.add(createMockUsageStats("com.google.android.youtube", 30000));
        usageStatsList.add(createMockUsageStats("com.twitter.android", 40000));
        usageStatsList.add(createMockUsageStats("com.facebook.katana", 50000));
        // Mock the queryUsageStats method
        when(mockUsageStatsManager.queryUsageStats(anyInt(), anyLong(), anyLong())).thenReturn(usageStatsList);
        // Act
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.trackAppUsage2();
        });
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.textView4)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("Instagram:       0 h00 min"))));
        Espresso.onView(ViewMatchers.withId(R.id.textView4)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("TikTok:          0 h 00 min"))));
        Espresso.onView(ViewMatchers.withId(R.id.textView4)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("YouTube:         0 h 00 min"))));
        Espresso.onView(ViewMatchers.withId(R.id.textView4)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("Twitter:         0 h 00 min"))));
        Espresso.onView(ViewMatchers.withId(R.id.textView4)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("Facebook:        0 h 00 min"))));
    }
    @Test
    public void testPreviousWeekButtonChangesGraph() {
        // Arrange
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.buttonPreviousWeek)).perform(ViewActions.click());
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.bar_chart)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testNextWeekButtonChangesGraph() {
        // Arrange
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.buttonNextWeek)).perform(ViewActions.click());
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.bar_chart)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testBottomNavigationButtonsNavigateCorrectly() {
        // Arrange
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.imageButtonLupa)).perform(ViewActions.click());
        // Assert
        intended(org.hamcrest.Matchers.any(Intent.class));
        // Arrange
        Intents.init();
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.imageButtonMaceta)).perform(ViewActions.click());
        // Assert
        intended(org.hamcrest.Matchers.any(Intent.class));
        // Arrange
        Intents.init();
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.imageButtonPlantadex)).perform(ViewActions.click());
        // Assert
        intended(org.hamcrest.Matchers.any(Intent.class));
    }
    @Test
    public void testExpandableSection() {
        // Arrange
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.imageView12)).perform(ViewActions.click());
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.profile_image)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.imageView13)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.user_name)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.textCreationDate)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.textScientificName)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.textNickname)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        // Act
        Espresso.onView(ViewMatchers.withId(R.id.imageView12)).perform(ViewActions.click());
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.profile_image)).check(ViewAssertions.matches(org.hamcrest.Matchers.not(ViewMatchers.isDisplayed())));
        Espresso.onView(ViewMatchers.withId(R.id.imageView13)).check(ViewAssertions.matches(org.hamcrest.Matchers.not(ViewMatchers.isDisplayed())));
        Espresso.onView(ViewMatchers.withId(R.id.user_name)).check(ViewAssertions.matches(org.hamcrest.Matchers.not(ViewMatchers.isDisplayed())));
        Espresso.onView(ViewMatchers.withId(R.id.textCreationDate)).check(ViewAssertions.matches(org.hamcrest.Matchers.not(ViewMatchers.isDisplayed())));
        Espresso.onView(ViewMatchers.withId(R.id.textScientificName)).check(ViewAssertions.matches(org.hamcrest.Matchers.not(ViewMatchers.isDisplayed())));
        Espresso.onView(ViewMatchers.withId(R.id.textNickname)).check(ViewAssertions.matches(org.hamcrest.Matchers.not(ViewMatchers.isDisplayed())));
    }
    @Test
    public void testPlantDataIsDisplayed() {
        // Arrange
        // Act
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.textScientificName)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("Rosa sp."))));
        Espresso.onView(ViewMatchers.withId(R.id.textNickname)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("Rosita"))));
    }
    @Test
    public void testPlantDataIsNotDisplayed() {
        // Arrange
        plantPrefs.edit().remove("selectedPlant").apply();
        // Act
        // Assert
        Espresso.onView(ViewMatchers.withId(R.id.textScientificName)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("ERROR ERROR ERROR"))));
        Espresso.onView(ViewMatchers.withId(R.id.textNickname)).check(ViewAssertions.matches(ViewMatchers.withText(org.hamcrest.Matchers.containsString("ERROR ERROR ERROR"))));
    }
    // Helper method to create mock UsageStats objects
    private UsageStats createMockUsageStats(String packageName, long totalTimeInForeground) {
        UsageStats mockUsageStats = mock(UsageStats.class);
        when(mockUsageStats.getPackageName()).thenReturn(packageName);
        when(mockUsageStats.getTotalTimeInForeground()).thenReturn(totalTimeInForeground);
        return mockUsageStats;
    }
}