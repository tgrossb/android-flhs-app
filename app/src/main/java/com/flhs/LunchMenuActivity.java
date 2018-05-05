package com.flhs;

import com.flhs.calendar.EventDecorator;
import com.flhs.preloader.InitialLoader;
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.Database;
import com.flhs.utils.Formatter;
import com.flhs.utils.ParserA;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LunchMenuActivity extends FLHSActivity implements OnLoadCompleteListener, InitialLoader.OnReloadFinish {
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar progressBar;
    private PDFView viewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_lunch_menu);
/*
        WebView LunchMenuPDFReader = findViewById(R.id.Lunch_Menu_Web_View);

        //I skipped this part.... too lazy to check for Google Drive Viewer Consistencies..... I just put the Google Drive Viewer URL directly in
        String PDFReaderURL = Formatter.googleDriveViewerURLFormat("http://bcsdny.org/files/filesystem/flmsflhslunchmenu1.pdf");
        //Ok... I use the rest of this stuff now.
//        LunchMenuPDFReader.setWebViewClient(new WebViewClient());
        LunchMenuPDFReader.getSettings().setJavaScriptEnabled(true);
//        settings.setBuiltInZoomControls(true);
//        settings.setDisplayZoomControls(true);
        SimpleDateFormat urlForm = new SimpleDateFormat("MMMMyyyy'MSHS'", Locale.US);
//        String url = "http://www.bcsdny.org/site/handlers/filedownload.ashx?moduleinstanceid=2804&dataid=8076&FileName=" + urlForm.format(new Date()) + "Lunch.pdf";
        String url = "https://www.adobe.com/content/dam/acom/en/devnet/acrobat/pdfs/pdf_open_parameters.pdf";
//        String url = "https://www.bcsdny.org/site/handlers/filedownload.ashx?moduleinstanceid=2804&dataid=8076&FileName=May2018MSHSLunch.pdf";
        url = "http://docs.google.com/gview?embedded=true&url=" + url;
        LunchMenuPDFReader.loadUrl(url);
        System.out.println(url);
*/
        viewer = findViewById(R.id.lunchMenuViewer);

        refreshLayout = findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                tryToConnect();
            }
        });

        progressBar = findViewById(R.id.progressBar);

        preload();
        onReloadFinish();
    }


    public void tryToConnect() {
        refreshLayout.setRefreshing(false);
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
        viewer.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void reversePreload() {
        progressBar.setVisibility(View.INVISIBLE);
        viewer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReloadFinish() {
        reversePreload();
        viewer.fromBytes(InitialLoader.menuBytes)
                .defaultPage(0)
                .enableSwipe(true)
                .onLoad(this)
                .load();
    }

    @Override
    public void loadComplete(int nbChanges) {
        // Change progress bar
    }

    /*Just might be useful someday with UP navigation in Action Bar....
    **
	 * Set up the {@link android.app.ActionBar}.
	 **
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home_red:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
*/
}
