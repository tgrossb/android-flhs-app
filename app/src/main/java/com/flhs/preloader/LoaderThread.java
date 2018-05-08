package com.flhs.preloader;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
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
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.EventObject;
import com.flhs.utils.ParserA;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.util.IOUtils;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class LoaderThread extends AsyncTask<Void, String, LoaderThread.ResultBundle> {
    public static final ResultBundle FAIL = null;
    private OnFinish finished;
    private WeakReference<Context> contextRef;
    private WeakReference<TextView> textViewRef;

    public interface OnFinish {
        void onFinish(ResultBundle results);
    }

    public LoaderThread(OnFinish finished, WeakReference<Context> contextRef, WeakReference<TextView> textViewRef){
        this.finished = finished;
        this.contextRef = contextRef;
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
        results.ical = ical;

        publishProgress("Parsing calendars...");
        String[] vevents = ical.split("END:VEVENT\nBEGIN:VEVENT");
        int firstStart = vevents[0].indexOf("BEGIN:VEVENT");
        vevents[0] = vevents[0].substring(firstStart + 12);
        results.vevents = vevents;

        publishProgress("Loading eventful days from BCSD...");
        results.eventfulDays = ParserA.getEventfulDays(vevents);

        publishProgress("Finding calendar start and stop days...");
        extractDays(results, vevents);

        publishProgress("Loading lunch menu from BCSD...");
        loadLunchMenu(results);

        publishProgress("Loading announcements from BCSD...");
        loadAnnouncements(results);

        publishProgress("Assembling identifier to navigation maps...");
        results.contentIdToNavId = buildContentIdToNavId();

        publishProgress("Assembling identifier to position maps...");
        results.contentIdToPos = buildContentIdToPos();

        publishProgress("Assembling navigation to class maps...");
        results.navIdToClass = buildNavIdToClass();

        publishProgress("Assembling navigation to icon maps...");
        results.navIdToRedIcon = buildNavIdToRedIcon();

        publishProgress("Looking for signed in accounts...");
        results.googleAccount = GoogleSignIn.getLastSignedInAccount(contextRef.get());

        publishProgress("Finished!");
        return results;
    }

    @Override
    public void onProgressUpdate(String... progress){
        if (textViewRef == null || textViewRef.get() == null)
            return;
        textViewRef.get().setText(progress[0]);
    }

    private void extractDays(ResultBundle resultBundle, String[] vevents){
        Date today = new Date();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(today);

        Calendar firstCalendarDay = Calendar.getInstance();
        firstCalendarDay.setTime(today);
        firstCalendarDay.set(Calendar.MONTH, Calendar.SEPTEMBER);
        firstCalendarDay.set(Calendar.DAY_OF_MONTH, firstCalendarDay.getActualMinimum(Calendar.DAY_OF_MONTH));
        if (nowCal.before(firstCalendarDay))
            firstCalendarDay.roll(Calendar.YEAR, -1);

        Calendar lastCalendarDay = Calendar.getInstance();
        lastCalendarDay.setTime(today);
        lastCalendarDay.set(Calendar.MONTH, Calendar.JUNE);
        lastCalendarDay.set(Calendar.DAY_OF_MONTH, lastCalendarDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (nowCal.after(lastCalendarDay))
            lastCalendarDay.roll(Calendar.YEAR, 1);

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
            if (firstCalendarDay.after(startCal))
                firstCalendarDay.setTime(start);
            if (lastCalendarDay.before(endCal))
                lastCalendarDay.setTime(end);
        }
        firstCalendarDay.set(Calendar.DAY_OF_MONTH, firstCalendarDay.getActualMinimum(Calendar.DAY_OF_MONTH));
        lastCalendarDay.set(Calendar.DAY_OF_MONTH, lastCalendarDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        setToMidnight(firstCalendarDay);
        setToMidnight(lastCalendarDay);

        resultBundle.firstCalendarDay = firstCalendarDay;
        resultBundle.lastCalendarDay = lastCalendarDay;
        resultBundle.firstCalendarDayMS = firstCalendarDay.getTimeInMillis();
        resultBundle.lastCalendarDayMS = lastCalendarDay.getTimeInMillis();
    }

    private static void setToMidnight(Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void loadLunchMenu(ResultBundle resultBundle){
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
        resultBundle.menuBytes = menuBytes;
    }

    private void loadAnnouncements(ResultBundle resultBundle){
        String url = "https://www.bcsdny.org/domain/154";
        Document announce;
        try {
            announce = Jsoup.connect(url).get();
            Element announceEl = announce.getElementsByClass("ui-article-description").get(0).child(0).child(0);
            Elements announcementEls = announceEl.children();
            // First element is date, second is day, third is just to read all - remove that one
            SimpleDateFormat announceToDate = new SimpleDateFormat("E, MMMMM d, yyyy", Locale.US);
            SimpleDateFormat dateToText = new SimpleDateFormat("EE M/d/yy", Locale.US);
            String unformedDate = htmlToString(announcementEls.get(0));
//            String announcementDateString = dateToText.format(announceToDate.parse(unformedDate));
            String announcementDateString = InitialLoader.viewableDate.format(announceToDate.parse(unformedDate));
            String announcementDayTypeString = htmlToString(announcementEls.get(1));
            ArrayList<String> announcements = new ArrayList<>(announcementEls.size());
            for (int c=3; c<announcementEls.size(); c++)
                announcements.add(htmlToString(announcementEls.get(c)));
            resultBundle.announcements = announcements;
            resultBundle.announcementDateString = announcementDateString;
            resultBundle.announcementDayTypeString = announcementDayTypeString;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private String htmlToString(Element html){
        return Html.fromHtml(html.html()).toString();
    }

    private SparseIntArray buildContentIdToNavId(){
        SparseIntArray contentIdToNavId = new SparseIntArray(6);
        contentIdToNavId.put(R.layout.activity_announcement, R.id.nav_announcements);
        contentIdToNavId.put(R.layout.activity_calendar, R.id.nav_calendar);
        contentIdToNavId.put(R.layout.activity_lunch_menu, R.id.nav_lunch_menu);
        contentIdToNavId.put(R.layout.activity_sports_navigation, R.id.nav_sports);
        contentIdToNavId.put(R.layout.temp_sports, R.id.nav_sports);
        contentIdToNavId.put(R.layout.activity_schedule, R.id.nav_bell_schedule);
        return contentIdToNavId;
    }

    private SparseIntArray buildContentIdToPos(){
        SparseIntArray contentIdToPos = new SparseIntArray(6);
        contentIdToPos.put(R.layout.activity_announcement, 0);
        contentIdToPos.put(R.layout.activity_calendar, 1);
        contentIdToPos.put(R.layout.activity_lunch_menu, 2);
        contentIdToPos.put(R.layout.activity_sports_navigation, 3);
        contentIdToPos.put(R.layout.temp_sports, 3);
        contentIdToPos.put(R.layout.activity_schedule, 4);
        return contentIdToPos;
    }

    private SparseArray<Class> buildNavIdToClass() {
        SparseArray<Class> navIdToClass = new SparseArray<>(5);
        navIdToClass.put(R.id.nav_announcements, AnnouncementActivity.class);
        navIdToClass.put(R.id.nav_bell_schedule, ScheduleActivity.class);
        navIdToClass.put(R.id.nav_calendar, CalendarActivity.class);
        navIdToClass.put(R.id.nav_lunch_menu, LunchMenuActivity.class);
        navIdToClass.put(R.id.nav_sports, TempSportsActivity.class);
        return navIdToClass;
    }

    private SparseIntArray buildNavIdToRedIcon(){
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
        Calendar firstCalendarDay, lastCalendarDay;
        long firstCalendarDayMS, lastCalendarDayMS;
        ArrayList<String> announcements;
        String announcementDateString, announcementDayTypeString;
        byte[] menuBytes;
        SparseIntArray contentIdToNavId, contentIdToPos, navIdToRedIcon;
        SparseArray<Class> navIdToClass;
        GoogleSignInAccount googleAccount;
    }
}