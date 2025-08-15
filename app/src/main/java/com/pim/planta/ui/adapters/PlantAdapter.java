package com.pim.planta.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.R;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.models.Plant;
import com.pim.planta.models.User;
import com.pim.planta.models.UserPlantRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private Context context;
    private PlantooRepository plantooRepository;
    private List<Plant> plantList;
    private Map<String, UserPlantRelation> userRelationMap;
    private OnItemClickListener onItemClickListener;
    private Typeface aventaFont;
    private User user;

    public PlantAdapter(Context context, List<Plant> plantList, Typeface font,
                        PlantooRepository repository, User user, List<UserPlantRelation> relations) {
        this.context = context;
        this.plantList = plantList;
        this.aventaFont = font;
        this.plantooRepository = repository;
        this.user = user;
        this.userRelationMap = new HashMap<>();
        for (UserPlantRelation rel : relations) {
            this.userRelationMap.put(rel.getPlantId(), rel);
        }
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_item, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        holder.plantImageView.setImageResource(plant.getImageResourceId());
        holder.plantDescriptionTextView.setText(plant.getDescription());

        UserPlantRelation rel = userRelationMap.get(plant.getId());

        if (rel != null) {
            String nickname = rel.getNickname();
            int xp = rel.getXp();
            int xpMax = plant.getXpMax();

            if (nickname != null && !nickname.isEmpty()) {
                holder.plantNameTextView.setText(
                        context.getString(R.string.xp_format, nickname, xp, xpMax)
                );
            } else {
                holder.plantNameTextView.setText(plant.getName());
            }

            holder.plantGrowthCountTextView.setText(
                    context.getString(R.string.growth_count_label) + rel.getGrowCount()
            );
        } else {
            holder.plantNameTextView.setText(plant.getName());
            holder.plantGrowthCountTextView.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(plant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public class PlantViewHolder extends RecyclerView.ViewHolder {
        public TextView plantNameTextView;
        public ImageView plantImageView;
        public TextView plantDescriptionTextView;
        public TextView plantGrowthCountTextView;

        public PlantViewHolder(View itemView) {
            super(itemView);
            plantNameTextView = itemView.findViewById(R.id.plant_name_textview);
            plantImageView = itemView.findViewById(R.id.plant_imageview);
            plantDescriptionTextView = itemView.findViewById(R.id.plant_description_textview);
            plantGrowthCountTextView = itemView.findViewById(R.id.plant_growth_count);

            plantNameTextView.setTypeface(aventaFont);
            plantDescriptionTextView.setTypeface(aventaFont);
            plantGrowthCountTextView.setTypeface(aventaFont);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Plant plant);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updatePlantList(List<Plant> newPlants) {
        this.plantList = new ArrayList<>(newPlants);
        notifyDataSetChanged();
    }
}
