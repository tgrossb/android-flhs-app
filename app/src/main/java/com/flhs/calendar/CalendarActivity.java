package com.flhs.calendar;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.home.HomeActivity;
import com.flhs.preloader.InitialLoader;
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
import android.os.Debug;
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


public class CalendarActivity extends FLHSActivity implements ConnectionErrorFragment.AlertDialogListener, InitialLoader.OnReloadFinish, OnDateSelectedListener {
    public static final String DAY = "d";
    private MaterialCalendarView calendar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView dateView;
    private ProgressBar progressBar;
    private ImageButton todayButton;
    private Date today;
    private ArrayList<EventDecorator> decorators;

    public void onCreate(Bundle savedInstanceState) {
//        Debug.startMethodTracing("trace");
        super.onCreate(savedInstanceState, R.layout.activity_calendar);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryToConnect();
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

        preload();
        onReloadFinish();
    }

    public void tryToConnect(){
        swipeRefreshLayout.setRefreshing(false);
        if (isOnline()) {
            preload();
            // Start loading the events if connected
            InitialLoader.reload(this, this);
        } else {
            // Show the connection error dialog if not connected
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(getFragmentManager(), "Connection Error");
            reversePreload();
        }
    }

    public void preload(){
        todayButton.setVisibility(View.INVISIBLE);
        dateView.setText("Loading...");
        calendar.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    public void reversePreload(){
        todayButton.setVisibility(View.VISIBLE);
        dateView.setText(InitialLoader.viewableDate.format(today));
        calendar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        tryToConnect();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        progressBar.setVisibility(View.INVISIBLE);
        todayButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReloadFinish(){
        reversePreload();

        calendar.state().edit()
                .setMinimumDate(InitialLoader.firstCalendarDay)
                .setMaximumDate(InitialLoader.lastCalendarDay).commit();

        decorators = new ArrayList<>();
        for (String key : InitialLoader.eventfulDays.keySet()) {
            Date date = ParserA.fromVeventDate(key);
            CalendarDay calDay = CalendarDay.from(date);
            EventDecorator decorator = new EventDecorator(calDay, InitialLoader.eventfulDays.get(key).size());
            calendar.addDecorator(decorator);
            decorators.add(decorator);
        }
    }

    @Override
    public void onDateSelected(MaterialCalendarView matCal, CalendarDay calDay, boolean selected) {
//        Debug.stopMethodTracing();
        if (selected) {
            Intent startIntent = EventfulDayDisplay.getStartIntent(this, calDay, decorators);
            startActivity(startIntent);
        }
    }
}
