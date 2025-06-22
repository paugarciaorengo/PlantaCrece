package com.pim.planta;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantRepository;
import com.pim.planta.models.CalendarDraw;
import com.pim.planta.models.DiaryEntry;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.YearAdapter;
import com.pim.planta.models.YearSelectorButton;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DiaryActivity extends NotificationActivity {
    private BottomNavigationHelper.Binding bottomNavBinding;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault());

    private CalendarDraw calendarDraw;
    private PlantRepository plantRepo;
    private List<DiaryEntry> diaryEntries;
    private YearSelectorButton yearSelectorButton;
    private ImageButton previousMonthButton, nextMonthButton;
    private TextView dateTextView;
    private int selectedEmotion = -1;
    private EditText highlightInput, annotationInput;
    private List<ImageView> emotionImages;
    private DiaryEntry currentDiaryEntry;
    private long dateInMillis;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        // Obtener referencia al contenedor de navegación inferior
        View bottomNavView = findViewById(R.id.bottomNavigation);
        bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);

        // Configurar navegación
        BottomNavigationHelper.setup(this, bottomNavBinding, DiaryActivity.class);

        setupUiComponents();
        setupEmotionSelection();
        setupCalendarInteraction();
        loadInitialData();
    }

    private void setupUiComponents() {
        calendarDraw = findViewById(R.id.calendar_draw);
        dateTextView = findViewById(R.id.dateTextView);
        previousMonthButton = findViewById(R.id.previousMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
        highlightInput = findViewById(R.id.highlightInput);
        annotationInput = findViewById(R.id.annotationInput);
        Button saveButton = findViewById(R.id.buttonSaveEntry);

        plantRepo = PlantRepository.getInstance(this);
        calendarDraw.setVisibility(View.VISIBLE);

        previousMonthButton.setOnClickListener(v -> calendarDraw.prevMonth());
        nextMonthButton.setOnClickListener(v -> calendarDraw.nextMonth());
        saveButton.setOnClickListener(v -> saveDiaryEntry());
    }

    private void setupEmotionSelection() {
        emotionImages = Arrays.asList(
                findViewById(R.id.excitedImage),
                findViewById(R.id.happyImage),
                findViewById(R.id.neutralImage),
                findViewById(R.id.sadImage),
                findViewById(R.id.verySadImage)
        );

        for (ImageView image : emotionImages) {
            image.setAlpha(0.5f);
            image.setOnClickListener(this::onEmotionClicked);
        }
    }

    private void onEmotionClicked(View v) {
        ImageView selectedImage = (ImageView) v;
        selectedImage.setAlpha(1.0f);

        for (ImageView image : emotionImages) {
            if (image != selectedImage) {
                image.setAlpha(0.5f);
            }
        }
        selectedEmotion = emotionImages.indexOf(selectedImage);
    }

    private void setupCalendarInteraction() {
        yearSelectorButton = findViewById(R.id.year_selector_button);
        yearSelectorButton.setCalendarDraw(calendarDraw);

        yearSelectorButton.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                setupYearSelector();
                yearSelectorButton.removeOnLayoutChangeListener(this);
            }
        });

        calendarDraw.highlightDay(LocalDate.now());
        updateDateDisplay(LocalDate.now());
        dateInMillis = convertToEpochMillis(LocalDate.now());

        calendarDraw.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                handleCalendarTouch(motionEvent);
            }
            return true;
        });
    }

    private void setupYearSelector() {
        RecyclerView yearRecyclerView = yearSelectorButton.yearRecyclerView;
        YearAdapter yearAdapter = new YearAdapter(
                yearSelectorButton.getCurrentYear(),
                yearSelectorButton.getMinimumYear()
        );

        yearRecyclerView.setAdapter(yearAdapter);
        yearRecyclerView.setLayoutManager(new LinearLayoutManager(
                DiaryActivity.this,
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        yearRecyclerView.setVisibility(View.VISIBLE);
    }

    private void handleCalendarTouch(MotionEvent motionEvent) {
        int dayClicked = calendarDraw.getDayFromCoordinates(motionEvent.getX(), motionEvent.getY());

        if (dayClicked != -1) {
            handleDaySelection(dayClicked);
        } else {
            reloadCurrentDay();
        }
    }

    private void handleDaySelection(int dayClicked) {
        if (isFutureDay(dayClicked)) return;

        LocalDate selectedDate = LocalDate.of(
                calendarDraw.getCurrentYear(),
                calendarDraw.getCurrentMonth(),
                dayClicked
        );

        calendarDraw.highlightDay(selectedDate);
        updateDateDisplay(selectedDate);
        dateInMillis = convertToEpochMillis(selectedDate);
        loadDiaryEntry(dateInMillis);
    }

    private boolean isFutureDay(int day) {
        return day > LocalDate.now().getDayOfMonth() &&
                calendarDraw.getCurrentMonth() == LocalDate.now().getMonthValue() &&
                calendarDraw.getCurrentYear() == LocalDate.now().getYear();
    }

    private void reloadCurrentDay() {
        dateInMillis = convertToEpochMillis(LocalDate.now());
        loadDiaryEntry(dateInMillis);
    }

    private void updateDateDisplay(LocalDate date) {
        dateTextView.setText(DATE_FORMATTER.format(date));
    }

    private long convertToEpochMillis(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void loadInitialData() {
        loadEmotions();
        loadDiaryEntry(dateInMillis);
    }

    private void saveDiaryEntry() {
        if (currentDiaryEntry != null) {
            updateExistingEntry();
        } else {
            createNewEntry();
        }
    }

    private void updateExistingEntry() {
        currentDiaryEntry.setHighlight(highlightInput.getText().toString());
        currentDiaryEntry.setAnnotation(annotationInput.getText().toString());
        currentDiaryEntry.setEmotion(selectedEmotion);
        new SaveDiaryEntryTask().execute(currentDiaryEntry);
    }

    private void createNewEntry() {
        LocalDate day = LocalDate.of(
                calendarDraw.getCurrentYear(),
                calendarDraw.getCurrentMonth(),
                calendarDraw.getCurrentDay().getDayOfMonth()
        );

        DiaryEntry entry = new DiaryEntry(
                highlightInput.getText().toString(),
                annotationInput.getText().toString(),
                selectedEmotion,
                UserLogged.getInstance().getCurrentUser().getId(),
                convertToEpochMillis(day)
        );
        new SaveDiaryEntryTask().execute(entry);
    }

    private void loadEmotions() {
        User user = UserLogged.getInstance().getCurrentUser();
        DatabaseExecutor.execute(() -> {
            diaryEntries = plantRepo.getPlantaDAO().getEntradasByUserId(user.getId());
            if (!diaryEntries.isEmpty()) {
                calendarDraw.setDiaryEntries(diaryEntries);
            }
        });
    }

    private void loadDiaryEntry(long date) {
        new LoadDiaryEntryTask().execute(date);
    }

    private void loadDiaryEntryUI(DiaryEntry entry) {
        LocalDate selectedDate = Instant.ofEpochMilli(dateInMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (entry == null) {
            if (!selectedDate.isAfter(LocalDate.now())) {
                resetEntry();
            }
            return;
        }

        if (entry.getHighlight() != null || entry.getAnnotation() != null || entry.getEmotion() != -1) {
            selectedEmotion = entry.getEmotion();
            updateEmotionImages();
            highlightInput.setText(entry.getHighlight());
            annotationInput.setText(entry.getAnnotation());
        }
    }

    private void updateEmotionImages() {
        for (int i = 0; i < emotionImages.size(); i++) {
            emotionImages.get(i).setAlpha(i == selectedEmotion ? 1.0f : 0.5f);
        }
    }

    private void resetEntry() {
        highlightInput.setText("");
        annotationInput.setText("");
        selectedEmotion = -1;
        for (ImageView image : emotionImages) {
            image.setAlpha(0.5f);
        }
    }

    private class LoadDiaryEntryTask extends AsyncTask<Long, Void, DiaryEntry> {
        @Override
        protected DiaryEntry doInBackground(Long... params) {
            long date = params[0];
            LocalDate selectedDate = Instant.ofEpochMilli(date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (selectedDate.isAfter(LocalDate.now())) {
                return null;
            }
            return plantRepo.getPlantaDAO().getEntradaByUserIdAndDate(
                    UserLogged.getInstance().getCurrentUser().getId(),
                    date
            );
        }

        @Override
        protected void onPostExecute(DiaryEntry entry) {
            currentDiaryEntry = entry;
            loadDiaryEntryUI(entry);
            loadEmotions();
            calendarDraw.invalidate();
        }
    }

    private class SaveDiaryEntryTask extends AsyncTask<DiaryEntry, Void, Void> {
        @Override
        protected Void doInBackground(DiaryEntry... entries) {
            DiaryEntry entry = entries[0];
            DiaryEntry existingEntry = plantRepo.getPlantaDAO().getEntradaByUserIdAndDate(
                    entry.getUserId(),
                    entry.getDate()
            );

            if (existingEntry != null) {
                entry.setId(existingEntry.getId());
                plantRepo.getPlantaDAO().update(entry);
            } else {
                plantRepo.getPlantaDAO().insert(entry);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadEmotions();
            calendarDraw.invalidate();
        }
    }
}