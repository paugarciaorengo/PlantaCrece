package com.pim.planta;

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
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.ToastMatcher;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.ui.activity.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule = new ActivityScenarioRule<>(LoginActivity.class);
    private DatabasePlantoo db;
    private PlantRepository plantRepo;
    private Context context;
    private User user;
    private DAO dao;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, DatabasePlantoo.class).allowMainThreadQueries().build();
        plantRepo = PlantRepository.getInstance(context);
        dao = db.DAO();
        user = new User("testUser","testUser@test.com", "testPassword");
        dao.insert(user);
        UserLogged.getInstance().setCurrentUser(user);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testLoginWithValidCredentials() {
        // Type valid credentials
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail)).perform(ViewActions.typeText("testUser@test.com"));
        Espresso.onView(ViewMatchers.withId(R.id.editTextPassword)).perform(ViewActions.typeText("testPassword"));
        // Click on the login button
        Espresso.onView(ViewMatchers.withId(R.id.buttonLogin)).perform(ViewActions.click());
        // Check if the JardinActivity is launched
        Espresso.onView(ViewMatchers.withId(R.id.activity_jardin)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testLoginWithInvalidCredentials() {
        // Type invalid credentials
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail)).perform(ViewActions.typeText("invalid@test.com"));
        Espresso.onView(ViewMatchers.withId(R.id.editTextPassword)).perform(ViewActions.typeText("invalidPassword"));
        // Click on the login button
        Espresso.onView(ViewMatchers.withId(R.id.buttonLogin)).perform(ViewActions.click());
        // Check if the toast message is displayed
        Espresso.onView(ViewMatchers.withText("Credenciales incorrectas")).inRoot(new ToastMatcher()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testNavigationToRegisterActivity() {
        // Click on the register text
        Espresso.onView(ViewMatchers.withId(R.id.textViewToRegister)).perform(ViewActions.click());
        // Check if the RegisterActivity is launched
        Espresso.onView(ViewMatchers.withId(R.id.activity_register)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testNavigationToPlantListActivity() {
        // Clear the selected plant from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("selectedPlant");
        editor.apply();
        // Type valid credentials
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail)).perform(ViewActions.typeText("testUser@test.com"));
        Espresso.onView(ViewMatchers.withId(R.id.editTextPassword)).perform(ViewActions.typeText("testPassword"));
        // Click on the login button
        Espresso.onView(ViewMatchers.withId(R.id.buttonLogin)).perform(ViewActions.click());
        // Check if the PlantListActivity is launched
        Espresso.onView(ViewMatchers.withId(R.id.activity_plantlist)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
    @Test
    public void testNavigationToJardinActivity() {
        // Add the selected plant to the SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("plant_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedPlant", "Rosa");
        editor.apply();
        // Type valid credentials
        Espresso.onView(ViewMatchers.withId(R.id.editTextEmail)).perform(ViewActions.typeText("testUser@test.com"));
        Espresso.onView(ViewMatchers.withId(R.id.editTextPassword)).perform(ViewActions.typeText("testPassword"));
        // Click on the login button
        Espresso.onView(ViewMatchers.withId(R.id.buttonLogin)).perform(ViewActions.click());
        // Check if the JardinActivity is launched
        Espresso.onView(ViewMatchers.withId(R.id.activity_jardin)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}