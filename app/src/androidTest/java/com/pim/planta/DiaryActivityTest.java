package com.pim.planta;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.os.Build;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.db.DatabasePlantoo;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.DiaryEntry;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.ui.activity.DiaryActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class DiaryActivityTest {

    @Rule
    public ActivityScenarioRule<DiaryActivity> activityScenarioRule = new ActivityScenarioRule<>(DiaryActivity.class);
    private DatabasePlantoo db;
    private PlantRepository plantRepo;
    private Context context;
    private User user;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, DatabasePlantoo.class).allowMainThreadQueries().build();
        plantRepo = PlantRepository.getInstance(context);
        user = new User("testUser","testEmail", "testPassword");
        user.setId(1);
        UserLogged.getInstance().setCurrentUser(user);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testSaveNewDiaryEntry() {
        // Click on a date
        onView(ViewMatchers.withId(R.id.calendar_draw)).perform(click());
        // Type text in highlightInput
        onView(ViewMatchers.withId(R.id.highlightInput)).perform(typeText("Test Highlight"), closeSoftKeyboard());
        // Type text in annotationInput
        onView(ViewMatchers.withId(R.id.annotationInput)).perform(typeText("Test Annotation"), closeSoftKeyboard());
        // Click on an emotion
        onView(ViewMatchers.withId(R.id.happyImage)).perform(click());
        // Click on save button
        onView(ViewMatchers.withId(R.id.buttonSaveEntry)).perform(click());
        // Check if the entry is saved
        DiaryEntry entry = plantRepo.getPlantaDAO().getEntradaByUserIdAndDate(user.getId(), Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
        assertNotNull(entry);
        assertEquals("Test Highlight", entry.getHighlight());
        assertEquals("Test Annotation", entry.getAnnotation());
        assertEquals(1, entry.getEmotion());
    }
    @Test
    public void testUpdateExistingDiaryEntry() {
        // Create a diary entry
        DiaryEntry entry = new DiaryEntry("Initial Highlight", "Initial Annotation", 0, user.getId(), Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
        plantRepo.getPlantaDAO().insert(entry);
        // Click on a date
        onView(ViewMatchers.withId(R.id.calendar_draw)).perform(click());
        // Type text in highlightInput
        onView(ViewMatchers.withId(R.id.highlightInput)).perform(typeText("Updated Highlight"), closeSoftKeyboard());
        // Type text in annotationInput
        onView(ViewMatchers.withId(R.id.annotationInput)).perform(typeText("Updated Annotation"), closeSoftKeyboard());
        // Click on an emotion
        onView(ViewMatchers.withId(R.id.sadImage)).perform(click());
        // Click on save button
        onView(ViewMatchers.withId(R.id.buttonSaveEntry)).perform(click());
        // Check if the entry is updated
        DiaryEntry updatedEntry = plantRepo.getPlantaDAO().getEntradaByUserIdAndDate(user.getId(), Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime());
        assertNotNull(updatedEntry);
        assertEquals("Updated Highlight", updatedEntry.getHighlight());
        assertEquals("Updated Annotation", updatedEntry.getAnnotation());
        assertEquals(3, updatedEntry.getEmotion());
    }
}