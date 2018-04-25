package com.flhs.announcements;

import com.flhs.FLHSActivity;
import com.flhs.R;
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


public class AnnouncementActivity extends FLHSActivity implements ConnectionErrorFragment.AlertDialogListener {
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
/*
        refresh = findViewById(R.id.refresh);
        refresh.setVisibility(View.INVISIBLE);
        refresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                tryToConnect();
            }
        });
*/
        tryToConnect();

    }

    public void tryToConnect() {
        swipeRefreshLayout.setRefreshing(false);
        if (isOnline()) {
            new AnnouncementParser().execute();
        } else {
            ConnectionErrorFragment alert = new ConnectionErrorFragment();
            alert.show(getFragmentManager(), "CONNECTION_ERROR");
            mProgress.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    private class AnnouncementParser extends AsyncTask<Void, Void, Void> {
        private String dateString, dayTypeString;
        private ArrayList<String> announcements;
        @Override
        protected void onPreExecute() {
            date.setText("Loading...");
            dayType.setVisibility(View.INVISIBLE);
            AnnouncementsAdapter announcementsAdapter = (AnnouncementsAdapter) announcementsList.getAdapter();
            if (announcementsAdapter != null)
                announcementsAdapter.removeAllItems();
//            refresh.setVisibility(View.INVISIBLE);
            mProgress.setVisibility(ProgressBar.VISIBLE);
            dateString = "";
            dayTypeString = "";
            announcements = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url = "https://www.bcsdny.org/domain/154";
            Document announce = null;
            try {
                announce = Jsoup.connect(url).get();
                Element announceEl = announce.getElementsByClass("ui-article-description").get(0).child(0).child(0);
                Elements announcementEls = announceEl.children();
                // First element is date, second is day, third is just to read all - remove that one
                SimpleDateFormat announceToDate = new SimpleDateFormat("E, MMMMM d, yyyy", Locale.US);
                SimpleDateFormat dateToText = new SimpleDateFormat("EE M/d/yy", Locale.US);
                String unformedDate = htmlToString(announcementEls.get(0));
                dateString = dateToText.format(announceToDate.parse(unformedDate));
                dayTypeString = htmlToString(announcementEls.get(1));
                for (int c=3; c<announcementEls.size(); c++)
                    announcements.add(htmlToString(announcementEls.get(c)));
            } catch (Exception e1) {
                e1.printStackTrace();
                ConnectionErrorFragment alert = new ConnectionErrorFragment();
                alert.show(getFragmentManager(), "CONNECTION_ERROR");
            }

            return null;
        }

        private String htmlToString(Element html){
            return Html.fromHtml(html.html()).toString();
        }

        @Override
        protected void onPostExecute(Void result) {
            date.setText(dateString);
            dayType.setText(dayTypeString);

            // Convert the list into one card
            ArrayList<String> announcementContainer = new ArrayList<>(1);
            String announcement = "";
            for (String announcementLine : announcements)
                announcement += announcementLine + "\n\n";
            if (announcements.size() > 0)
                announcementContainer.add(announcement.substring(0, announcement.length()-4));
            else
                announcementContainer.add("Couldn't connect to bcsdny.org");
            AnnouncementsAdapter announcementsAdapter = new AnnouncementsAdapter(announcementContainer, getResources().getColor(R.color.soft_red));
            announcementsList.setAdapter(announcementsAdapter);

            announcementsList.setClickable(false);
            dayType.setVisibility(View.VISIBLE);
            mProgress.setVisibility(ProgressBar.INVISIBLE);
//            refresh.setVisibility(View.VISIBLE);
//            swipeRefreshLayout.setRefreshing(false);
        }
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
