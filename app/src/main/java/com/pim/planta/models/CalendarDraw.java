package com.pim.planta.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.pim.planta.R;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CalendarDraw extends View {
    private static final int DAYS_IN_WEEK = 7;
    private static final int MAX_WEEKS = 6;
    private static final int HEADER_HEIGHT = 100;
    private Paint dayPaint, headerPaint, backGroundPaint, underlinePaint,
            highlightedDayPaint, highlightedDayCircle;
    private Typeface customFont, customFontBold;
    private int currentMonth, currentYear;
    private LocalDate highlightedDay;
    private List<DiaryEntry> diaryEntries;
    private Runnable monthChangeListener;

    public CalendarDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarDraw(Context context) {
        super(context);
        init();
    }

    private void init() {
        customFont = ResourcesCompat.getFont(getContext(), R.font.aventa);
        customFontBold = Typeface.create(customFont, Typeface.BOLD);

        dayPaint = createPaint(Color.parseColor("#073E24"), 46, Paint.Align.CENTER, customFont);
        underlinePaint = createStrokePaint(Color.parseColor("#35D18F"), 5f);
        headerPaint = createPaint(Color.parseColor("#073E24"), 48, Paint.Align.CENTER, customFontBold);
        highlightedDayPaint = createPaint(Color.WHITE, 46, Paint.Align.CENTER, customFont);

        highlightedDayCircle = new Paint();
        highlightedDayCircle.setColor(getResources().getColor(R.color.dark_green));
        highlightedDayCircle.setStyle(Paint.Style.FILL);

        backGroundPaint = new Paint();
        backGroundPaint.setColor(Color.WHITE);
        backGroundPaint.setStyle(Paint.Style.FILL);

        LocalDate today = LocalDate.now();
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();
    }

    private Paint createPaint(int color, float textSize, Paint.Align align, Typeface typeface) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setTextAlign(align);
        paint.setTypeface(typeface);
        return paint;
    }

    private Paint createStrokePaint(int color, float strokeWidth) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cornerRadius = getWidth() * 0.05f;
        RectF bounds = new RectF(0, 0, getWidth(), getHeight());
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, backGroundPaint);
        canvas.clipRect(bounds);

        drawMonthHeader(canvas);
        drawDays(canvas);
    }

    private void drawMonthHeader(Canvas canvas) {
        String monthName = LocalDate.of(currentYear, currentMonth, 1)
                .format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
                .toUpperCase();
        canvas.drawText(monthName, getWidth() / 2f, HEADER_HEIGHT, headerPaint);
    }

    private void drawDays(Canvas canvas) {
        int dayWidth = getWidth() / DAYS_IN_WEEK;
        int dayHeight = (getHeight() - HEADER_HEIGHT) / MAX_WEEKS;
        LocalDate firstDay = LocalDate.of(currentYear, currentMonth, 1);
        int daysInMonth = firstDay.lengthOfMonth();
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();
        int startColumn = (startDayOfWeek == 7) ? 6 : startDayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            int col = (startColumn + day - 1) % DAYS_IN_WEEK;
            int row = (startColumn + day - 1) / DAYS_IN_WEEK;
            float x = col * dayWidth + dayWidth / 2f;
            float y = row * dayHeight + dayHeight / 2f + HEADER_HEIGHT;

            LocalDate currentDate = LocalDate.of(currentYear, currentMonth, day);
            boolean isHighlighted = currentDate.equals(highlightedDay);
            boolean hasEntry = hasEntryForDay(currentDate);

            if (isHighlighted) {
                canvas.drawCircle(x, y, dayWidth / 3f, highlightedDayCircle);
                canvas.drawText(String.valueOf(day), x, y + 10, highlightedDayPaint);
            } else {
                canvas.drawText(String.valueOf(day), x, y + 10, dayPaint);
            }

            if (diaryEntries != null) {
                for (DiaryEntry entry : diaryEntries) {
                    LocalDate entryDate = Instant.ofEpochMilli(entry.getDate())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    if (entryDate.equals(currentDate)) {
                        Paint emotionPaint = getPaintForEmotion(entry.getEmotion());
                        drawUnderline(canvas, x, y, dayWidth, emotionPaint);
                        break;
                    }
                }
            }

        }
    }

    private void drawUnderline(Canvas canvas, float x, float y, int width, Paint paint) {
        float startX = x - width / 4f;
        float endX = x + width / 4f;
        canvas.drawLine(startX, y + 30, endX, y + 30, paint);
    }

    public void setDiaryEntries(List<DiaryEntry> entries) {
        this.diaryEntries = entries;
        invalidate();
    }

    public void prevMonth() {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        highlightedDay = null;
        invalidate();
        if (monthChangeListener != null) monthChangeListener.run();
    }

    public void nextMonth() {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        LocalDate now = LocalDate.now();
        if (currentYear > now.getYear() || (currentYear == now.getYear() && currentMonth > now.getMonthValue())) {
            currentMonth = now.getMonthValue();
            currentYear = now.getYear();
        }
        highlightedDay = null;
        invalidate();
        if (monthChangeListener != null) monthChangeListener.run();
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(int year) {
        int maxYear = LocalDate.now().getYear();
        if (year > maxYear) year = maxYear;
        this.currentYear = year;
        highlightedDay = null;
        invalidate();
    }

    public int getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(int month) {
        int maxMonth = (currentYear == LocalDate.now().getYear()) ? LocalDate.now().getMonthValue() : 12;
        this.currentMonth = Math.min(month, maxMonth);
        highlightedDay = null;
        invalidate();
    }

    public LocalDate getCurrentDay() {
        return highlightedDay;
    }

    public void highlightDay(LocalDate date) {
        this.highlightedDay = date;
        invalidate();
    }

    public boolean hasEntryForDay(LocalDate date) {
        if (diaryEntries == null || diaryEntries.isEmpty()) {
            return false;
        }
        for (DiaryEntry entry : diaryEntries) {
            LocalDate entryDate = Instant.ofEpochMilli(entry.getDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (entryDate.getYear() == date.getYear() &&
                    entryDate.getMonthValue() == date.getMonthValue() &&
                    entryDate.getDayOfMonth() == date.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    public long normalizeToStartOfDay(long timestamp) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    public int getDayFromCoordinates(float x, float y) {
        int dayWidth = getWidth() / DAYS_IN_WEEK;
        int dayHeight = (getHeight() - HEADER_HEIGHT) / MAX_WEEKS;
        LocalDate firstDay = LocalDate.of(currentYear, currentMonth, 1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue();
        int startColumn = (startDayOfWeek == 7) ? 6 : startDayOfWeek - 1;
        int col = (int) (x / dayWidth);
        int row = (int) ((y - HEADER_HEIGHT) / dayHeight);
        if (col >= 0 && col < 7 && row >= 0 && row < 6) {
            int dayClicked = row * 7 + col + 1 - startColumn;
            if (dayClicked > 0 && dayClicked <= firstDay.lengthOfMonth()) {
                return dayClicked;
            }
        }
        return -1;
    }

    public void setOnMonthChangeListener(Runnable listener) {
        this.monthChangeListener = listener;
    }

    private Paint getPaintForEmotion(int emotion) {
        int color;
        switch (emotion) {
            case 1: // Excited
                color = getResources().getColor(R.color.excited_green); // Define este color en colors.xml
                break;
            case 2: // Happy
                color = getResources().getColor(R.color.happy_yellow);
                break;
            case 3: // Neutral
                color = getResources().getColor(R.color.neutral_gray);
                break;
            case 4: // Sad
                color = getResources().getColor(R.color.sad_blue);
                break;
            case 5: // Very sad
                color = getResources().getColor(R.color.very_sad_purple);
                break;
            default:
                color = Color.LTGRAY;
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

}