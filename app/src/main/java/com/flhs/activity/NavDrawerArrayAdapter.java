package com.flhs.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.flhs.R;

class NavDrawerArrayAdapter extends ArrayAdapter<String> {
    private final String[] values;
    private final FLHSActivity activity;

    NavDrawerArrayAdapter(FLHSActivity activity, String[] values) {
        super(activity.getApplicationContext(), R.layout.nav_drawer_lv_item, values);
        this.activity = activity;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.nav_drawer_lv_item, parent, false);
        TextView textView = rowView.findViewById(R.id.label);
        ImageView imageView = rowView.findViewById(R.id.icon);
        textView.setText(values[position]);
        switch (position) {
            case 0:
                if (activity.getTitle().equals("Announcements")) {
                    imageView.setImageResource(R.drawable.announcements_icon_red);
                    textView.setTextColor(Color.RED);
                } else {
                    imageView.setImageResource(R.drawable.announcements_icon_black);
                }
                break;
            case 1:
                if (activity.getTitle().equals("Calendar")) {
                    textView.setTextColor(Color.RED);
                    imageView.setImageResource(R.drawable.calendar_icon_red);
                } else {
                    imageView.setImageResource(R.drawable.calendar_icon_black);
                }
                break;
            case 2:
                if (activity.getTitle().equals("Lunch Menu")) {
                    imageView.setImageResource(R.drawable.lunch_menu_red);
                    textView.setTextColor(Color.RED);
                } else {
                    imageView.setImageResource(R.drawable.lunch_menu_black);
                }
                break;
            case 3:
                if (activity.getTitle().equals("Sports")) {
                    imageView.setImageResource(R.drawable.sports_icon_red);
                    textView.setTextColor(Color.RED);
                } else {
                    imageView.setImageResource(R.drawable.sports_icon_black);
                }
                break;
            case 4:
                if (activity.getTitle().equals("Bell Schedules")) {
                    imageView.setImageResource(R.drawable.schedule_red);
                    textView.setTextColor(Color.RED);
                } else {
                    imageView.setImageResource(R.drawable.schedule_black);
                }
                break;
            default:
                imageView.setImageResource(R.drawable.ic_launcher);
        }
        return rowView;
    }
}