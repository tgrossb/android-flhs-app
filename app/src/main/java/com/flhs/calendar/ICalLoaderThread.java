package com.flhs.calendar;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.flhs.utils.EventObject;
import com.flhs.utils.ParserA;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ICalLoaderThread extends AsyncTask<Void, Void, String> {
    public static final String[] FAIL = null;
    private OnFinish finished;
    private WeakReference<TextView> dateViewRef;
    private WeakReference<MaterialCalendarView> calendarRef;
    private WeakReference<ProgressBar> progressBarRef;

    public interface OnFinish {
        void onFinish(String[] vevents);
    }

    public ICalLoaderThread(OnFinish finished, TextView dateView, MaterialCalendarView calendar, ProgressBar progressBar){
        this.finished = finished;
        dateViewRef = new WeakReference<>(dateView);
        calendarRef  = new WeakReference<>(calendar);
        progressBarRef = new WeakReference<>(progressBar);
    }

    @Override
    protected void onPreExecute() {
        dateViewRef.get().setText("Loading...");
        calendarRef.get().setVisibility(View.INVISIBLE);
        progressBarRef.get().setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    protected void onPostExecute(String ical) {
        if (ical == null){
            finished.onFinish(null);
            return;
        }

        // Split the ical into vevents
        String[] vevents = ical.split("END:VEVENT\nBEGIN:VEVENT");

        calendarRef.get().setVisibility(View.VISIBLE);
        progressBarRef.get().setVisibility(View.INVISIBLE);

        finished.onFinish(vevents);
    }

    @Override
    protected String doInBackground(Void... arg0) {
        return ParserA.readCalendar(ParserA.DISTRICT_CALENDAR);
    }
}