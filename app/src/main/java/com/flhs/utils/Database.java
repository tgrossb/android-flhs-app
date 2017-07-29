package com.flhs.utils;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Theo Grossberndt on 7/29/17.
 */

public class Database {
    public static String DATABASE_URL = "https://flhs-info.firebaseio.com";
    public static String LUNCH_MENU_URL_LOCATION = "params/LunchMenuURL";
    public static String SCHEDULE_TYPE_LOCATION = "params/ScheduleType";
    public static String WHAT_DAY_LOCATION = "params/WhatDay";
    public static String SPECIAL_DAY_TIMES_LOCATION = "params/SpecialDayTimes";
    public static String SPECIAL_DAY_COURSES_LOCATION = "params/SpecialDayCourses";
    private FirebaseDatabase database;

    public Database() {
        database = FirebaseDatabase.getInstance(DATABASE_URL);
    }

    private String url;
    public String getLunchMenuURL() {
        new DatabaseReader(LUNCH_MENU_URL_LOCATION, new Action() {
            @Override
            public void act(DataSnapshot dataSnapshot) {
                url = dataSnapshot.getValue(String.class);
            }
        }).read();
        return url;
    }

    private ArrayList<String> days;
    public ArrayList<String> getWhatDay() {
        new DatabaseReader(WHAT_DAY_LOCATION, new Action() {
            @Override
            public void act(DataSnapshot dataSnapshot) {
                days = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren())
                    days.add(child.getValue(String.class));
            }
        }).read();
        return days;
    }

    private String scheduleType;
    public String getScheduleType() {
        new DatabaseReader(SCHEDULE_TYPE_LOCATION, new Action() {
            @Override
            public void act(DataSnapshot dataSnapshot) {
                scheduleType = dataSnapshot.getValue(String.class);
            }
        }).read();
        return scheduleType;
    }

    private String[] timeSchedule;
    public String[] getSpecialDayTimes(){
        new DatabaseReader(SPECIAL_DAY_TIMES_LOCATION, new Action() {
            @Override
            public void act(DataSnapshot dataSnapshot) {
                timeSchedule = new String[(int) dataSnapshot.getChildrenCount()];
                int c = 0;
                for (DataSnapshot child : dataSnapshot.getChildren())
                    timeSchedule[c++] = child.getValue(String.class);
            }
        }).read();
        return timeSchedule;
    }

    private String[] courseSchedule;
    public String[] getSpecialDayCourses(){
        new DatabaseReader(SPECIAL_DAY_COURSES_LOCATION, new Action() {
            @Override
            public void act(DataSnapshot dataSnapshot) {
                courseSchedule = new String[(int) dataSnapshot.getChildrenCount()];
                int c = 0;
                for (DataSnapshot child : dataSnapshot.getChildren())
                    courseSchedule[c++] = child.getValue(String.class);
            }
        }).read();
        return courseSchedule;
    }

    private interface Action {
        void act(DataSnapshot dataSnapshot);
    }

    class DatabaseReader {
        private String location;
        private Action action;

        public DatabaseReader(String location, Action action){
            this.location = location;
            this.action = action;
        }

        public void read() {
            DatabaseReference ref = database.getReference(location);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    action.act(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i("FIREBASE ERROR", "Error reading from: " + location + "\n" + databaseError.getMessage());
                }
            });
        }
    }
}
