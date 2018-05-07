package com.flhs.announcements;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.preloader.InitialLoader;
import com.flhs.utils.ConnectionErrorFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.AsyncTask;
import android.app.ProgressDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class AnnouncementActivity extends FLHSActivity implements ConnectionErrorFragment.AlertDialogListener, InitialLoader.OnReloadFinish {
    private TextView date, dayType;
    private RecyclerView announcementsList;
    private ProgressBar mProgress;
//    private Button refresh;
    private SwipeRefreshLayout swipeRefreshLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_announcement);

        mProgress = findViewById(R.id.progressBar1);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryToConnect();
            }
        });

        date = findViewById(R.id.date);
        dayType = findViewById(R.id.dayType);

        announcementsList = findViewById(R.id.announcements_holder);
        announcementsList.setLayoutManager(new LinearLayoutManager(this));

        reversePreload();
        onReloadFinish();
    }

    public void tryToConnect() {
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

    public void preload() {
        date.setText("Loading...");
        dayType.setVisibility(View.INVISIBLE);
        AnnouncementsAdapter announcementsAdapter = (AnnouncementsAdapter) announcementsList.getAdapter();
        if (announcementsAdapter != null)
            announcementsAdapter.removeAllItems();
        mProgress.setVisibility(View.VISIBLE);
    }

    public void reversePreload() {
        date.setText(InitialLoader.announcementDateString);
        dayType.setVisibility(View.VISIBLE);
        dayType.setText(InitialLoader.announcementDayTypeString);
        mProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onReloadFinish() {
        reversePreload();

        // Convert the list into one card
        ArrayList<String> announcementContainer = new ArrayList<>(1);
        String announcement = "";
        for (String announcementLine : InitialLoader.announcements)
            announcement += announcementLine + "\n\n";
        if (InitialLoader.announcements.size() > 0)
            announcementContainer.add(announcement.substring(0, announcement.length()-4));
        else
            announcementContainer.add("Couldn't connect to bcsdny.org");
        AnnouncementsAdapter announcementsAdapter = new AnnouncementsAdapter(announcementContainer, getResources().getColor(R.color.soft_red));
        announcementsList.setAdapter(announcementsAdapter);
        announcementsList.setClickable(false);
    }

    @Override
    public void onDialogPositiveClick(android.app.DialogFragment dialog) {
        tryToConnect();
    }

    @Override
    public void onDialogNegativeClick(android.app.DialogFragment dialog) {
        mProgress.setVisibility(ProgressBar.INVISIBLE);
    }
}
