package com.flhs.preloader;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flhs.LunchMenuActivity;
import com.flhs.R;
import com.flhs.ScheduleActivity;
import com.flhs.TempSportsActivity;
import com.flhs.announcements.AnnouncementActivity;
import com.flhs.calendar.CalendarActivity;
import com.flhs.utils.EventObject;
import com.flhs.utils.ParserA;
import com.google.android.gms.common.util.IOUtils;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class LoaderThread extends AsyncTask<Void, String, LoaderThread.ResultBundle> {
    public static final ResultBundle FAIL = null;
    private OnFinish finished;
    private WeakReference<TextView> textViewRef;

    public interface OnFinish {
        void onFinish(ResultBundle results);
    }

    public LoaderThread(OnFinish finished, WeakReference<TextView> textViewRef){
        this.finished = finished;
        this.textViewRef = textViewRef;
    }

    @Override
    protected void onPostExecute(ResultBundle resultBundle) {
        if (resultBundle.ical == null || resultBundle.vevents == null || resultBundle.eventfulDays == null || resultBundle.menuBytes == null){
            publishProgress("An error occurred, trying again");
            finished.onFinish(null);
            return;
        }

        finished.onFinish(resultBundle);
    }

    @Override
    protected ResultBundle doInBackground(Void... arg0) {
        ResultBundle results = new ResultBundle();

        publishProgress("Loading calendars from BCSD...");
        String ical = ParserA.readCalendar(ParserA.DISTRICT_CALENDAR);
        results.setIcal(ical);

        publishProgress("Parsing calendars...");
        String[] vevents = ical.split("END:VEVENT\nBEGIN:VEVENT");
        int firstStart = vevents[0].indexOf("BEGIN:VEVENT");
        vevents[0] = vevents[0].substring(firstStart + 12);
        results.setVevents(vevents);

        publishProgress("Loading eventful days from BCSD...");
        HashMap<String, ArrayList<EventObject>> eventfulDays = ParserA.getEventfulDays(vevents);
        results.setEventfulDays(eventfulDays);

        publishProgress("Loading lunch menu from BCSD...");
        byte[] menuBytes = null;
        try {
            SimpleDateFormat urlForm = new SimpleDateFormat("MMMMyyyy'MSHS'", Locale.US);
            String urlString = "https://www.bcsdny.org/site/handlers/filedownload.ashx?moduleinstanceid=2804&dataid=8076&FileName=" +
                    urlForm.format(new Date()) + "Lunch.pdf";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream is = connection.getInputStream();
            menuBytes = IOUtils.toByteArray(is);
        } catch (Exception e){
            System.out.println("Error loading menu");
            e.printStackTrace();
        }
        results.setMenuBytes(menuBytes);

        publishProgress("Assembling identifier to navigation maps...");
        results.setContentIdToNavId(buildContentIdToNavId());

        publishProgress("Assembling identifier to position maps...");
        results.setContentIdToPos(buildContentIdToPos());

        publishProgress("Assembling navigation to class maps...");
        results.setNavIdToClass(buildNavIdToClass());

        publishProgress("Assembling navigation to icon maps...");
        results.setNavIdToBlackIcon(buildNavIdToBlackIcon());
        results.setNavIdToRedIcon(buildNavIdToRedIcon());

        publishProgress("Finished!");
        return results;
    }

    @Override
    public void onProgressUpdate(String... progress){
        if (textViewRef == null || textViewRef.get() == null)
            return;
        textViewRef.get().setText(progress[0]);
    }


    public SparseIntArray buildContentIdToNavId(){
        SparseIntArray contentIdToNavId = new SparseIntArray(6);
        contentIdToNavId.put(R.layout.activity_announcement, R.id.nav_announcements);
        contentIdToNavId.put(R.layout.activity_calendar, R.id.nav_calendar);
        contentIdToNavId.put(R.layout.activity_lunch_menu, R.id.nav_lunch_menu);
        contentIdToNavId.put(R.layout.activity_sports_navigation, R.id.nav_sports);
        contentIdToNavId.put(R.layout.temp_sports, R.id.nav_sports);
        contentIdToNavId.put(R.layout.activity_schedule, R.id.nav_bell_schedule);
        return contentIdToNavId;
    }

    public SparseIntArray buildContentIdToPos(){
        SparseIntArray contentIdToPos = new SparseIntArray(6);
        contentIdToPos.put(R.layout.activity_announcement, 0);
        contentIdToPos.put(R.layout.activity_calendar, 1);
        contentIdToPos.put(R.layout.activity_lunch_menu, 2);
        contentIdToPos.put(R.layout.activity_sports_navigation, 3);
        contentIdToPos.put(R.layout.temp_sports, 3);
        contentIdToPos.put(R.layout.activity_schedule, 4);
        return contentIdToPos;
    }

    public SparseArray<Class> buildNavIdToClass() {
        SparseArray<Class> navIdToClass = new SparseArray<>(5);
        navIdToClass.put(R.id.nav_announcements, AnnouncementActivity.class);
        navIdToClass.put(R.id.nav_bell_schedule, ScheduleActivity.class);
        navIdToClass.put(R.id.nav_calendar, CalendarActivity.class);
        navIdToClass.put(R.id.nav_lunch_menu, LunchMenuActivity.class);
        navIdToClass.put(R.id.nav_sports, TempSportsActivity.class);
        return navIdToClass;
    }

    public SparseIntArray buildNavIdToBlackIcon(){
        SparseIntArray navIdToBlackIcon = new SparseIntArray(5);
        navIdToBlackIcon.put(R.id.nav_announcements, R.drawable.announcements_icon_black);
        navIdToBlackIcon.put(R.id.nav_calendar, R.drawable.calendar_icon_black);
        navIdToBlackIcon.put(R.id.nav_lunch_menu, R.drawable.lunch_menu_black);
        navIdToBlackIcon.put(R.id.nav_sports, R.drawable.sports_icon_black);
        navIdToBlackIcon.put(R.id.nav_bell_schedule, R.drawable.schedule_black);
        return navIdToBlackIcon;
    }

    public SparseIntArray buildNavIdToRedIcon(){
        SparseIntArray navIdToRedIcon = new SparseIntArray(5);
        navIdToRedIcon.put(R.id.nav_announcements, R.drawable.announcements_icon_red);
        navIdToRedIcon.put(R.id.nav_calendar, R.drawable.calendar_icon_red);
        navIdToRedIcon.put(R.id.nav_lunch_menu, R.drawable.lunch_menu_red);
        navIdToRedIcon.put(R.id.nav_sports, R.drawable.sports_icon_red);
        navIdToRedIcon.put(R.id.nav_bell_schedule, R.drawable.schedule_red);
        return navIdToRedIcon;
    }

    static class ResultBundle {
        String ical;
        String[] vevents;
        HashMap<String, ArrayList<EventObject>> eventfulDays;
        byte[] menuBytes;
        SparseIntArray contentIdToNavId, contentIdToPos, navIdToBlackIcon, navIdToRedIcon;
        SparseArray<Class> navIdToClass;

        void setIcal(String ical){
            this.ical = ical;
        }

        void setVevents(String[] vevents){
            this.vevents = vevents;
        }

        void setEventfulDays(HashMap<String, ArrayList<EventObject>> eventfulDays){
            this.eventfulDays = eventfulDays;
        }

        void setMenuBytes(byte[] menuBytes){
            this.menuBytes = menuBytes;
        }

        void setContentIdToNavId(SparseIntArray contentIdToNavId){
            this.contentIdToNavId = contentIdToNavId;
        }

        void setContentIdToPos(SparseIntArray contentIdToPos){
            this.contentIdToPos = contentIdToPos;
        }

        void setNavIdToBlackIcon(SparseIntArray navIdToBlackIcon){
            this.navIdToBlackIcon = navIdToBlackIcon;
        }

        void setNavIdToRedIcon(SparseIntArray navIdToRedIcon){
            this.navIdToRedIcon = navIdToRedIcon;
        }

        void setNavIdToClass(SparseArray<Class> navIdToClass){
            this.navIdToClass = navIdToClass;
        }
    }
}