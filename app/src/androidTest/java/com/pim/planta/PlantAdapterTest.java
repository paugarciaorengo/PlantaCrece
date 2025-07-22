package com.pim.planta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.pim.planta.db.DAO;
import com.pim.planta.models.Plant;
import com.pim.planta.ui.adapters.PlantAdapter;
import com.pim.planta.models.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@Config(sdk = {Build.VERSION_CODES.O_MR1})
public class PlantAdapterTest {

    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private Context context;
    private Typeface aventaFont;
    @Mock
    private DAO dao;
    @Mock
    private User user;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        context = ApplicationProvider.getApplicationContext();
        aventaFont = Typeface.DEFAULT;
        plantList = new ArrayList<>();
        plantList.add(new Plant("Plant 1", "basePath", R.drawable.ic_launcher_foreground, 100, 200, "Description 1", "Scientific 1"));
        plantList.add(new Plant("Plant 2", "basePath", R.drawable.ic_launcher_foreground, 150, 250, "Description 2", "Scientific 2"));
        plantAdapter = new PlantAdapter(context, plantList, aventaFont, dao, user);
    }

    @Test
    public void testOnCreateViewHolder() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View parent = inflater.inflate(R.layout.plant_item, null, false);
        PlantAdapter.PlantViewHolder viewHolder = plantAdapter.onCreateViewHolder(null, 0);
        assertNotNull(viewHolder);
        assertNotNull(viewHolder.plantNameTextView);
        assertNotNull(viewHolder.plantImageView);
        assertNotNull(viewHolder.plantDescriptionTextView);
        assertNotNull(viewHolder.plantGrowthCountTextView);
    }

    @Test
    public void testOnBindViewHolder() {
        Plant plant = plantList.get(0);
        PlantAdapter.PlantViewHolder viewHolder = mock(PlantAdapter.PlantViewHolder.class);
        when(dao.getPlantaByName(any())).thenReturn(plant);
        when(dao.getGrowCount(any(), any())).thenReturn(5);
        plantAdapter.onBindViewHolder(viewHolder, 0);
        verify(viewHolder.plantNameTextView).setText(plant.getName());
        verify(viewHolder.plantImageView).setImageResource(plant.getImageResourceId());
        verify(viewHolder.plantDescriptionTextView).setText(plant.getDescription());
        verify(viewHolder.plantGrowthCountTextView).setText("Growth count: 5");
    }

    @Test
    public void testOnBindViewHolderWithNickname() {
        Plant plant = plantList.get(0);
        plant.setNickname("Nickname");
        PlantAdapter.PlantViewHolder viewHolder = mock(PlantAdapter.PlantViewHolder.class);
        when(dao.getPlantaByName(any())).thenReturn(plant);
        when(dao.getGrowCount(any(), any())).thenReturn(5);
        plantAdapter.onBindViewHolder(viewHolder, 0);
        verify(viewHolder.plantNameTextView).setText("Nickname XP: 100/200");
        verify(viewHolder.plantImageView).setImageResource(plant.getImageResourceId());
        verify(viewHolder.plantDescriptionTextView).setText(plant.getDescription());
        verify(viewHolder.plantGrowthCountTextView).setText("Growth count: 5");
    }

    @Test
    public void testGetItemCount() {
        assertEquals(plantList.size(), plantAdapter.getItemCount());
    }

    @Test
    public void testOnItemClickListener() {
        Plant plant = plantList.get(0);
        PlantAdapter.OnItemClickListener listener = mock(PlantAdapter.OnItemClickListener.class);
        plantAdapter.setOnItemClickListener(listener);
        PlantAdapter.PlantViewHolder viewHolder = mock(PlantAdapter.PlantViewHolder.class);
        plantAdapter.onBindViewHolder(viewHolder, 0);
        verify(viewHolder.itemView).setOnClickListener(any());
    }
}