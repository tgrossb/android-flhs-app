package com.flhs.home;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.ParserA;
import com.flhs.utils.SportsEvent;
import com.flhs.utils.EventObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;


public class HomeActivity extends FLHSActivity implements ConnectionErrorFragment.AlertDialogListener {
    private ProgressBar mProgress;
    public TextView dateView;
    public RecyclerView eventsTodayList;
    public RecyclerView sportsEventsTodayList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        return super.onCreateOptionsMenu(menu);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_home);

        String[] loadStrings = {"Loading events from the internet....."};
        ArrayAdapter<String> loadingAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, loadStrings);

        mProgress = findViewById(R.id.progress_bar);
        // Set the list views to loading content message

        TabHost host = (TabHost)findViewById(R.id.tabHost);
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
        eventsTodayList.setLayoutManager(new LinearLayoutManager(this));

        sportsEventsTodayList = findViewById(R.id.sportsEventsTodayList);
        sportsEventsTodayList.setLayoutManager(new LinearLayoutManager(this));


        // Format date to be "DayOfWeek Month Day, Year"
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US);
        String date = dateFormat.format(new Date());

        // Set the date text view
        dateView = findViewById(R.id.dateHeader);
        dateView.setText(date);
        tryToConnect();
    }

    public void tryToConnect(){
        if (isOnline()) {
            // Start loading the events if connected
            new EventsLoaderThread().execute();
        } else {
            // Show the connection error dialog if not connected
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(getFragmentManager(), "Connection Error");
        }
    }

    // Load the events from the internet asynchronously
    class EventsLoaderThread extends AsyncTask<Void, Void, Void> {
        private ArrayList<EventObject> eventsToday;
        private ArrayList<EventObject> sportsEventsToday;

        @Override
        protected void onPreExecute() {
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

    public void justifyListViewHeightBasedOnChildren(RecyclerView list) {
        EventsAdapter adapter = (EventsAdapter) list.getAdapter();
        if (adapter == null)
            return;
        int totalHeight = list.getHeight();

        System.out.println(totalHeight);
        ViewGroup.LayoutParams par = list.getLayoutParams();
        par.height = totalHeight;
        list.setLayoutParams(par);
        list.requestLayout();
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