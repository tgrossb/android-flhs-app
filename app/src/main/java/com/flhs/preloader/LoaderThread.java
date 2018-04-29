package com.flhs.preloader;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flhs.utils.EventObject;
import com.flhs.utils.ParserA;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class LoaderThread extends AsyncTask<Void, String, LoaderThread.ResultBundle> {
    public static final String FAIL = null;
    private OnFinish finished;
    private WeakReference<TextView> textViewRef;

    public interface OnFinish {
        void onFinish(String ical, String[] vevents, HashMap<String, ArrayList<EventObject>> eventfulDays);
    }

    public LoaderThread(OnFinish finished, WeakReference<TextView> textViewRef){
        this.finished = finished;
        this.textViewRef = textViewRef;
    }

    @Override
    protected void onPostExecute(ResultBundle resultBundle) {
        if (resultBundle.ical == null || resultBundle.vevents == null || resultBundle.eventfulDays == null){
            publishProgress("An error occurred, trying again");
            finished.onFinish(null, null, null);
            return;
        }

        finished.onFinish(resultBundle.ical, resultBundle.vevents, resultBundle.eventfulDays);
    }

    @Override
    protected ResultBundle doInBackground(Void... arg0) {
        publishProgress("Loading calendars from BCSD...");
        String ical = ParserA.readCalendar(ParserA.DISTRICT_CALENDAR);

        publishProgress("Parsing calendars...");
        String[] vevents = ical.split("END:VEVENT\nBEGIN:VEVENT");
        int firstStart = vevents[0].indexOf("BEGIN:VEVENT");
        vevents[0] = vevents[0].substring(firstStart + 12);

        publishProgress("Loading eventful days from BCSD...");
        HashMap<String, ArrayList<EventObject>> eventfulDays = ParserA.getEventfulDays(vevents);

        publishProgress("Finished!");
        return new ResultBundle().setIcal(ical).setVevents(vevents).setEventfulDays(eventfulDays);
    }

    @Override
    public void onProgressUpdate(String... progress){
        if (textViewRef == null || textViewRef.get() == null)
            return;
        textViewRef.get().setText(progress[0]);
    }

    static class ResultBundle {
        String ical;
        String[] vevents;
        HashMap<String, ArrayList<EventObject>> eventfulDays;

        ResultBundle setIcal(String ical){
            this.ical = ical;
            return this;
        }

        ResultBundle setVevents(String[] vevents){
            this.vevents = vevents;
            return this;
        }

        ResultBundle setEventfulDays(HashMap<String, ArrayList<EventObject>> eventfulDays){
            this.eventfulDays = eventfulDays;
            return this;
        }
    }
}