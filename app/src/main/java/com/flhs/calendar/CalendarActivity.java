package com.flhs.calendar;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.home.HomeActivity;
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.EventObject;
import com.flhs.utils.NPALayoutManager;
import com.flhs.utils.ParserA;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class CalendarActivity extends FLHSActivity implements ConnectionErrorFragment.AlertDialogListener, ICalLoaderThread.OnFinish, OnDateSelectedListener {
    public static final String DAY = "d";
    private static String[] vevents = null;
    private MaterialCalendarView calendar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView dateView;
    private ProgressBar progressBar;
    private HashMap<String, ArrayList<EventObject>> eventfulDays;
    private Calendar firstDay, lastDay;
    private ImageButton todayButton;
    private Date today;
    private ArrayList<EventDecorator> decorators;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_calendar);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryToConnect(true);
            }
        });

        dateView = findViewById(R.id.dateHeader);
        progressBar = findViewById(R.id.progress_bar);
        todayButton = findViewById(R.id.today);

        today = new Date();
        Date initDay = new Date();
        Intent startIntent = getIntent();
        if (startIntent.hasExtra(DAY)){
            try {
                initDay = EventfulDayDisplay.formatter.parse(startIntent.getStringExtra(DAY));
            } catch (ParseException e) {
                initDay = new Date();
            }
        }
        calendar = findViewById(R.id.calendarView);
        calendar.setCurrentDate(initDay);
        calendar.setDateSelected(initDay, true);
        calendar.setOnDateChangedListener(this);

        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.setDateSelected(calendar.getSelectedDate(), false);
                calendar.setCurrentDate(today);
                calendar.setDateSelected(today, true);
            }
        });
        tryToConnect(false);
    }

    public void tryToConnect(boolean userInduced){
        swipeRefreshLayout.setRefreshing(false);
        if (isOnline()) {
            if (vevents != null && !userInduced)
                onFinish(vevents);
            else
                // Start loading the events if connected
                todayButton.setVisibility(View.INVISIBLE);
                new ICalLoaderThread(this, dateView, calendar, progressBar).execute();
        } else {
            // Show the connection error dialog if not connected
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(getFragmentManager(), "Connection Error");
            progressBar.setVisibility(View.INVISIBLE);
            todayButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        tryToConnect(true);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        progressBar.setVisibility(View.INVISIBLE);
        todayButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinish(String[] recievedVevents){
        progressBar.setVisibility(View.INVISIBLE);
        if (recievedVevents == ICalLoaderThread.FAIL){
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(getFragmentManager(), "Connection Error");
            return;
        }
        vevents = recievedVevents;
        int firstStart = vevents[0].indexOf("BEGIN:VEVENT");
        vevents[0] = vevents[0].substring(firstStart + 12);

        setupCalendar();

        todayButton.setVisibility(View.VISIBLE);

        eventfulDays = ParserA.getEventfulDays(vevents);
        decorators = new ArrayList<>();
        for (String key : eventfulDays.keySet()) {
            Date date = ParserA.fromVeventDate(key);
            CalendarDay calDay = CalendarDay.from(date);
            EventDecorator decorator = new EventDecorator(calDay, eventfulDays.get(key).size());
            calendar.addDecorator(decorator);
            decorators.add(decorator);
        }
    }

    @Override
    public void onDateSelected(MaterialCalendarView matCal, CalendarDay calDay, boolean selected){
        if (selected){
            Intent startIntent = EventfulDayDisplay.getStartIntent(this, calDay, eventfulDays, firstDay, lastDay, decorators);
            startActivity(startIntent);
        }
    }

    public void setupCalendar(){
        Date today = new Date();
        // Format date to be "DayOfWeek Month Day, Year"
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US);
        String date = dateFormat.format(today);
        dateView.setText(date);

        // Get soft first and last days, changed later when exceeded
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(today);

        firstDay = Calendar.getInstance();
        firstDay.setTime(today);
        firstDay.set(Calendar.MONTH, Calendar.SEPTEMBER);
        firstDay.set(Calendar.DAY_OF_MONTH, firstDay.getActualMinimum(Calendar.DAY_OF_MONTH));
        if (nowCal.before(firstDay))
            firstDay.roll(Calendar.YEAR, -1);

        lastDay = Calendar.getInstance();
        lastDay.setTime(today);
        lastDay.set(Calendar.MONTH, Calendar.JUNE);
        lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (nowCal.after(lastDay))
            lastDay.roll(Calendar.YEAR, 1);

        for (String vevent : vevents) {
            String dtstart = ParserA.getValue(vevent, "DTSTART:");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date start = new Date();
            try {
                start = format.parse(dtstart);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date end = ParserA.getEventEndDate(vevent, start);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(start);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(end);
            if (firstDay.after(startCal))
                firstDay.setTime(start);
            if (lastDay.before(endCal))
                lastDay.setTime(end);
        }
        firstDay.set(Calendar.DAY_OF_MONTH, firstDay.getActualMinimum(Calendar.DAY_OF_MONTH));
        lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));

        calendar.state().edit()
                .setMinimumDate(firstDay)
                .setMaximumDate(lastDay).commit();
    }
}
