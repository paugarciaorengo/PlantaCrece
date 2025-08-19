package com.pim.planta.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.R;
import com.pim.planta.models.UserPot;

import java.util.ArrayList;
import java.util.List;

/**
 * Inventario horizontal de macetas (no colocadas).
 * Muestra cada maceta como una “card” con ripple.
 */
public class PotsInventoryAdapter extends RecyclerView.Adapter<PotsInventoryAdapter.VH> {

    /** Callback para iniciar el drag & drop desde el inventario. */
    public interface OnStartDrag { void onDrag(View view, UserPot pot); }

    private final List<UserPot> items = new ArrayList<>();
    private OnStartDrag dragCallback;

    public PotsInventoryAdapter() { }

    public PotsInventoryAdapter(OnStartDrag cb) { this.dragCallback = cb; }

    /** Establece/actualiza el callback de drag. */
    public void setOnStartDragListener(OnStartDrag cb) { this.dragCallback = cb; }

    /** Reemplaza la lista completa. */
    public void submitList(List<UserPot> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    /** Elimina del inventario una maceta por id. */
    public void removeById(String potId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(potId)) {
                items.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Contenedor tipo “card”
        FrameLayout root = new FrameLayout(parent.getContext());
        int size = parent.getResources().getDimensionPixelSize(R.dimen.pot_size);
        int pad  = parent.getResources().getDimensionPixelSize(R.dimen.pot_item_padding);
        root.setLayoutParams(new ViewGroup.LayoutParams(size, size));
        root.setPadding(pad, pad, pad, pad);
        root.setBackground(makeRoundedSelectableBg(parent.getContext(),
                parent.getResources().getDimension(R.dimen.pot_item_radius)));

        // Imagen de la maceta
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        root.addView(iv);
        return new VH(root, iv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        UserPot pot = items.get(position);

        // Usa versión *_full si hay planta asignada
        int resId = getPotDrawableForState(holder.itemView.getContext(), pot);
        if (resId == 0) resId = getResIdByName(holder.itemView.getContext(), pot.getDrawable());
        holder.iv.setImageResource(resId != 0 ? resId : R.drawable.maceta);

        holder.itemView.setContentDescription("Maceta " + (position + 1));
        holder.itemView.setLongClickable(true);
        holder.itemView.setHapticFeedbackEnabled(true);

        holder.itemView.setOnLongClickListener(v -> {
            // Solo arrastrables si no están colocadas
            if (dragCallback != null && !pot.isPlaced()) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                dragCallback.onDrag(holder.itemView, pot);
                return true;
            }
            return false;
        });
    }

    @Override public int getItemCount() { return items.size(); }

    /* -------------------- Helpers -------------------- */

    /** Crea un fondo redondeado con ripple. */
    private static RippleDrawable makeRoundedSelectableBg(Context ctx, float radiusPx) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(0x22FFFFFF); // leve fondo translúcido
        shape.setCornerRadius(radiusPx);
        return new RippleDrawable(
                ColorStateList.valueOf(0x33FFFFFF),
                shape, null
        );
    }

    /** Devuelve drawable según si hay planta asignada: base o base_full si existe. */
    private static int getPotDrawableForState(Context ctx, UserPot pot) {
        String base = pot.getDrawable(); // ej. "maceta"
        String variant = (pot.getAssignedPlantId() != null) ? base + "_full" : base;
        int res = getResIdByName(ctx, variant);
        if (res == 0) res = getResIdByName(ctx, base);
        return res;
    }

    /** Busca recurso drawable por nombre. */
    private static int getResIdByName(Context ctx, String name) {
        if (name == null || name.isEmpty()) return 0;
        return ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
    }

    /* -------------------- ViewHolder -------------------- */

    public static class VH extends RecyclerView.ViewHolder {
        public final ImageView iv;
        public VH(@NonNull View itemView, ImageView iv) { super(itemView); this.iv = iv; }
    }
}
