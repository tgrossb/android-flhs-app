package com.flhs.preloader;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.TextViewCompat;
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
import android.widget.Toast;

import com.flhs.FLHSActivity;
import com.flhs.R;
import com.flhs.home.HomeActivity;
import com.flhs.utils.AccountInfo;
import com.flhs.utils.BadSignInFragment;
import com.flhs.utils.ConnectionErrorFragment;
import com.flhs.utils.EventObject;
import com.flhs.utils.ParserA;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class InitialLoader extends AppCompatActivity implements LoaderThread.OnFinish, ConnectionErrorFragment.AlertDialogListener, BadSignInFragment.SignInCallback {
    public static final int finishDelay = 500;
    public static final int panDuration = 10000;
    public static final int ITEM_SELECTED_DELAY = 100;

    public static String[] vevents;
    public static String ical;
    public static Calendar firstCalendarDay, lastCalendarDay;
    public static long firstCalendarDayMS, lastCalendarDayMS;
    public static HashMap<String, ArrayList<EventObject>> eventfulDays;
    public static ArrayList<String> announcements;
    public static String announcementDateString, announcementDayTypeString;
    public static byte[] menuBytes;
    public static SparseIntArray contentIdToNavId, contentIdToPos, navIdToRedIcon;
    public static SparseArray<Class> navIdToClass;
    public static GoogleSignInAccount googleAccount;

    public static final SimpleDateFormat viewableDate = new SimpleDateFormat("EE MMMM d, yyyy", Locale.US);
    public static final SimpleDateFormat veventDateTime = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
    public static final SimpleDateFormat veventDate = new SimpleDateFormat("yyyyMMdd", Locale.US);

    private TextView loadingText;
    private CircularProgressBar progressBar;
    private SignInButton signInButton;
    private GoogleSignInClient signInClient;

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

        loadingText = findViewById(R.id.loading_text);
        progressBar = findViewById(R.id.progressBar);
        signInButton = findViewById(R.id.signInButton);
        signInButton.setVisibility(View.INVISIBLE);
        for (int c=0; c<signInButton.getChildCount(); c++){
            View v = signInButton.getChildAt(c);
            if (v instanceof TextView)
                ((TextView)v).setTextSize(16);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.server_client_id))
                .requestProfile()
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
//        signInClient.signOut();

        tryToConnect();
    }

    public void tryToConnect(){
        if (FLHSActivity.isOnline(this)) {
            // Start loading the events if connected
            new LoaderThread(this, new WeakReference<Context>(this), new WeakReference<>(loadingText)).execute();
        } else {
            // Show the connection error dialog if not connected
            ConnectionErrorFragment connectionError = new ConnectionErrorFragment();
            connectionError.show(getFragmentManager(), "Connection Error");
        }
    }

    public void finished(){
        AccountInfo.signIn(this, googleAccount.getEmail(), googleAccount.getDisplayName(), 0, googleAccount.getPhotoUrl());
        Intent goHome = new Intent(InitialLoader.this, HomeActivity.class);
        goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goHome);
    }

    @Override
    public void onFinish(LoaderThread.ResultBundle resultBundle) {
        if (resultBundle == LoaderThread.FAIL) {
            tryToConnect();
            return;
        }

        assign(resultBundle);

        if (InitialLoader.googleAccount == null) {
            progressBar.setVisibility(View.INVISIBLE);
            loadingText.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signInWithGoogle();
                }
            });
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finished();
                }
            }, finishDelay);
        }
    }

    @Override
    public void signInWithGoogle(){
        if (!FLHSActivity.isOnline(this)){
            BadSignInFragment badSignInFragment = new BadSignInFragment();
            Bundle args = new Bundle();
            args.putInt("cause", BadSignInFragment.BAD_GOOGLE_SIGN_IN);
            badSignInFragment.setArguments(args);
            badSignInFragment.show(getFragmentManager(), "BadSignIn");
        } else {
//            googleClient.clearDefaultAccountAndReconnect();
            Intent startSignIn = signInClient.getSignInIntent();
            startActivityForResult(startSignIn, 1738 /* Ay, I'm like hey whats up hello */);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1738 && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            receivedSignIn(task);
        }
    }

    public void receivedSignIn(Task<GoogleSignInAccount> recieved){
        try {
            InitialLoader.googleAccount = recieved.getResult(ApiException.class);
            finished();
        } catch (ApiException e){
            e.printStackTrace();
            BadSignInFragment badSignIn = new BadSignInFragment();
            Bundle args = new Bundle();
            args.putInt("cause", e.getStatusCode());
            badSignIn.setArguments(args);
            badSignIn.show(getFragmentManager(), "BadSignIn");
        }
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
            }, new WeakReference<Context>(activity), null).execute();
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
        onBackPressed();
    }

    public static void assign(LoaderThread.ResultBundle resultBundle){
        InitialLoader.ical = resultBundle.ical;
        InitialLoader.vevents = resultBundle.vevents;

        InitialLoader.firstCalendarDay = resultBundle.firstCalendarDay;
        InitialLoader.lastCalendarDay = resultBundle.lastCalendarDay;
        InitialLoader.firstCalendarDayMS = resultBundle.firstCalendarDayMS;
        InitialLoader.lastCalendarDayMS = resultBundle.lastCalendarDayMS;

        InitialLoader.eventfulDays = resultBundle.eventfulDays;

        InitialLoader.announcements = resultBundle.announcements;
        InitialLoader.announcementDateString = resultBundle.announcementDateString;
        InitialLoader.announcementDayTypeString = resultBundle.announcementDayTypeString;

        InitialLoader.menuBytes = resultBundle.menuBytes;

        InitialLoader.contentIdToNavId = resultBundle.contentIdToNavId;
        InitialLoader.contentIdToPos = resultBundle.contentIdToPos;
        InitialLoader.navIdToRedIcon = resultBundle.navIdToRedIcon;
        InitialLoader.navIdToClass = resultBundle.navIdToClass;

        InitialLoader.googleAccount = resultBundle.googleAccount;
    }
}
