package com.flhs.preloader;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.home.HomeActivity;
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.EventObject;
import com.flhs.utils.ParserA;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class InitialLoader extends AppCompatActivity implements LoaderThread.OnFinish, ConnectionErrorFragment.AlertDialogListener {
    public static final int finishDelay = 500;
    public static final int panDuration = 10000;
    public static final int ITEM_SELECTED_DELAY = 100;

    public static String[] vevents;
    public static String ical;
    public static Calendar firstCalendarDay, lastCalendarDay;
    public static long firstCalendarDayMS, lastCalendarDayMS;
    public static HashMap<String, ArrayList<EventObject>> eventfulDays;
    public static byte[] menuBytes;
    public static SparseIntArray contentIdToNavId, contentIdToPos, navIdToBlackIcon, navIdToRedIcon;
    public static SparseArray<Class> navIdToClass;

    public static final SimpleDateFormat viewableDate = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US);
    public static final SimpleDateFormat veventDateTime = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
    public static final SimpleDateFormat veventDate = new SimpleDateFormat("yyyyMMdd", Locale.US);

    private TextView loadingText;

    public interface OnReloadFinish {
        void onReloadFinish();
    }

    @Override
    public void onCreate(Bundle savedInstancesState){
        super.onCreate(savedInstancesState);
        setContentView(R.layout.activity_initial_loader);

        ImageView background = findViewById(R.id.background);
        TranslateAnimation animation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, -450f,
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f);
        animation.setDuration(panDuration);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setInterpolator(new LinearInterpolator());
//        background.setAnimation(animation);

        findViewById(R.id.logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goHome = new Intent(InitialLoader.this, HomeActivity.class);
                startActivity(goHome);
            }
        });

        loadingText = findViewById(R.id.loading_text);

        tryToConnect();
    }

    public void tryToConnect(){
        if (FLHSActivity.isOnline(this)) {
            // Start loading the events if connected
            new LoaderThread(this, new WeakReference<>(loadingText)).execute();
        } else {
            // Show the connection error dialog if not connected
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(getFragmentManager(), "Connection Error");
        }
    }

    @Override
    public void onFinish(LoaderThread.ResultBundle resultBundle){
        if (resultBundle == LoaderThread.FAIL) {
            tryToConnect();
            return;
        }

        assign(resultBundle);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent goHome = new Intent(InitialLoader.this, HomeActivity.class);
                goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(goHome);
            }
        }, finishDelay);
    }

    public static void assign(LoaderThread.ResultBundle resultBundle){
        InitialLoader.ical = resultBundle.ical;
        InitialLoader.vevents = resultBundle.vevents;
        InitialLoader.eventfulDays = resultBundle.eventfulDays;
        InitialLoader.menuBytes = resultBundle.menuBytes;
        InitialLoader.contentIdToNavId = resultBundle.contentIdToNavId;
        InitialLoader.contentIdToPos = resultBundle.contentIdToPos;
        InitialLoader.navIdToBlackIcon = resultBundle.navIdToBlackIcon;
        InitialLoader.navIdToRedIcon = resultBundle.navIdToRedIcon;
        InitialLoader.navIdToClass = resultBundle.navIdToClass;


        // Get soft first and last days, changed later when exceeded
        Date today = new Date();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(today);

        firstCalendarDay = Calendar.getInstance();
        firstCalendarDay.setTime(today);
        firstCalendarDay.set(Calendar.MONTH, Calendar.SEPTEMBER);
        firstCalendarDay.set(Calendar.DAY_OF_MONTH, firstCalendarDay.getActualMinimum(Calendar.DAY_OF_MONTH));
        if (nowCal.before(firstCalendarDay))
            firstCalendarDay.roll(Calendar.YEAR, -1);

        lastCalendarDay = Calendar.getInstance();
        lastCalendarDay.setTime(today);
        lastCalendarDay.set(Calendar.MONTH, Calendar.JUNE);
        lastCalendarDay.set(Calendar.DAY_OF_MONTH, lastCalendarDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (nowCal.after(lastCalendarDay))
            lastCalendarDay.roll(Calendar.YEAR, 1);

        for (String vevent : InitialLoader.vevents) {
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

        firstCalendarDayMS = firstCalendarDay.getTimeInMillis();
        lastCalendarDayMS = lastCalendarDay.getTimeInMillis();
    }

    public static void setToMidnight(Calendar cal){
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public static void reload(AppCompatActivity activity, final OnReloadFinish onReloadFinish){
        if (FLHSActivity.isOnline(activity)){
            new LoaderThread(new LoaderThread.OnFinish() {
                @Override
                public void onFinish(LoaderThread.ResultBundle resultBundle) {
                    if (resultBundle == LoaderThread.FAIL)
                        return;

                    InitialLoader.assign(resultBundle);

                    onReloadFinish.onReloadFinish();
                }
            }, null).execute();
        } else {
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(activity.getFragmentManager(), "Connection Error");
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        tryToConnect();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        tryToConnect();
    }
}
