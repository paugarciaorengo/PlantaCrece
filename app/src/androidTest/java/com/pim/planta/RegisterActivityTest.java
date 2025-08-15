package com.pim.planta;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.db.DAO;
import com.pim.planta.models.Plant;
import com.pim.planta.ui.activity.RegisterActivity;

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
public class RegisterActivityTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityScenarioRule = new ActivityScenarioRule<>(RegisterActivity.class);

    @Mock
    private DAO mockDao;
    @Mock
    private PlantRepository mockPlantRepository;
    private Context context;
    private View decorView;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        // Mock the DAO
        mockPlantRepository = mock(PlantRepository.class);
        mockDao = mock(DAO.class);
        when(mockPlantRepository.getPlantaDAO()).thenReturn(mockDao);
        List<Plant> plantList = new ArrayList<>();
        plantList.add(new Plant("Rosatest","",1,1,1,"",""));
        plantList.add(new Plant("Tulipan","",1,1,1,"",""));
        when(mockDao.getAllPlantas()).thenReturn(plantList);
        activityScenarioRule.getScenario().onActivity(activity -> {
            decorView = activity.getWindow().getDecorView();
        });
    }

    @Test
    public void testEmptyFields() {
        // Act
        onView(withId(R.id.buttonRegister)).perform(click());
        // Assert
        onView(withText("Por favor, completa todos los campos")).inRoot(withDecorView(not(decorView))).check(matches(isDisplayed()));
    }
}