package com.pim.planta;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.db.DAO;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.ui.activity.PlantListActivity;

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
public class PlantListActivityTest {

    @Rule
    public ActivityScenarioRule<PlantListActivity> activityScenarioRule = new ActivityScenarioRule<>(PlantListActivity.class);

    @Mock
    private DAO mockDao;
    @Mock
    private PlantRepository mockPlantRepository;
    private Context context;
    private SharedPreferences plantPrefs;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        plantPrefs = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        // Clear SharedPreferences before each test
        plantPrefs.edit().clear().apply();
        // Mock the DAO
        mockPlantRepository = mock(PlantRepository.class);
        mockDao = mock(DAO.class);
        when(mockPlantRepository.getPlantaDAO()).thenReturn(mockDao);
    }

    @Test
    public void testPlantListIsDisplayed() {
        // Arrange
        List<Plant> plantList = new ArrayList<>();
        plantList.add(new Plant("Rosatest","",1,1,1,"test","test"));
        plantList.add(new Plant("Tulipantest","",1,1,1,"test","test"));
        when(mockDao.getAllPlantas()).thenReturn(plantList);
        // Act
        activityScenarioRule.getScenario().onActivity(activity -> {
            // Do nothing, the activity will load the plants
        });
        // Assert
        onView(withId(R.id.plant_list_recyclerview)).check(matches(isDisplayed()));
    }
}