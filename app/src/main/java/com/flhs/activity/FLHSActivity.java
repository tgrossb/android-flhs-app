package com.flhs.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.flhs.AboutActivity;
import com.flhs.R;
import com.flhs.home.HomeActivity;

public class FLHSActivity extends AppCompatActivity {
    public static String[] activities = {"Announcements", "Calendar", "Lunch Menu", "Sports", "Bell Schedules"};
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence title = getTitle();
    public NavDrawerArrayAdapter navDrawerLvAdapter;
    public ListView navList;

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        try {
            networkInfo = connMgr.getActiveNetworkInfo();
        } catch (NullPointerException e){
            Toast.makeText(this, "Null pointer from isOnline", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.taskbar, menu);
        try {
            getSupportActionBar().show();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.info_icon:
                Intent aboutActivityExecute = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(aboutActivityExecute);
                return true;

            case R.id.home_icon:
                Intent HomeActivityExecute = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(HomeActivityExecute);
                return true;
            default:
                //Until there is something useful on that Activity....
                Toast.makeText(getApplicationContext(), "Settings....", Toast.LENGTH_LONG).show();
                return true;
        }
    }

    public void SetupNavDrawer() {
        title = getTitle();
        navList = findViewById(R.id.left_drawer);
        navDrawerLvAdapter = new NavDrawerArrayAdapter(this, activities);
        navList.setAdapter(navDrawerLvAdapter);
        navList.setOnItemClickListener(new DrawerItemClickListener(this));


        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        //mDrawerToggle = new ActionBarDrawerToggle(
        //        this,                  /* host Activity */
        //        mDrawerLayout,         /* DrawerLayout object */
        //        R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
        //        R.string.drawer_open,  /* "open drawer" description */
        //        R.string.hello_world  /* "close drawer" description */
        //) {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(title);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("FLHS Menu");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
