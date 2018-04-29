package com.flhs.calendar;

import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.EventLog;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.preloader.InitialLoader;
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.EventObject;
import com.flhs.utils.EventsAdapter;
import com.flhs.utils.NPALayoutManager;
import com.flhs.utils.ParserA;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EventfulDayDisplay extends FLHSActivity implements OnDateSelectedListener, InitialLoader.OnReloadFinish {
    public static final String CLICKED_DAY = "cd", DECORATORS = "d";
    public static SimpleDateFormat formatter = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US);
    private String dateString, todayString;
    private ViewPager pager;
    private MaterialCalendarView calendarView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView dateHeader;
    private ImageButton todayButton;
    private ProgressBar progressBar;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState, R.layout.activity_eventful_day_display);

        Intent intent = getIntent();
        dateString = intent.getStringExtra(CLICKED_DAY);

        Date setDate;
        try {
            setDate = formatter.parse(dateString);
        } catch (ParseException e){
            setDate = new Date();
        }

        calendarView = findViewById(R.id.weeksView);
        calendarView.setCurrentDate(setDate);
        calendarView.setDateSelected(setDate, true);
        calendarView.setOnDateChangedListener(this);
        calendarView.state().edit()
                .setMinimumDate(InitialLoader.firstCalendarDay)
                .setMaximumDate(InitialLoader.lastCalendarDay)
                .commit();

        ArrayList<EventDecorator> decorators = intent.getParcelableArrayListExtra(DECORATORS);
        for (EventDecorator decorator : decorators)
            calendarView.addDecorator(decorator);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position){
                Date date = fromPos(position, InitialLoader.firstCalendarDayMS);
                calendarView.setCurrentDate(date);
                calendarView.setSelectedDate(date);
            }
        });

        // Set date will be at midnight, add a millisecond to handle this
        int pos = toPos(setDate.getTime()+1, InitialLoader.firstCalendarDayMS);
        System.out.println("Today  " + setDate.toString() + " => " + pos);
        pager.setCurrentItem(pos);

        dateHeader = findViewById(R.id.dateHeader);
        todayButton = findViewById(R.id.today);
        progressBar = findViewById(R.id.spinner);

        todayString = InitialLoader.viewableDate.format(new Date());
        dateHeader.setText(todayString);
        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarView.setDateSelected(calendarView.getSelectedDate(), false);
                Date today = new Date();
                calendarView.setCurrentDate(today);
                calendarView.setDateSelected(today, true);
                pager.setCurrentItem(toPos(today.getTime(), InitialLoader.firstCalendarDayMS));
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryToConnect();
            }
        });
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
        calendarView.setVisibility(View.INVISIBLE);
        pager.setVisibility(View.INVISIBLE);

        dateHeader.setText("Loading...");
        todayButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void reversePreload(){
        calendarView.setVisibility(View.VISIBLE);
        pager.setVisibility(View.VISIBLE);

        dateHeader.setText(todayString);
        todayButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        // Trigger a pager view update
        pager.setCurrentItem(pager.getCurrentItem(), false);
    }

    @Override
    public void onReloadFinish(){
        reversePreload();
    }

    public static Intent getStartIntent(Context context, CalendarDay clickedDay, ArrayList<EventDecorator> decorators){
        Intent start = new Intent(context.getApplicationContext(), EventfulDayDisplay.class);
        start.putExtra(CLICKED_DAY, formatter.format(clickedDay.getDate()));
        start.putParcelableArrayListExtra(DECORATORS, decorators);
        return start;
    }

    @Override
    public void onDateSelected(MaterialCalendarView matCal, CalendarDay calDay, boolean selected){
        if (selected)
            // CalendarDay objects are set at midnight, roll it forward a millisecond to handle this
            pager.setCurrentItem(toPos(calDay.getDate().getTime() + 100, InitialLoader.firstCalendarDayMS));
    }

    @Override
    public void onBackPressed(){
        Intent backToCal = new Intent(this, CalendarActivity.class);
        backToCal.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        backToCal.putExtra(CalendarActivity.DAY, dateString);
        startActivity(backToCal);
    }

    public static int toPos(long endMs, long startMs){
        long msDiff = endMs - startMs;
        if (msDiff > 0)
            return (int) TimeUnit.DAYS.convert(msDiff, TimeUnit.MILLISECONDS);
        return 0;
    }

    public static Date fromPos(int pos, long startMs){
        if (pos == 0)
            return new Date(startMs);
        long msDiff = TimeUnit.MILLISECONDS.convert(pos, TimeUnit.DAYS);
        return new Date(startMs + msDiff);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private int dayCount;

        public ViewPagerAdapter(FragmentManager manager){
            super(manager);
            long diffInMs = InitialLoader.lastCalendarDayMS - InitialLoader.firstCalendarDayMS;
            this.dayCount = (int) TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public Fragment getItem(int pos){
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(InitialLoader.firstCalendarDayMS);
            start.add(Calendar.DAY_OF_YEAR, pos);
            String key = InitialLoader.veventDate.format(start.getTime());
            ArrayList<EventObject> events = InitialLoader.eventfulDays.get(key);
            if (events == null) {
                events = new ArrayList<>();
            }
            return EventfulDayFragment.newInstance(key, events);
        }

        @Override
        public int getCount(){
            return dayCount;
        }
    }
}