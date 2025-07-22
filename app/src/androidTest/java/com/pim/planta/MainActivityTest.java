package com.pim.planta;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.os.Build;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.testing.SynchronousExecutor;
import androidx.work.testing.WorkManagerTestInitHelper;

import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabasePlantoo;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.ui.activity.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);
    private DatabasePlantoo db;
    private PlantRepository plantRepo;
    private Context context;
    private DAO dao;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, DatabasePlantoo.class).allowMainThreadQueries().build();
        plantRepo = PlantRepository.getInstance(context);
        dao = db.DAO();
        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(
                context, new androidx.work.Configuration.Builder()
                        .setExecutor(new SynchronousExecutor())
                        .build()
        );
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testNavigationToLoginActivity() {
        // Click on the login button
        onView(withId(R.id.buttonEmpezar)).perform(click());
        // Check if the LoginActivity is launched
        onView(withId(R.id.activity_login)).check(matches(isDisplayed()));
    }
    @Test
    public void testDatabasePopulation() {
        // Check if the database is populated
        activityScenarioRule.getScenario().onActivity(activity -> {
            List<Plant> plantas = dao.getAllPlantas();
            assertFalse(plantas.isEmpty());
            assertEquals(4, plantas.size());
        });
    }
    @Test
    public void testDatabaseIsPopulated() {
        // Check if the database is populated
        activityScenarioRule.getScenario().onActivity(activity -> {
            List<Plant> plantas = dao.getAllPlantas();
            assertFalse(plantas.isEmpty());
            assertEquals(4, plantas.size());
            // Check if the plants are the correct ones
            assertEquals("Rosa", plantas.get(0).getName());
            assertEquals("Margarita", plantas.get(1).getName());
            assertEquals("Girasol", plantas.get(2).getName());
            assertEquals("Tulipan", plantas.get(3).getName());
        });
    }
    @Test
    public void testNotificationWorkerIsScheduled() throws InterruptedException {
        // Get the WorkManager instance
        WorkManager workManager = WorkManager.getInstance(context);

        // Create a LiveData observer
        androidx.lifecycle.Observer<List<WorkInfo>> observer = new androidx.lifecycle.Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                if (workInfos != null && !workInfos.isEmpty()) {
                    // Check if the work is in the correct state
                    assertEquals(WorkInfo.State.ENQUEUED, workInfos.get(0).getState());
                    // Remove the observer
                    workManager.getWorkInfosForUniqueWorkLiveData("NotificationWork").removeObserver(this);
                }
            }
        };

        // Observe the LiveData
        workManager.getWorkInfosForUniqueWorkLiveData("NotificationWork").observeForever(observer);

        // Wait for the observer to be called (max 5 seconds)
        Thread.sleep(5000);
    }
}