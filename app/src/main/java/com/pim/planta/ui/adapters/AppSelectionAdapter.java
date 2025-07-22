package com.pim.planta.ui.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pim.planta.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionAdapter extends RecyclerView.Adapter<AppSelectionAdapter.AppViewHolder> {

    private final List<ApplicationInfo> appList;
    private final PackageManager packageManager;
    private final Set<String> selectedPackages = new HashSet<>();

    public AppSelectionAdapter(Context context, List<ApplicationInfo> apps) {
        this.appList = apps;
        this.packageManager = context.getPackageManager();
    }

    public Set<String> getSelectedPackages() {
        return selectedPackages;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_selectable, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ApplicationInfo appInfo = appList.get(position);
        String appName = (String) appInfo.loadLabel(packageManager);
        Drawable appIcon = appInfo.loadIcon(packageManager);
        String packageName = appInfo.packageName;

        holder.textViewAppName.setText(appName);
        holder.imageViewAppIcon.setImageDrawable(appIcon);
        holder.checkBox.setChecked(selectedPackages.contains(packageName));

        holder.itemView.setOnClickListener(v -> {
            if (selectedPackages.contains(packageName)) {
                selectedPackages.remove(packageName);
                holder.checkBox.setChecked(false);
            } else {
                selectedPackages.add(packageName);
                holder.checkBox.setChecked(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewAppIcon;
        TextView textViewAppName;
        CheckBox checkBox;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAppIcon = itemView.findViewById(R.id.imageViewAppIcon);
            textViewAppName = itemView.findViewById(R.id.textViewAppName);
            checkBox = itemView.findViewById(R.id.checkBoxApp);
        }
    }
}
