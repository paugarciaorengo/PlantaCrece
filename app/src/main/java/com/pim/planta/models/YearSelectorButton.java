package com.pim.planta.models;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.pim.planta.R;

import java.time.LocalDate;

public class YearSelectorButton extends FrameLayout {
    private final int maximumYear = LocalDate.now().getYear(); // Evita años futuros
    private int currentYear;
    public RecyclerView yearRecyclerView;
    public YearAdapter yearAdapter;
    public SnapHelper snapHelper;
    private int minimumYear = 2010;
    private int holderWidth;
    private OnYearSelectedListener listener;
    private CalendarDraw calendarDraw;

    public YearSelectorButton(@NonNull Context context) {
        super(context);
        init(context);
    }

    public YearSelectorButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public YearSelectorButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.year_picker_layout, this, true);

        yearRecyclerView = view.findViewById(R.id.year_recycler_view);

        currentYear = LocalDate.now().getYear();
        yearAdapter = new YearAdapter(currentYear, minimumYear, maximumYear);
        yearRecyclerView.setAdapter(yearAdapter);
        yearRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Calculate holder width after RecyclerView layout
        yearRecyclerView.post(() -> {
            YearAdapter.YearViewHolder viewHolder = (YearAdapter.YearViewHolder) yearRecyclerView.findViewHolderForAdapterPosition(4); // Changed to 4 to account for empty spaces
            if (viewHolder != null) {
                holderWidth = viewHolder.itemView.getWidth(); // Store holder width
                yearAdapter.setHolderWidth(holderWidth); // Pass holderWidth to YearAdapter

                // Smooth scroll to the current year to ensure it's centered
                LinearLayoutManager layoutManager = (LinearLayoutManager) yearRecyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int initialPosition = currentYear - minimumYear + 4;
                    int offset = (yearRecyclerView.getWidth() - holderWidth) / 2;
                    layoutManager.scrollToPositionWithOffset(initialPosition, offset);

                }
            }
        });

        yearRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(yearRecyclerView.getLayoutManager());
                    if (centerView != null) {
                        int position = yearRecyclerView.getLayoutManager().getPosition(centerView);
                        int selectedYear = minimumYear + position - 4; // Adjust for empty spaces
                        onYearSelected(selectedYear);
                    }
                }
            }
        });

        snapHelper = new LinearSnapHelper() {
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                int targetPosition = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);

                // Prevent snapping to empty spaces
                if (yearAdapter.getItemViewType(targetPosition) == YearAdapter.EMPTY_VIEW_TYPE) {
                    return RecyclerView.NO_POSITION;
                }

                // Allow snapping to first and last items
                if (targetPosition == RecyclerView.NO_POSITION) {
                    if (velocityX > 0) {
                        targetPosition = yearAdapter.getItemCount() - 5; // Snap to last year (2024)
                    } else if (velocityX < 0) {
                        targetPosition = 4; // Snap to first year (2010)
                    }
                }

                return targetPosition;
            }
        };
        snapHelper.attachToRecyclerView(yearRecyclerView);

        yearRecyclerView.setVisibility(View.VISIBLE);
    }

    public void onYearSelected(int year) {
        // Get the currently centered/snapped view
        View centerView = snapHelper.findSnapView(yearRecyclerView.getLayoutManager());

        if (centerView != null) {
            int centerPosition = yearRecyclerView.getLayoutManager().getPosition(centerView);
            int centeredYear = minimumYear + centerPosition - 4; // Adjust for empty spaces

            // Only trigger the callback if the clicked year is the centered year
            if (year == centeredYear) {
                currentYear = year;

                if (listener != null) {
                    listener.onYearSelected(year); // Update CalendarDraw
                }

                // Update CalendarDraw (assuming you have a method to set the year)
                if (calendarDraw != null) { // Check if calendarDraw is initialized
                    calendarDraw.setCurrentYear(year); // Replace with your actual method call
                    calendarDraw.invalidate(); // Invalidate CalendarDraw to redraw
                    calendarDraw.requestLayout(); // Request layout to ensure redraw
                }

                // Update the views directly
                for (int i = 0; i < yearAdapter.getItemCount(); i++) {
                    YearAdapter.YearViewHolder viewHolder = (YearAdapter.YearViewHolder) yearRecyclerView.findViewHolderForAdapterPosition(i);
                    if (viewHolder != null) {
                        int yearInView = minimumYear + i - 4; // Adjust for empty spaces
                        if (yearInView == year) {
                            // Selected year
                            viewHolder.yearTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24); // Larger text size
                            viewHolder.yearTextView.setTextColor(Color.WHITE); // White text color
                        } else if (yearInView >= minimumYear && yearInView <= LocalDate.now().getYear()) { // Check if year is within visible range
                            // Other years
                            viewHolder.yearTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // Smaller text size
                            viewHolder.yearTextView.setTextColor(Color.LTGRAY); // Light gray text color
                        }
                    }
                }
            }
        }
        if (currentYear==LocalDate.now().getYear() && calendarDraw.getCurrentMonth()>LocalDate.now().getMonthValue())
            calendarDraw.setCurrentMonth(LocalDate.now().getMonthValue());
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
        yearAdapter.setCurrentYear(currentYear);
        yearAdapter.notifyDataSetChanged();

        // Scroll to the selected year
        int position = currentYear - minimumYear + 4; // Ajustado por espacios vacíos

        // Esperar a que RecyclerView esté listo para scroll
        yearRecyclerView.post(() -> {
            LinearLayoutManager layoutManager = (LinearLayoutManager) yearRecyclerView.getLayoutManager();
            if (layoutManager != null && holderWidth > 0) {
                int visibleItemCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition() + 3;
                int offset = (yearRecyclerView.getWidth() - holderWidth * visibleItemCount) / 2;
                layoutManager.scrollToPositionWithOffset(position, offset);
            }
        });
    }


    public int getMinimumYear() {
        return minimumYear;
    }

    public void setCalendarDraw(CalendarDraw calendarDraw) {
        this.calendarDraw = calendarDraw;
    }

    public void setOnYearSelectedListener(OnYearSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnYearSelectedListener {
        void onYearSelected(int year);
    }
}