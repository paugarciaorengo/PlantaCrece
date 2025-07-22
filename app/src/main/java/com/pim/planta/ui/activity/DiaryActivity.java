package com.pim.planta.ui.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pim.planta.helpers.BottomNavigationHelper;
import com.pim.planta.base.NotificationActivity;
import com.pim.planta.R;
import com.pim.planta.db.DatabaseExecutor;
import com.pim.planta.db.PlantooRepository;
import com.pim.planta.models.CalendarDraw;
import com.pim.planta.models.DiaryEntry;
import com.pim.planta.models.User;
import com.pim.planta.models.UserLogged;
import com.pim.planta.models.YearSelectorButton;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryActivity extends NotificationActivity {

    private TextView dateTextView;
    private EditText noteEditText;
    private EditText highlightEditText;
    private LinearLayout emotionLayout;
    private PlantooRepository plantooRepository;
    private User currentUser;

    private int selectedEmotionCode = 0;
    private CalendarDraw calendarDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        currentUser = UserLogged.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no encontrado. Por favor, inicia sesión.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        plantooRepository = PlantooRepository.getInstance(this);

        dateTextView = findViewById(R.id.dateTextView);
        noteEditText = findViewById(R.id.annotationInput);
        highlightEditText = findViewById(R.id.highlightInput);
        emotionLayout = findViewById(R.id.emotionsLayout);
        Button saveButton = findViewById(R.id.buttonSaveEntry);

        calendarDraw = findViewById(R.id.calendar_draw);
        YearSelectorButton yearSelector = findViewById(R.id.year_selector_button);
        yearSelector.setCalendarDraw(calendarDraw);
        yearSelector.setOnYearSelectedListener(year -> {
            calendarDraw.setCurrentYear(year);
            reloadEntriesForCurrentMonth();
        });

        calendarDraw.setOnMonthChangeListener(() -> reloadEntriesForCurrentMonth());

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dateTextView.setText(today);

        setupEmotionButtons();
        saveButton.setOnClickListener(v -> saveDiaryEntry(dateTextView.getText().toString()));

        View bottomNavView = findViewById(R.id.bottomNavigation);
        BottomNavigationHelper.Binding bottomNavBinding = new BottomNavigationHelper.Binding(bottomNavView);
        BottomNavigationHelper.setup(this, bottomNavBinding, DiaryActivity.class);

        loadExistingEntry(today);
        setupCalendarTouchListener();
        reloadEntriesForCurrentMonth();

        ImageView prevMonthBtn = findViewById(R.id.previousMonthButton);
        ImageView nextMonthBtn = findViewById(R.id.nextMonthButton);

        prevMonthBtn.setOnClickListener(v -> {
            calendarDraw.prevMonth();
            reloadEntriesForCurrentMonth(); // Actualiza las entradas para el mes nuevo
        });

        nextMonthBtn.setOnClickListener(v -> {
            calendarDraw.nextMonth();
            reloadEntriesForCurrentMonth(); // Igual para siguiente mes
        });
    }

    private void setupCalendarTouchListener() {
        calendarDraw.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int clickedDay = calendarDraw.getDayFromCoordinates(event.getX(), event.getY());
                if (clickedDay != -1) {
                    LocalDate selectedDate = LocalDate.of(
                            calendarDraw.getCurrentYear(),
                            calendarDraw.getCurrentMonth(),
                            clickedDay
                    );
                    calendarDraw.highlightDay(selectedDate);
                    dateTextView.setText(selectedDate.toString());
                    loadExistingEntry(selectedDate.toString());
                }
            }
            return false;
        });
    }

    private void setupEmotionButtons() {
        ImageView excited = findViewById(R.id.excitedImage);
        ImageView happy = findViewById(R.id.happyImage);
        ImageView neutral = findViewById(R.id.neutralImage);
        ImageView sad = findViewById(R.id.sadImage);
        ImageView verySad = findViewById(R.id.verySadImage);

        View.OnClickListener listener = v -> {
            resetEmotionHighlights();
            v.setBackgroundResource(R.drawable.selected_emotion_border);
            int id = v.getId();
            if (id == R.id.excitedImage) {
                selectedEmotionCode = 1;
            } else if (id == R.id.happyImage) {
                selectedEmotionCode = 2;
            } else if (id == R.id.neutralImage) {
                selectedEmotionCode = 3;
            } else if (id == R.id.sadImage) {
                selectedEmotionCode = 4;
            } else if (id == R.id.verySadImage) {
                selectedEmotionCode = 5;
            }
            Toast.makeText(this, "Emoción seleccionada", Toast.LENGTH_SHORT).show();
        };

        excited.setOnClickListener(listener);
        happy.setOnClickListener(listener);
        neutral.setOnClickListener(listener);
        sad.setOnClickListener(listener);
        verySad.setOnClickListener(listener);
    }

    private void resetEmotionHighlights() {
        for (int i = 0; i < emotionLayout.getChildCount(); i++) {
            View child = emotionLayout.getChildAt(i);
            child.setBackground(null);
        }
    }

    private void saveDiaryEntry(String date) {
        String note = noteEditText.getText().toString().trim();
        String highlight = highlightEditText.getText().toString().trim();

        if (selectedEmotionCode == 0 && note.isEmpty() && highlight.isEmpty()) {
            Toast.makeText(this, "Debes seleccionar una emoción o escribir algo", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseExecutor.execute(() -> {
            plantooRepository.insertDiaryEntry(currentUser.getId(), selectedEmotionCode, highlight, note, date);
            runOnUiThread(() -> {
                Toast.makeText(this, "Entrada guardada", Toast.LENGTH_SHORT).show();
                reloadEntriesForCurrentMonth();
            });
        });
    }

    private void loadExistingEntry(String date) {
        DatabaseExecutor.execute(() -> {
            int emotion = plantooRepository.getEmotionCodeByUserAndDate(currentUser.getId(), date);
            String annotation = plantooRepository.getNoteByUserAndDate(currentUser.getId(), date);
            String highlight = plantooRepository.getHighlightByUserAndDate(currentUser.getId(), date);

            runOnUiThread(() -> {
                selectedEmotionCode = emotion;
                highlightEditText.setText(highlight != null ? highlight : "");
                noteEditText.setText(annotation != null ? annotation : "");
                highlightSelectedEmotion();
            });
        });
    }

    private void highlightSelectedEmotion() {
        resetEmotionHighlights();

        int id = 0;
        switch (selectedEmotionCode) {
            case 1:
                id = R.id.excitedImage;
                break;
            case 2:
                id = R.id.happyImage;
                break;
            case 3:
                id = R.id.neutralImage;
                break;
            case 4:
                id = R.id.sadImage;
                break;
            case 5:
                id = R.id.verySadImage;
                break;
        }

        if (id != 0) {
            View view = findViewById(id);
            if (view != null) {
                view.setBackgroundResource(R.drawable.selected_emotion_border);
            }
        }
    }

    private void reloadEntriesForCurrentMonth() {
        int year = calendarDraw.getCurrentYear();
        int month = calendarDraw.getCurrentMonth();
        DatabaseExecutor.execute(() -> {
            List<DiaryEntry> entries = plantooRepository.getEntriesByUserAndMonth(currentUser.getId(), year, month);
            runOnUiThread(() -> calendarDraw.setDiaryEntries(entries));
        });
    }
}