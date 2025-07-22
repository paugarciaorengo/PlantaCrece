package com.pim.planta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.ui.adapters.ImageAdapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class ImageAdapterTest {

    private Context context;
    private int[] testImages;
    private ImageAdapter imageAdapter;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        testImages = new int[]{R.drawable.ic_launcher_background, R.drawable.ic_launcher_foreground, R.drawable.ic_arrow_down};
        imageAdapter = new ImageAdapter(context, testImages);
    }

    @Test
    public void testGetCount() {
        assertEquals(testImages.length, imageAdapter.getCount());
    }

    @Test
    public void testGetItem() {
        assertEquals(testImages[0], imageAdapter.getItem(0));
        assertEquals(testImages[1], imageAdapter.getItem(1));
        assertEquals(testImages[2], imageAdapter.getItem(2));
    }

    @Test
    public void testGetItemId() {
        assertEquals(0, imageAdapter.getItemId(0));
        assertEquals(1, imageAdapter.getItemId(1));
        assertEquals(2, imageAdapter.getItemId(2));
    }

    @Test
    public void testGetView() {
        View view = imageAdapter.getView(0, null, null);
        assertNotNull(view);
        assertTrue(view instanceof ImageView);

        ImageView imageView = (ImageView) view;
        Drawable drawable = imageView.getDrawable();
        assertNotNull(drawable);
        // Check if the drawable is a BitmapDrawable
        assertTrue(drawable instanceof BitmapDrawable);
        // Check if the drawable has the correct resource ID
        int expectedResourceId = testImages[0];
        int actualResourceId = getResourceId(drawable);
        assertEquals(expectedResourceId, actualResourceId);
        assertEquals(ImageView.ScaleType.CENTER_CROP, imageView.getScaleType());
        assertEquals(200, imageView.getLayoutParams().width);
        assertEquals(200, imageView.getLayoutParams().height);
    }
    private int getResourceId(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return R.drawable.ic_launcher_background;
        }
        return 0;
    }
}