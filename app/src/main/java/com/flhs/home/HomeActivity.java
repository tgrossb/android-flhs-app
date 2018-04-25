package com.flhs.home;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.SignInActivity;
import com.flhs.utils.AccountInfo;
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.EventsAdapter;
import com.flhs.utils.ParserA;
import com.flhs.utils.EventObject;
import com.flhs.utils.NPALayoutManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;


public class HomeActivity extends FLHSActivity implements ConnectionErrorFragment.AlertDialogListener {
    public static boolean noSignIn = true;
    public static boolean dontRemember = true;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar mProgress;
    public TextView dateView;
    public RecyclerView eventsTodayList;
    public RecyclerView sportsEventsTodayList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_home);

        //Before anything happens, check if the user is signed in
        if (!noSignIn && (!AccountInfo.isSignedIn(this) || dontRemember)){
            Intent signIn = new Intent(this, SignInActivity.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(signIn);
        }

        String[] loadStrings = {"Loading events from the internet....."};
        ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, loadStrings);

        mProgress = findViewById(R.id.progress_bar);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryToConnect();
            }
        });
        // Set the list views to loading content message
        TabHost host = findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Events Today");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Events Today");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Sports Today");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Sports Today");
        host.addTab(spec);

        AnimatedTabHostListener hostListener = new AnimatedTabHostListener(host);
        host.setOnTabChangedListener(hostListener);

        eventsTodayList = findViewById(R.id.eventsTodayList);
        eventsTodayList.setLayoutManager(new NPALayoutManager(this));

        sportsEventsTodayList = findViewById(R.id.sportsEventsTodayList);
        sportsEventsTodayList.setLayoutManager(new NPALayoutManager(this));

        dateView = findViewById(R.id.dateHeader);
        tryToConnect();
    }

    public void tryToConnect(){
        swipeRefreshLayout.setRefreshing(false);
        if (isOnline()) {
            // Start loading the events if connected
            new EventsLoaderThread().execute();
        } else {
            // Show the connection error dialog if not connected
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(getFragmentManager(), "Connection Error");
            mProgress.setVisibility(View.INVISIBLE);
        }
    }

    // Load the events from the internet asynchronously
    class EventsLoaderThread extends AsyncTask<Void, Void, Void> {
        private ArrayList<EventObject> eventsToday;
        private ArrayList<EventObject> sportsEventsToday;

        @Override
        protected void onPreExecute() {
            dateView.setText("Loading...");
//            eventsTodayList.removeAllViews();
//            sportsEventsTodayList.removeAllViews();
            EventsAdapter eventsAdapter = (EventsAdapter) eventsTodayList.getAdapter();
            EventsAdapter sportsAdapter = (EventsAdapter) sportsEventsTodayList.getAdapter();
            if (eventsAdapter != null)
                eventsAdapter.removeAllItems();
            if (sportsAdapter != null)
                sportsAdapter.removeAllItems();
            mProgress.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            eventsToday = ParserA.parseEventsToday();
            sportsEventsToday = ParserA.printSportsToday();
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            // Format date to be "DayOfWeek Month Day, Year"
            SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US);
            String date = dateFormat.format(new Date());
            dateView.setText(date);

            if (eventsToday == null) {
                if (getApplicationContext().equals(HomeActivity.this)){
                    ConnectionErrorFragment errorAlert = new ConnectionErrorFragment();
                    errorAlert.show(getFragmentManager(), "Connection Error");
                }
                eventsToday = new ArrayList<>(1);
                eventsToday.add(new EventObject("Try again soon", "Couldn't connect to bcsdny.org....."));
            } else if (eventsToday.size() == 0)
                eventsToday.add(new EventObject("All day", "No Events Today!"));

            if (sportsEventsToday == null) {
                if (getApplicationContext().equals(HomeActivity.this)){
                    ConnectionErrorFragment errorAlert = new ConnectionErrorFragment();
                    errorAlert.show(getFragmentManager(), "Connection Error");
                }
                sportsEventsToday = new ArrayList<>(1);
                sportsEventsToday.add(new EventObject("Try again soon", "Couldn't connect to sportspak.swboces.org....."));
            } else if (sportsEventsToday.size() == 0)
                sportsEventsToday.add(new EventObject("All day", "No Events Today!"));


            //Events Today ListView
            EventsAdapter eventsTodayAdapter = new EventsAdapter(eventsToday, getResources().getColor(R.color.soft_red));
            eventsTodayList.setAdapter(eventsTodayAdapter);

            //SportsNews ListView
            EventsAdapter sportsEventsTodayAdapter = new EventsAdapter(sportsEventsToday, getResources().getColor(R.color.soft_blue));
            sportsEventsTodayList.setAdapter(sportsEventsTodayAdapter);

            eventsTodayList.setClickable(false);
            sportsEventsTodayList.setClickable(false);
            mProgress.setVisibility(ProgressBar.INVISIBLE);
//            justifyListViewHeightBasedOnChildren(eventsTodayList);
//            justifyListViewHeightBasedOnChildren(sportsEventsTodayList);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        tryToConnect();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        mProgress.setVisibility(ProgressBar.INVISIBLE);
    }
}