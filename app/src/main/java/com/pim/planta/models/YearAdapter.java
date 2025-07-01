package com.pim.planta.models;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.R;

public class YearAdapter extends RecyclerView.Adapter<YearAdapter.YearViewHolder> {

    private final int maximumYear;
    public int currentYear;
    private int minimumYear;
    public int holderWidth;
    public static final int EMPTY_VIEW_TYPE = 0;
    public static final int YEAR_VIEW_TYPE = 1;

    public YearAdapter(int currentYear, int minimumYear, int maximumYear) {
        this.currentYear = currentYear;
        this.minimumYear = minimumYear;
        this.maximumYear = maximumYear;
    }

    @NonNull
    @Override
    public YearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.year_item, parent, false);
        return new YearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YearViewHolder holder, int position) {
        int year = minimumYear + position - 4;

        if (getItemViewType(position) == YEAR_VIEW_TYPE && year <= maximumYear) {
            holder.yearTextView.setVisibility(View.VISIBLE);
            holder.yearTextView.setText(String.valueOf(year));
            if (year == currentYear) {
                holder.yearTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                holder.yearTextView.setTextColor(Color.WHITE);
            } else {
                holder.yearTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                holder.yearTextView.setTextColor(Color.LTGRAY);
            }
        } else {
            holder.yearTextView.setText(""); // Evita mostrar años inválidos
            holder.yearTextView.setVisibility(View.INVISIBLE);
            if (holderWidth > 0) {
                holder.yearTextView.setWidth(holderWidth);
            } else {
                holder.yearTextView.setWidth(holder.itemView.getContext().getResources()
                        .getDimensionPixelSize(R.dimen.year_selector_empty_space_width));
            }
        }
    }

    public void setHolderWidth(int holderWidth) {
        this.holderWidth = holderWidth;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (maximumYear - minimumYear + 1) + 8; // +8 for 4 empty slots before/after
    }

    @Override
    public int getItemViewType(int position) {
        int year = minimumYear + position - 4;

        if (position < 4 || position >= getItemCount() - 4 || year > maximumYear) {
            return EMPTY_VIEW_TYPE;
        } else {
            return YEAR_VIEW_TYPE;
        }
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
        notifyDataSetChanged();
    }

    public static class YearViewHolder extends RecyclerView.ViewHolder {
        public TextView yearTextView;

        YearViewHolder(View itemView) {
            super(itemView);
            yearTextView = itemView.findViewById(R.id.year_text_view);
        }
    }
}
