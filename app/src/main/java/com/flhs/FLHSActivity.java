package com.flhs;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.flhs.announcements.AnnouncementActivity;
import com.flhs.calendar.CalendarActivity;
import com.flhs.home.HomeActivity;

public class FLHSActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private final Handler drawerActionHandler = new Handler();
    public static String[] vevents;

    public boolean isOnline() {
        return isOnline(this);
    }

    public static boolean isOnline(Context context){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        try {
            networkInfo = connMgr.getActiveNetworkInfo();
        } catch (NullPointerException e) {
//            Toast.makeText(this, "Null pointer from isOnline", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return (networkInfo != null && networkInfo.isConnected());
    }

    protected void onCreate(Bundle savedInstances, int contentId) {
        super.onCreate(savedInstances);
        setContentView(contentId);

        buildContentIdToNavId();
        buildContentIdToPos();
        buildNavIdToClass();
        buildNavIdToBlackIcon();
        buildNavIdToRedIcon();

        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar bar = findViewById(R.id.toolbar);
        bar.setLogo(null);
        bar.setContentInsetStartWithNavigation(0);
        setSupportActionBar(bar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

        NavigationView navigationView = findViewById(R.id.nav_drawer);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        int navId = contentIdToNavId.get(contentId, -1);
        int pos = contentIdToPos.get(contentId, -1);
        if (pos > -1 && navId > -1) {
            MenuItem item = navigationView.getMenu().getItem(pos);
            item.setIcon(navIdToRedIcon.get(navId));
            item.setChecked(true);
        }

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, bar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.home_icon:
                Intent homeActivityIntent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(homeActivityIntent);
                return true;
            case R.id.info_icon:
                Intent aboutActivityIntent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(aboutActivityIntent);
                return true;
            default:
                Toast.makeText(getApplicationContext(), "You clicked somewhere else????", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void navigate(int itemId){
        Intent switchIntent = new Intent(getApplicationContext(), navIdToClass.get(itemId, HomeActivity.class));
        startActivity(switchIntent);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item){
        item.setIcon(navIdToRedIcon.get(item.getItemId()));
        item.setChecked(true);

        drawerLayout.closeDrawers();
        drawerActionHandler.postDelayed(new Runnable(){
            @Override
            public void run(){
                navigate(item.getItemId());
            }
        }, 300); // Delay in ms
        return true;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public static void buildContentIdToNavId(){
        if (contentIdToNavId != null)
            return;
        contentIdToNavId = new SparseIntArray(6);
        contentIdToNavId.put(R.layout.activity_announcement, R.id.nav_announcements);
        contentIdToNavId.put(R.layout.activity_calendar, R.id.nav_calendar);
        contentIdToNavId.put(R.layout.activity_lunch_menu, R.id.nav_lunch_menu);
        contentIdToNavId.put(R.layout.activity_sports_navigation, R.id.nav_sports);
        contentIdToNavId.put(R.layout.temp_sports, R.id.nav_sports);
        contentIdToNavId.put(R.layout.activity_schedule, R.id.nav_bell_schedule);
    }

    public static void buildContentIdToPos(){
        if (contentIdToPos != null)
            return;
        contentIdToPos = new SparseIntArray(6);
        contentIdToPos.put(R.layout.activity_announcement, 0);
        contentIdToPos.put(R.layout.activity_calendar, 1);
        contentIdToPos.put(R.layout.activity_lunch_menu, 2);
        contentIdToPos.put(R.layout.activity_sports_navigation, 3);
        contentIdToPos.put(R.layout.temp_sports, 3);
        contentIdToPos.put(R.layout.activity_schedule, 4);
    }

    public static void buildNavIdToClass() {
        if (navIdToClass != null)
            return;
        navIdToClass = new SparseArray<>(5);
        navIdToClass.put(R.id.nav_announcements, AnnouncementActivity.class);
        navIdToClass.put(R.id.nav_bell_schedule, ScheduleActivity.class);
        navIdToClass.put(R.id.nav_calendar, CalendarActivity.class);
        navIdToClass.put(R.id.nav_lunch_menu, LunchMenuActivity.class);
        navIdToClass.put(R.id.nav_sports, TempSportsActivity.class);
    }

    public static void buildNavIdToBlackIcon(){
        if (navIdToBlackIcon != null)
            return;
        navIdToBlackIcon = new SparseIntArray(5);
        navIdToBlackIcon.put(R.id.nav_announcements, R.drawable.announcements_icon_black);
        navIdToBlackIcon.put(R.id.nav_calendar, R.drawable.calendar_icon_black);
        navIdToBlackIcon.put(R.id.nav_lunch_menu, R.drawable.lunch_menu_black);
        navIdToBlackIcon.put(R.id.nav_sports, R.drawable.sports_icon_black);
        navIdToBlackIcon.put(R.id.nav_bell_schedule, R.drawable.schedule_black);
    }

    public static void buildNavIdToRedIcon(){
        if (navIdToRedIcon != null)
            return;
        navIdToRedIcon = new SparseIntArray(5);
        navIdToRedIcon.put(R.id.nav_announcements, R.drawable.announcements_icon_red);
        navIdToRedIcon.put(R.id.nav_calendar, R.drawable.calendar_icon_red);
        navIdToRedIcon.put(R.id.nav_lunch_menu, R.drawable.lunch_menu_red);
        navIdToRedIcon.put(R.id.nav_sports, R.drawable.sports_icon_red);
        navIdToRedIcon.put(R.id.nav_bell_schedule, R.drawable.schedule_red);
    }

    public static SparseIntArray contentIdToNavId;
    public static SparseIntArray contentIdToPos;
    public static SparseArray<Class> navIdToClass;
    public static SparseIntArray navIdToBlackIcon;
    public static SparseIntArray navIdToRedIcon;
}