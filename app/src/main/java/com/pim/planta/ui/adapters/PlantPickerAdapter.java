package com.pim.planta.ui.adapters;

import android.content.res.Resources;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Selector visual de plantas para el BottomSheet de Invernadero.
 * Muestra tarjetas (imagen + título) y devuelve el item seleccionado vía callback.
 */
public class PlantPickerAdapter extends RecyclerView.Adapter<PlantPickerAdapter.VH> {

    /** Callback cuando el usuario elige una planta. */
    public interface OnPick { void onPick(PlantCard item); }

    private final List<PlantCard> data = new ArrayList<>();
    private final OnPick callback;

    public PlantPickerAdapter(List<PlantCard> initial, OnPick callback) {
        if (initial != null) data.addAll(initial);
        this.callback = callback;
    }

    /** Reemplaza el contenido de la lista y refresca. */
    public void submitList(List<PlantCard> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_plant, parent, false);

        // Tamaño y márgenes desde dimens
        Resources res = parent.getResources();
        int size = res.getDimensionPixelSize(R.dimen.plant_card_size);
        int pad  = res.getDimensionPixelSize(R.dimen.pot_item_padding);

        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(size, size);
        lp.setMargins(pad, pad, pad, pad);
        v.setLayoutParams(lp);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PlantCard it = data.get(position);
        h.title.setText(it.title);

        // Imagen: si no hay recurso válido, usa un fallback
        int img = it.imgRes != 0 ? it.imgRes : R.drawable.image_margarita;
        h.image.setImageResource(img);

        h.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (callback != null) callback.onPick(it);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.plantImage);
            title = itemView.findViewById(R.id.plantTitle);
        }
    }

    /** DTO simple usado por el adapter. */
    public static class PlantCard {
        public final String plantId;
        public final String title;
        public final int imgRes;

        public PlantCard(String plantId, String title, int imgRes) {
            this.plantId = plantId;
            this.title = title;
            this.imgRes = imgRes;
        }
    }
}