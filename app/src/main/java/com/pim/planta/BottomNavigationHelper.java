package com.pim.planta;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

public class BottomNavigationHelper {

    public static class Binding {
        public final ImageButton btnLupa;
        public final ImageButton btnMaceta;
        public final ImageButton btnPlantadex;
        public final ImageButton btnUsuario;

        public Binding(View rootView) {
            btnLupa = rootView.findViewById(R.id.imageButtonLupa);
            btnMaceta = rootView.findViewById(R.id.imageButtonMaceta);
            btnPlantadex = rootView.findViewById(R.id.imageButtonPlantadex);
            btnUsuario = rootView.findViewById(R.id.imageButtonUsuario);
        }
    }

    public static void setup(Activity activity, Binding binding, Class<?> currentActivity) {
        // Deshabilitar botón actual
        if (currentActivity.equals(DiaryActivity.class)) {
            disableButton(binding.btnLupa);
        } else if (currentActivity.equals(JardinActivity.class)) {
            disableButton(binding.btnMaceta);
        } else if (currentActivity.equals(PlantListActivity.class)) {
            disableButton(binding.btnPlantadex);
        } else if (currentActivity.equals(PerfilActivity.class)) {
            disableButton(binding.btnUsuario);
        }

        // Configurar listeners
        binding.btnPlantadex.setOnClickListener(v -> navigate(activity, PlantListActivity.class, v));
        binding.btnMaceta.setOnClickListener(v -> navigate(activity, JardinActivity.class, v));
        binding.btnLupa.setOnClickListener(v -> navigate(activity, DiaryActivity.class, v));
        binding.btnUsuario.setOnClickListener(v -> navigate(activity, PerfilActivity.class, v));
    }

    private static void disableButton(ImageButton button) {
        button.setEnabled(false);
        button.setImageAlpha(128);
    }

    private static void navigate(Activity activity, Class<?> target, View view) {
        if (activity.getClass().equals(target)) return;

        animateButton(view);
        Intent intent = new Intent(activity, target);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void animateButton(View view) {
        ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 0.9f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 0.9f, 1.0f)
        ).setDuration(150).start();
    }
}