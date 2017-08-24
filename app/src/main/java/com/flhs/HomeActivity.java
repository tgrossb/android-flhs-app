//Written by Drew Gregory..... 2/5/2014
package com.flhs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.ListViewHolderItem;
import com.flhs.utils.ParserA;
import com.flhs.utils.SportEvent;
import com.flhs.utils.eventobject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class HomeActivity extends FLHSActivity implements ConnectionErrorFragment.AlertDialogListener {
    private ProgressBar mProgress;
    private ParserA parser = new ParserA();
    public ListView eventstodaylv;
    public TextView txtdate;
    public ListView sporteventstodaylv;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setIcon(R.drawable.ic_launcher);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SetupNavDrawer();

        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        //Sets listviews as "Loading...."
        eventstodaylv = (ListView) findViewById(R.id.eventsTodayListView);
        eventstodaylv.setScrollContainer(false);
        eventstodaylv.setAdapter(loadingAdapter);

        sporteventstodaylv = (ListView) findViewById(R.id.sporteventsTodayListView);
        sporteventstodaylv.setAdapter(loadingAdapter);


        //datestring is todays date formatted to be "DayOfWeek Month Day, Year"
        String datestring = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US).format(new Date());

        //Current Date
        txtdate = (TextView) findViewById(R.id.dateHeader);
        txtdate.setText(datestring);
        tryToConnect();
    }

    void tryToConnect() {
        if (isOnline()) {
            //Runs the loader thread!
            new EventsLoaderThread().execute();
        } else {
            ConnectionErrorFragment errorAlert = new ConnectionErrorFragment();
            errorAlert.show(getFragmentManager(), "Connection Error");
        }


    }

    class EventsLoaderThread extends AsyncTask<Void, Void, Void> {
        ProgressDialog pr = new ProgressDialog(HomeActivity.this);
        ArrayList<eventobject> todaysevents;
        ArrayList<SportEvent> sportEventsToday;

        @Override
        protected void onPreExecute() {
            mProgress.setVisibility(ProgressBar.VISIBLE);
        }


        @Override
        protected Void doInBackground(Void... arg0) {
            todaysevents = ParserA.parseEventsToday();
            sportEventsToday = ParserA.printSportsToday();
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            //Events Today Format
            String[] lvEventsArray = {"Couldn't connect to bcsdny.org......."};
            if (todaysevents != null) {
                if (todaysevents.size() != 0) {
                    lvEventsArray = new String[todaysevents.size()];
                    for (int lvEventsArrayIndex = 0; lvEventsArrayIndex < todaysevents.size(); lvEventsArrayIndex++)
                        lvEventsArray[lvEventsArrayIndex] = todaysevents.get(lvEventsArrayIndex).eventsDesc[0] + "\n" + todaysevents.get(lvEventsArrayIndex).Date;
                }
                if (todaysevents.size() == 0) {
                    lvEventsArray = new String[1];
                    lvEventsArray[0] = "No Events Today!";
                }
            }
            if (todaysevents == null) {
                if (getApplicationContext().equals(HomeActivity.this)) {
                    ConnectionErrorFragment errorAlert = new ConnectionErrorFragment();
                    errorAlert.show(getFragmentManager(), "Connection Error");
                }
            }


            //Sports News Today Format
            String[] lvSportEventsArray = {"Couldn't connect to sportspak.swboces.org....."};
            if (sportEventsToday != null) {
                if (sportEventsToday.size() == 0) {
                    lvSportEventsArray = new String[1];
                    lvSportEventsArray[0] = "No Games Today!";
                } else {
                    lvSportEventsArray = new String[sportEventsToday.size()];
                    for (int lvSportEventsArrayIndex = 0; lvSportEventsArrayIndex < sportEventsToday.size(); lvSportEventsArrayIndex++) {
                        SportEvent indexedgame = sportEventsToday.get(lvSportEventsArrayIndex);
                        lvSportEventsArray[lvSportEventsArrayIndex] = indexedgame.getTime() + "\n"
                                + indexedgame.getSport() + "\n" + indexedgame.getHomeTeam() + " vs " + indexedgame.getVisitingTeam();
                    }
                }
            } else {
                if (getApplicationContext().equals(HomeActivity.this)) {
                    ConnectionErrorFragment errorAlert = new ConnectionErrorFragment();
                    errorAlert.show(getFragmentManager(), "Connection Error");
                }
            }


            //Events Today ListView
            EventsAdapter eventsTodayLvAdapter = new EventsAdapter(HomeActivity.this, lvEventsArray);

            eventstodaylv.setAdapter(eventsTodayLvAdapter);

            //SportsNews ListView

            EventsAdapter sporteventstodayadapter = new EventsAdapter(HomeActivity.this, lvSportEventsArray);
            sporteventstodaylv.setAdapter(sporteventstodayadapter);

            eventstodaylv.setClickable(false);
            sporteventstodaylv.setClickable(false);
            mProgress.setVisibility(ProgressBar.INVISIBLE);
            justifyListViewHeightBasedOnChildren(eventstodaylv);
            justifyListViewHeightBasedOnChildren(sporteventstodaylv);
        }
    }

    public void justifyListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null)
            return;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight() + 50;
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    private class EventsAdapter extends ArrayAdapter<String> {
        private String[] events;
        private Context context;

        EventsAdapter(Context context, String[] Events) {
            super(context, R.layout.events_today_list_view_item, Events);
            this.events = Events;
            this.context = context;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolderItem item;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.events_today_list_view_item, parent, false);
                item = new ListViewHolderItem();
                item.courseName = (TextView) convertView.findViewById(R.id.eventsListTextView); //Too lazy to make different ListViewHolderItem.... courseName will be the TextView we will use :-)
                convertView.setTag(item);
            } else {
                item = (ListViewHolderItem) convertView.getTag();
            }

            item.courseName.setText(events[position]);
            return convertView;
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