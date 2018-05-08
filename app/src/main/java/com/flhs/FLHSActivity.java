package com.flhs;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flhs.announcements.AnnouncementActivity;
import com.flhs.calendar.CalendarActivity;
import com.flhs.home.HomeActivity;
import com.flhs.preloader.InitialLoader;
import com.flhs.utils.AccountInfo;

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
        int navId = InitialLoader.contentIdToNavId.get(contentId, -1);
        int pos = InitialLoader.contentIdToPos.get(contentId, -1);
        if (pos > -1 && navId > -1) {
            MenuItem item = navigationView.getMenu().getItem(pos);
            item.setIcon(InitialLoader.navIdToRedIcon.get(navId));
            item.setChecked(true);
        }
        View view = navigationView.getHeaderView(0);
        ImageView imageSpot = view.findViewById(R.id.nav_header_image);
        imageSpot.setImageBitmap(AccountInfo.getProfilePicture(this));
        TextView nameSpot = view.findViewById(R.id.nav_header_name);
        nameSpot.setText(AccountInfo.getDispName(this));
        TextView emailSpot = view.findViewById(R.id.nav_header_email);
        emailSpot.setText(AccountInfo.getEmail(this));
//        TextViewCompat.setAutoSizeTextTypeWithDefaults(emailSpot, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
//        emailSpot.setMaxWidth(imageSpot.getWidth());
//        view.invalidate();

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
        drawerLayout.closeDrawers();
        drawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(item.getItemId());
            }
        }, InitialLoader.ITEM_SELECTED_DELAY); // Delay in ms
        // Don't display it as selected, we are changing activities anyway
        return false;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}