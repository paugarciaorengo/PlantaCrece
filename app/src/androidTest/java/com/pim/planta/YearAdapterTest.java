package com.pim.planta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.models.YearAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class YearAdapterTest {

    private YearAdapter yearAdapter;
    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        yearAdapter = new YearAdapter(2025, 2020);
    }

    @Test
    public void testOnCreateViewHolder() {
        YearAdapter.YearViewHolder viewHolder = yearAdapter.onCreateViewHolder(null, 0);
        assertNotNull(viewHolder);
        assertNotNull(viewHolder.yearTextView);
    }

    @Test
    public void testOnBindViewHolderCurrentYear() {
        YearAdapter.YearViewHolder viewHolder = mock(YearAdapter.YearViewHolder.class);
        TextView textView = new TextView(context);
        viewHolder.yearTextView = textView;
        yearAdapter.onBindViewHolder(viewHolder, 4);
        assertEquals("2024", viewHolder.yearTextView.getText().toString());
        assertEquals(Color.WHITE, viewHolder.yearTextView.getCurrentTextColor());
        assertEquals(24, TypedValue.COMPLEX_UNIT_SP, viewHolder.yearTextView.getTextSize());
    }

    @Test
    public void testOnBindViewHolderOtherYear() {
        YearAdapter.YearViewHolder viewHolder = mock(YearAdapter.YearViewHolder.class);
        TextView textView = new TextView(context);
        viewHolder.yearTextView = textView;
        yearAdapter.onBindViewHolder(viewHolder, 5);
        assertEquals("2021", viewHolder.yearTextView.getText().toString());
        assertEquals(Color.LTGRAY, viewHolder.yearTextView.getCurrentTextColor());
        assertEquals(16, TypedValue.COMPLEX_UNIT_SP, viewHolder.yearTextView.getTextSize());
    }

    @Test
    public void testOnBindViewHolderEmptySpace() {
        YearAdapter.YearViewHolder viewHolder = mock(YearAdapter.YearViewHolder.class);
        TextView textView = new TextView(context);
        viewHolder.yearTextView = textView;
        yearAdapter.onBindViewHolder(viewHolder, 0);
        assertEquals(View.INVISIBLE, viewHolder.yearTextView.getVisibility());
    }

    @Test
    public void testGetItemCount() {
        assertEquals(9, yearAdapter.getItemCount());
    }

    @Test
    public void testGetItemViewType() {
        assertEquals(YearAdapter.EMPTY_VIEW_TYPE, yearAdapter.getItemViewType(0));
        assertEquals(YearAdapter.EMPTY_VIEW_TYPE, yearAdapter.getItemViewType(3));
        assertEquals(YearAdapter.YEAR_VIEW_TYPE, yearAdapter.getItemViewType(4));
        assertEquals(YearAdapter.YEAR_VIEW_TYPE, yearAdapter.getItemViewType(5));
        assertEquals(YearAdapter.EMPTY_VIEW_TYPE, yearAdapter.getItemViewType(8));
    }

    @Test
    public void testSetCurrentYear() {
        yearAdapter.setCurrentYear(2025);
        assertEquals(2025, yearAdapter.currentYear);
    }
    @Test
    public void testSetHolderWidth() {
        yearAdapter.setHolderWidth(100);
        assertEquals(100, yearAdapter.holderWidth);
    }
}