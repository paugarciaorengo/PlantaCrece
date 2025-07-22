package com.pim.planta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.models.CalendarDraw;
import com.pim.planta.ui.adapters.YearAdapter;
import com.pim.planta.models.YearSelectorButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.time.LocalDate;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class YearSelectorButtonTest {

    private YearSelectorButton yearSelectorButton;
    private Context context;
    @Mock
    private YearAdapter yearAdapter;
    @Mock
    private CalendarDraw calendarDraw;
    @Mock
    private YearSelectorButton.OnYearSelectedListener listener;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        yearSelectorButton = new YearSelectorButton(context);
        yearSelectorButton.yearAdapter = yearAdapter;
        yearSelectorButton.setCalendarDraw(calendarDraw);
    }

    @Test
    public void testInit() {
        assertNotNull(yearSelectorButton.yearRecyclerView);
        assertNotNull(yearSelectorButton.yearAdapter);
        assertEquals(yearSelectorButton.yearRecyclerView.getAdapter(), yearSelectorButton.yearAdapter);
        assertNotNull(yearSelectorButton.yearRecyclerView.getLayoutManager());
        assertEquals(LinearLayoutManager.HORIZONTAL, ((LinearLayoutManager) yearSelectorButton.yearRecyclerView.getLayoutManager()).getOrientation());
        assertNotNull(yearSelectorButton.snapHelper);
        assertEquals(LocalDate.now().getYear(), yearSelectorButton.getCurrentYear());
        assertEquals(2010, yearSelectorButton.getMinimumYear());
        assertEquals(View.VISIBLE, yearSelectorButton.yearRecyclerView.getVisibility());
    }

    @Test
    public void testOnYearSelected() {
        yearSelectorButton.onYearSelected(2024);
        verify(listener).onYearSelected(2024);
        verify(calendarDraw).setCurrentYear(2024);
        verify(calendarDraw).invalidate();
        verify(calendarDraw).requestLayout();
    }

    @Test
    public void testSetCurrentYear() {
        yearSelectorButton.setCurrentYear(2025);
        assertEquals(2025, yearSelectorButton.getCurrentYear());
        verify(yearAdapter).setCurrentYear(2025);
    }

    @Test
    public void testGetMinimumYear() {
        assertEquals(2010, yearSelectorButton.getMinimumYear());
    }

    @Test
    public void testSetCalendarDraw() {
        CalendarDraw calendarDraw = mock(CalendarDraw.class);
        yearSelectorButton.setCalendarDraw(calendarDraw);
        yearSelectorButton.onYearSelected(2024);
        verify(calendarDraw).setCurrentYear(2024);
    }
}