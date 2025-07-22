package com.pim.planta.ui.components;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.pim.planta.R;

public class UsageMarkerView extends MarkerView {

    private final TextView markerTextView;
    private final String[] socialApps;

    public UsageMarkerView(Context context, int layoutResource, String[] socialApps) {
        super(context, layoutResource);
        this.socialApps = socialApps;
        markerTextView = findViewById(R.id.marker_text);
    }

    @Override
    public void refreshContent(com.github.mikephil.charting.data.Entry e, Highlight highlight) {
        if (e instanceof BarEntry) {
            BarEntry barEntry = (BarEntry) e;
            float[] values = barEntry.getYVals();
            if (values != null) {
                StringBuilder tooltip = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    tooltip.append(socialApps[i])
                            .append(": ")
                            .append(String.format("%.2f h", values[i]))
                            .append("\n");
                }
                markerTextView.setText(tooltip.toString().trim());
            } else {
                markerTextView.setText(String.format("%.2f h", barEntry.getY()));
            }
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        // Centrar el marcador horizontalmente
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}

