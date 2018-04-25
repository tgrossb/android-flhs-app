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
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.flhs.FLHSActivity;
import com.flhs.R;
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

public class EventfulDayDisplay extends FLHSActivity implements OnDateSelectedListener {
    public static final String CLICKED_DAY = "cd", EVENTS = "e", START_DAY = "sd", END_DAY = "ed", DECORATORS = "d";
    public static SimpleDateFormat formatter = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US);
    private String dateString;
    private HashMap<String, ArrayList<EventObject>> events;
    private long startDate, endDate;
    private ViewPager pager;
    private MaterialCalendarView calendarView;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState, R.layout.activity_eventful_day_display);

        Intent intent = getIntent();
        dateString = intent.getStringExtra(CLICKED_DAY);
        events = (HashMap) intent.getSerializableExtra(EVENTS);
        startDate = intent.getLongExtra(START_DAY, 0);
        endDate = intent.getLongExtra(END_DAY, 0);

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
                .setMinimumDate(new Date(startDate))
                .setMaximumDate(new Date(endDate))
                .commit();

        ArrayList<EventDecorator> decorators = intent.getParcelableArrayListExtra(DECORATORS);
        for (EventDecorator decorator : decorators)
            calendarView.addDecorator(decorator);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), events, startDate, endDate));
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position){
                Date date = fromPos(position, startDate);
                calendarView.setCurrentDate(date);
                calendarView.setSelectedDate(date);
            }
        });

        pager.setCurrentItem(toPos(setDate, startDate));
    }

    public static Intent getStartIntent(Context context, CalendarDay clickedDay, HashMap<String, ArrayList<EventObject>> eventfulDays,
                                        Calendar startDay, Calendar endDay, ArrayList<EventDecorator> decorators){
        Intent start = new Intent(context.getApplicationContext(), EventfulDayDisplay.class);
        start.putExtra(CLICKED_DAY, formatter.format(clickedDay.getDate()));
        start.putExtra(EVENTS, eventfulDays);
        start.putExtra(START_DAY, startDay.getTimeInMillis());
        start.putExtra(END_DAY, endDay.getTimeInMillis());
        start.putParcelableArrayListExtra(DECORATORS, decorators);
        return start;
    }

    public static int toPos(Date d1, long startMs){
        long msDiff = d1.getTime() - startMs;
        if (msDiff > 0)
            return (int) TimeUnit.DAYS.convert(msDiff, TimeUnit.MILLISECONDS) + 1;
        return 0;
    }

    public static Date fromPos(int pos, long startMs){
        if (pos == 0)
            return new Date(startMs);
        long msDiff = TimeUnit.MILLISECONDS.convert(pos, TimeUnit.DAYS);
        return new Date(startMs + msDiff);
    }

    @Override
    public void onDateSelected(MaterialCalendarView matCal, CalendarDay calDay, boolean selected){
        if (selected)
            pager.setCurrentItem(toPos(calDay.getDate(), startDate));
    }

    @Override
    public void onBackPressed(){
        Intent backToCal = new Intent(this, CalendarActivity.class);
        backToCal.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        backToCal.putExtra(CalendarActivity.DAY, dateString);
        startActivity(backToCal);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private HashMap<String, ArrayList<EventObject>> eventfulDays;
        private int dayCount;
        private long startMs;

        public ViewPagerAdapter(FragmentManager manager, HashMap<String, ArrayList<EventObject>> eventfulDays, long startMs, long endMs){
            super(manager);
            this.eventfulDays = eventfulDays;
            long diffInMs = endMs - startMs;
            this.dayCount = (int) TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);
            this.startMs = startMs;
        }

        @Override
        public Fragment getItem(int pos){
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(startMs);
            start.add(Calendar.DAY_OF_YEAR, pos);
            String key = ParserA.veventFormNoTime.format(start.getTime());
            ArrayList<EventObject> events = eventfulDays.get(key);
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