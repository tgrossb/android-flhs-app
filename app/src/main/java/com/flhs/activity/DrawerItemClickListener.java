package com.flhs.activity;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.flhs.AnnouncementActivity;
import com.flhs.CalendarActivity;
import com.flhs.LunchMenuActivity;
import com.flhs.ScheduleActivity;
import com.flhs.TempSportsActivity;

class DrawerItemClickListener implements AdapterView.OnItemClickListener {
    private FLHSActivity activity;

    public DrawerItemClickListener(FLHSActivity activity){
        this.activity = activity;
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        switch (position) {
            case 0:
                Intent announcementsActivityExecute = new Intent(activity.getApplicationContext(), AnnouncementActivity.class);
                activity.startActivity(announcementsActivityExecute);
                break;
            case 1:
                Intent calendarActivityExecute = new Intent(activity.getApplicationContext(), CalendarActivity.class);
                activity.startActivity(calendarActivityExecute);
                break;
            case 2:
                Intent LunchMenuActivityExecute = new Intent(activity.getApplicationContext(), LunchMenuActivity.class);
                activity.startActivity(LunchMenuActivityExecute);
                break;
            case 3:
                Intent SportsNavigationActivityExecute = new Intent(activity.getApplicationContext(), TempSportsActivity.class);
                activity.startActivity(SportsNavigationActivityExecute);
                break;
            case 4:
                Intent schedulesActivityExecute = new Intent(activity.getApplicationContext(), ScheduleActivity.class);
                activity.startActivity(schedulesActivityExecute);
                break;
            default:
                Toast.makeText(activity.getApplicationContext(), "I don't know what happened....", Toast.LENGTH_LONG).show();
        }
    }
}