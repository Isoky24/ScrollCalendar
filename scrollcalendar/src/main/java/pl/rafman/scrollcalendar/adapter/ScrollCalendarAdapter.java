package pl.rafman.scrollcalendar.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pl.rafman.scrollcalendar.contract.ClickCallback;
import pl.rafman.scrollcalendar.contract.DateWatcher;
import pl.rafman.scrollcalendar.contract.DayViewFactory;
import pl.rafman.scrollcalendar.contract.MonthScrollListener;
import pl.rafman.scrollcalendar.contract.OnDateClickListener;
import pl.rafman.scrollcalendar.contract.State;
import pl.rafman.scrollcalendar.data.CalendarDay;
import pl.rafman.scrollcalendar.data.CalendarMonth;
import pl.rafman.scrollcalendar.style.DayResProvider;
import pl.rafman.scrollcalendar.style.MonthResProvider;

/**
 * Created by rafal.manka on 10/09/2017
 */
public class ScrollCalendarAdapter extends RecyclerView.Adapter<MonthViewHolder> implements ClickCallback {

    @NonNull
    private final List<CalendarMonth> months = new ArrayList<>();

    @Nullable
    private RecyclerView recyclerView;

    @Nullable
    private MonthScrollListener monthScrollListener;
    @Nullable
    private OnDateClickListener onDateClickListener;
    @Nullable
    private DateWatcher dateWatcher;
    private MonthResProvider monthResProvider;
    private DayResProvider dayResProvider;
    private DayViewFactory factory;

    public ScrollCalendarAdapter(@NonNull MonthResProvider monthResProvider, @NonNull DayResProvider dayResProvider) {
        this.monthResProvider = monthResProvider;
        this.dayResProvider = dayResProvider;
        months.add(CalendarMonth.now());
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MonthViewHolder.create(parent, this, monthResProvider, dayResProvider, factory);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        CalendarMonth month = getItem(position);
        prepare(month);
        holder.bind(month);
        afterBindViewHolder(position);
    }

    private void afterBindViewHolder(int position) {
        if (recyclerView != null) {
            if (shouldAddPreviousMonth(position)) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        prependCalendarMonth();
                    }
                });
            }
            if (shouldAddNextMonth(position)) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        appendCalendarMonth();
                    }
                });
            }
        }

    }

    private boolean shouldAddPreviousMonth(int position) {
        return isNearTop(position) && isAllowedToAddPreviousMonth();
    }

    protected boolean isAllowedToAddPreviousMonth() {
        if (monthScrollListener == null) {
            return false;
        }
        CalendarMonth item = getFirstItem();
        return monthScrollListener.shouldAddPreviousMonth(item.getYear(), item.getMonth());
    }

    private boolean shouldAddNextMonth(int position) {
        return isNearBottom(position) && isAllowedToAddNextMonth();
    }

    protected boolean isAllowedToAddNextMonth() {
        if (monthScrollListener == null) {
            return true;
        }
        CalendarMonth item = getLastItem();
        return monthScrollListener.shouldAddNextMonth(item.getYear(), item.getMonth());
    }

    private void prepare(CalendarMonth month) {
        for (CalendarDay calendarDay : month.getDays()) {
            calendarDay.setState(makeState(month, calendarDay));
        }
    }

    @State
    private int makeState(CalendarMonth month, CalendarDay calendarDay) {
        int year = month.getYear();
        int monthInt = month.getMonth();
        int day = calendarDay.getDay();
        return getStateForDate(year, monthInt, day);
    }

    @State
    protected int getStateForDate(int year, int month, int day) {
        if (dateWatcher == null) {
            return CalendarDay.DEFAULT;
        }
        return dateWatcher.getStateForDate(year, month, day);
    }

    private void prependCalendarMonth() {
        if (recyclerView != null) {
            months.add(0, getFirstItem().previous());
            notifyItemInserted(0);
        }
    }

    private void appendCalendarMonth() {
        months.add(getLastItem().next());
        notifyItemInserted(months.size() - 1);
    }

    private CalendarMonth getFirstItem() {
        return months.get(0);
    }

    private CalendarMonth getLastItem() {
        return months.get(months.size() - 1);
    }

    private CalendarMonth getItem(int position) {
        return months.get(position);
    }

    private boolean isNearTop(int position) {
        return position == 0;
    }

    private boolean isNearBottom(int position) {
        return months.size() - 1 <= position;
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    public void setOnDateClickListener(@Nullable OnDateClickListener onDateClickListener) {
        this.onDateClickListener = onDateClickListener;
    }

    public void setMonthScrollListener(@Nullable MonthScrollListener monthScrollListener) {
        this.monthScrollListener = monthScrollListener;
    }

    public void setDateWatcher(@Nullable DateWatcher dateWatcher) {
        this.dateWatcher = dateWatcher;
    }

    @Override
    public void onCalendarDayClicked(@NonNull CalendarMonth calendarMonth, @NonNull CalendarDay calendarDay) {
        int year = calendarMonth.getYear();
        int month = calendarMonth.getMonth();
        int day = calendarDay.getDay();
        onCalendarDayClicked(year, month, day);
        notifyDataSetChanged();
    }

    protected void onCalendarDayClicked(int year, int month, int day) {
        if (onDateClickListener != null) {
            onDateClickListener.onCalendarDayClicked(year, month, day);
        }
    }

    public void setFactory(DayViewFactory factory) {
        this.factory = factory;
    }
}
