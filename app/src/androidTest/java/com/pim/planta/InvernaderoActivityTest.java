package com.pim.planta;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.content.Context;
import android.os.Build;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.db.DAO;
import com.pim.planta.db.DatabasePlantoo;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.ui.activity.InvernaderoActivity;

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
public class InvernaderoActivityTest {

    @Rule
    public ActivityScenarioRule<InvernaderoActivity> activityScenarioRule = new ActivityScenarioRule<>(InvernaderoActivity.class);
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
        user = new User("testUser","testEmail","testPassword");
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
        Plant rosa = new Plant("Rosatest", "Rosa", 1, 1,1,"Rosa","Rosa");
        Plant girasol = new Plant("Girasoltest","Girasol", 2, 2,2,"Girasol","Girasol");
        dao.insert(rosa);
        dao.insert(girasol);
        // Add some relations to the database
        dao.insertUserPlantRelation(user.getId(), rosa.getId());
        dao.insertUserPlantRelation(user.getId(), girasol.getId());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testClickImageViewOpensDialog() {
        // Click on an ImageView
        onView(ViewMatchers.withId(R.id.imageView9)).perform(click());
        // Check if the dialog is displayed
        onView(ViewMatchers.withId(R.id.grid_view)).check(matches(isDisplayed()));
    }
    @Test
    public void testClickImageInDialogPlacesPlantInImageView() {
        // Click on an ImageView
        onView(ViewMatchers.withId(R.id.imageView9)).perform(click());
        // Click on an image in the dialog
        onView(ViewMatchers.withId(R.id.grid_view))
                .perform(androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition(0, click()));
        // Check if the plant is placed in the ImageView
        onView(ViewMatchers.withId(R.id.imageView9)).check(matches(isDisplayed()));
    }
}