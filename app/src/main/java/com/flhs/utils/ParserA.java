/*
 * Author:Drew Gregory,
 * for: parseEventsToday(), printSportsToday(), parseSchedules() 
 */
package com.flhs.utils;


import android.util.EventLog;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParserA {
    final static int MIDDLE_LUNCH = 1;
    final static int LATE_LUNCH = 2;

    public static String integerMonthToString(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        try {
            return months[month - 1];
        } catch (IndexOutOfBoundsException e){
            return "Invalid month";
        }
    }

    public static ArrayList<EventObject> parseEventsToday() {
        ArrayList<EventObject> events = new ArrayList<>();
        String ical = readCalendar("https://www.bcsdny.org/site/handlers/icalfeed.ashx?MIID=2047");

        // Split the ical into vevents
        String[] vevents = ical.split("END:VEVENT\nBEGIN:VEVENT");
        int firstStart = vevents[0].indexOf("BEGIN:VEVENT");
        vevents[0] = vevents[0].substring(firstStart + 12);
        int lastEnd = vevents[vevents.length-1].indexOf("END:VEVENT");
        vevents[vevents.length-1] = vevents[vevents.length-1].substring(0, lastEnd);

        System.out.println(getValue(vevents[0], "DTSTART:"));

        // Look for today's date as a DTSTART
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
//        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.set(2018, 1, 19);
        Date today = c.getTime();
        String todayFormed = format.format(today);
        for (String vevent : vevents) {
            boolean match = false;
            if (vevent.contains("DTSTART:" + todayFormed)) {
                match = true;
            } else if (vevent.contains("DURATION:")) {
                String durationPeriod = getValue(vevent, "DURATION:");
                int duration = Integer.parseInt(durationPeriod.substring(1, durationPeriod.length() - 1));
                String period = durationPeriod.substring(durationPeriod.length() - 1);

                String startDate = getValue(vevent, "DTSTART:").substring(0, 8);
                Date start = new Date();
                try {
                    start = format.parse(startDate);
                } catch (ParseException e){
                    e.printStackTrace();
                }
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(start);
                if (period.equals("D"))
                    endCal.add(Calendar.DATE, duration);
                else if (period.equals("M"))
                    endCal.add(Calendar.MONTH, duration);
                else if (period.equals("Y"))
                    endCal.add(Calendar.YEAR, duration);
                Date end = endCal.getTime();
                boolean nmatch = false;
                if (start.compareTo(today) * today.compareTo(end) >= 0)
                    nmatch = true;
                System.out.println("Is " + today.toString() + " between " + start.toString() + " and " + end.toString() + ": " + nmatch);
                if (nmatch)
                    match = true;
            }
            if (match){
                String dateTime = getValue(vevent, "DTSTART:");
                int timeStart = dateTime.indexOf("T");
                int timeStop = dateTime.indexOf("Z");
                String time = "00:00:00";
                if (timeStart != -1 && timeStop != -1) {
                    time = dateTime.substring(timeStart + 1, timeStop);
                    time = Integer.parseInt(time.substring(0, 2)) - 5 + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
                }

                String description = getValue(vevent, "SUMMARY:");
                EventObject event = new EventObject(time, description);
                events.add(event);
            }
        }

        return events;
    }

    public static String getValue(String big, String key){
        int keyEnd = big.indexOf(key) + key.length();
        int nextNewline = big.indexOf("\n", keyEnd);
        return big.substring(keyEnd, nextNewline);
    }

    public static String readCalendar(String adress) {
        try {
            URL url = new URL(adress);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String buffer;
            StringBuffer file = new StringBuffer();
            while ((buffer = reader.readLine()) != null)
                file.append(buffer).append("\n");
            reader.close();
            return file.toString();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<EventObject> printSportsToday() {
        ArrayList<EventObject> sportsEvents;
        String url = "http://sportspak.swboces.org/sportspak/oecgi3.exe/O4W_SPAKONLINE_HOME"; //Day, Month, Year, 0 if I want today's..... Change 13 to 0 after testing....
        try {
            Document SportsFormatToPrintWebpage = Jsoup.connect(url).get();
            Element spanResults = SportsFormatToPrintWebpage.getElementById("DAILY_EVENTS");
            sportsEvents = new ArrayList<>();
            if (spanResults != null) {
                Elements EventRows = spanResults.child(1).children();
                for (int EventRowIndex = 1; EventRowIndex < EventRows.size(); EventRowIndex++) { //I put it at 1 because the first row is just the header...... so I skipped 0....
                    Element EventRow = EventRows.get(EventRowIndex);
                    if (EventRow.child(4).text().contains("FOX LANE HS") || EventRow.child(3).text().contains("FOX LANE HS")) {
                        /*
				 		TR Help:
				 		child(0) Game Time
				 		child(1) Sport
				 		child(2) Level
				 		child(3) Home
				 		child(4) Visitor
				 		child(5) Type
				 		child(6) Facility
						 */

                        String sport = EventRow.child(2).text() + " " + EventRow.child(1).text();
                        String visitingTeam = EventRow.child(4).text();
                        String homeTeam = EventRow.child(3).text();
                        String time = EventRow.child(0).text();
                        SportsEvent game = new SportsEvent(sport, visitingTeam, homeTeam, time);
                        sportsEvents.add(game);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            sportsEvents = null;
        }
        return sportsEvents;
    }

    public static String[] setLunchOrder(String[] Lunch1Schedule, int lunchType) {
        String[] ParsedSchedule = Lunch1Schedule;
        if (Lunch1Schedule.length == 7) {
            String Lunch = Lunch1Schedule[2];
            String FirstClass = Lunch1Schedule[3];
            String SecondClass = Lunch1Schedule[4];
            if (lunchType == MIDDLE_LUNCH) {
                ParsedSchedule[2] = FirstClass;
                ParsedSchedule[3] = Lunch;
                ParsedSchedule[4] = SecondClass;
            }
            if (lunchType == LATE_LUNCH) {
                ParsedSchedule[2] = FirstClass;
                ParsedSchedule[3] = SecondClass;
                ParsedSchedule[4] = Lunch;
            }
        }
        if (Lunch1Schedule.length == 9) {

            String Lunch = Lunch1Schedule[3];
            String FirstClass = Lunch1Schedule[4];
            String SecondClass = Lunch1Schedule[5];
            if (lunchType == MIDDLE_LUNCH) {
                ParsedSchedule[3] = FirstClass;
                ParsedSchedule[4] = Lunch;
                ParsedSchedule[5] = SecondClass;
            }
            if (lunchType == LATE_LUNCH) {
                ParsedSchedule[3] = FirstClass;
                ParsedSchedule[4] = SecondClass;
                ParsedSchedule[5] = Lunch;
            }
        }
        return ParsedSchedule;
    }

    public static String parseNumToDay(int day) {
        String[] days = {"A", "B", "C", "D", "E", "1", "2", "3", "4", "5", "Adv E", "Adv 5", "Collab E", "Collab 5"};
        try {
            return days[day-1];
        } catch (IndexOutOfBoundsException e){
            return "Unknown";
        }
    }
}
