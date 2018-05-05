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
import com.flhs.preloader.InitialLoader;

public class FLHSActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private final Handler drawerActionHandler = new Handler();

    public boolean isOnline() {
        return isOnline(this);
    }

    public static boolean isOnline(Context context) {
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
        navigationView.getMenu().setGroupCheckable(R.id.itemGroup, true, true);
        int navId = InitialLoader.contentIdToNavId.get(contentId, -1);
        int pos = InitialLoader.contentIdToPos.get(contentId, -1);
        if (pos > -1 && navId > -1) {
//            MenuItem item = navigationView.getMenu().getItem(pos);
//            item.setIcon(InitialLoader.navIdToRedIcon.get(navId));
//            item.setChecked(true);
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

    public void navigate(int itemId) {
        Intent switchIntent = new Intent(getApplicationContext(), InitialLoader.navIdToClass.get(itemId, HomeActivity.class));
        startActivity(switchIntent);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        Toast.makeText(this, "Navigation item selected", Toast.LENGTH_SHORT).show();
        //        item.setIcon(InitialLoader.navIdToRedIcon.get(item.getItemId()));
        item.setChecked(false);
        ((NavigationView)findViewById(R.id.nav_drawer)).setCheckedItem(R.id.dummyItem);

        drawerLayout.closeDrawers();
        drawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(item.getItemId());
            }
        }, InitialLoader.ITEM_SELECTED_DELAY); // Delay in ms
        return true;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void clearMenuIcons() {
//        Menu navigationMenu = navigationView.getMenu();
//        System.out.println("Menu size: " + navigationMenu.size());
//        for (int c = 0; c < navigationMenu.size(); c++) {
//            MenuItem item = navigationMenu.getItem(c);
//            item.setIcon(InitialLoader.navIdToBlackIcon.get(item.getItemId()));
//            item.setChecked(false);
//        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back", Toast.LENGTH_SHORT).show();
//        clearMenuIcons();
//        navigationView.getMenu().getItem(0).setChecked(true);
        super.onBackPressed();
    }
}