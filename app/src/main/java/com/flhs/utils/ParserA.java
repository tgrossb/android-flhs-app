/*
 * Author:Drew Gregory,
 * for: parseEventsToday(), printSportsToday(), parseSchedules() 
 */
package com.flhs.utils;


import android.util.EventLog;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParserA {
    final static int MIDDLE_LUNCH = 1;
    final static int LATE_LUNCH = 2;
    public static final String BEGIN = "BEGIN:VEVENT", END = "END:VEVENT", DTSTART = "DTSTART:",
                            DTEND = "DTEND:", DURATION = "DURATION:", SUMMARY = "SUMMARY:";
    public static final SimpleDateFormat veventForm = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US);
    public static final SimpleDateFormat veventFormNoTime = new SimpleDateFormat("yyyyMMdd", Locale.US);
    public static final String DISTRICT_CALENDAR = "https://www.bcsdny.org/site/handlers/icalfeed.ashx?MIID=2047";
    public static final String FLHS_CALENDAR = "https://www.bcsdny.org/site/handlers/icalfeed.ashx?MIID=2048";

    public static ArrayList<EventObject> parseEventsToday(){
        // Use the district calendar by default
        String icalAdress = ParserA.DISTRICT_CALENDAR;
        String ical = readCalendar(icalAdress);
        Date today = new Date();
        // Uncomment to force a day with 3 events for checking
//        Calendar c = Calendar.getInstance();
//        c.set(2018, 3, 3);
//        today = c.getTime();
        return parseEventsToday(today, ical);
    }

    public static ArrayList<EventObject> parseEventsToday(Date today, String ical) {
        if (ical == null)
            return null;

        // Split the ical into vevents
        String[] vevents = ical.split(END + "\n" + BEGIN);
        int firstStart = vevents[0].indexOf(BEGIN);
        vevents[0] = vevents[0].substring(firstStart + 12);
        int lastEnd = vevents[vevents.length-1].indexOf(END);
        vevents[vevents.length-1] = vevents[vevents.length-1].substring(0, lastEnd);

        HashMap<String, ArrayList<EventObject>> eventfulDays = getEventfulDays(vevents);
        String todayFormed = veventFormNoTime.format(today);
        if (eventfulDays.containsKey(todayFormed))
            return eventfulDays.get(todayFormed);
        return new ArrayList<>();
    }

    // String dates are dates formatted with veventFormNoTime
    public static HashMap<String, ArrayList<EventObject>> getEventfulDays(String[] vevents){
        HashMap<String, ArrayList<EventObject>> eventfulDays = new HashMap<>();
        for (String vevent : vevents){
            String dtstart = getValue(vevent, DTSTART);

            Date startDate = fromVeventDate(dtstart);
            Date endDate = getEventEndDate(vevent, startDate);

            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            String description = getValue(vevent, SUMMARY);
            String time = getTime(vevent);
            while (startCal.before(endCal)){
                Date betweenUnformed = startCal.getTime();
                String between = veventFormNoTime.format(betweenUnformed);
                EventObject event = new EventObject(time, description);
                if (eventfulDays.containsKey(between))
                    eventfulDays.get(between).add(event);
                else {
                    ArrayList<EventObject> events = new ArrayList<>(1);
                    events.add(event);
                    eventfulDays.put(between, events);
                }
                startCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        return eventfulDays;
    }

    public static String getTime(String vevent){
        String dateTimeStart = getValue(vevent, DTSTART);
        if (dateTimeStart == null)
            return "Never starts";

        // Time end is not present if the event is all day or multiple days
        String time;
        String dateTimeEnd = getValue(vevent, DTEND);

        Date startDateTime = fromVeventDate(dateTimeStart);
        Date endDateTime = fromVeventDate(dateTimeEnd);

        if (dateTimeEnd != null){
            String start = UTCToNY(startDateTime);
            String end = UTCToNY(endDateTime);
            if (end.equals("11:59 PM"))
                time = "Starts at " + start;
            else
                time = start + " - " + end;
        } else {
            time = "All Day";
        }
        return time;
    }

    //TODO: Change everything to Instant everywhere... for real...  i know its a lot, but do it
    public static String UTCToNY(Date utcInNy){
        SimpleDateFormat dateToInstant = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.00'Z'", Locale.US);
        DateTimeFormatter toNY = DateTimeFormatter.ofPattern("h:mm a");
        String ny = Instant.parse(dateToInstant.format(utcInNy)).atZone(ZoneId.of("America/New_York")).format(toNY);

        if (ny.equals("12:00 AM"))
            ny = "Midnight";
        else if (ny.equals("12:00 PM"))
            ny = "Noon";
        return ny;
    }

    public static Date getEventEndDate(String vevent, Date start){
        String dtend = getValue(vevent, DTEND);
        if (dtend != null)
            return fromVeventDate(dtend);
        String durationPeriod = getValue(vevent, DURATION);

        if (durationPeriod == null)
            return start;
        int duration = Integer.parseInt(durationPeriod.substring(1, durationPeriod.length() - 1));
        String period = durationPeriod.substring(durationPeriod.length() - 1);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(start);
        if (period.equals("D"))
            endCal.add(Calendar.DATE, duration);
        else if (period.equals("M"))
            endCal.add(Calendar.MONTH, duration);
        else if (period.equals("Y"))
            endCal.add(Calendar.YEAR, duration);
        Date end = endCal.getTime();
        return end;
    }

    public static String getValue(String big, String key){
        int keyStart = big.indexOf(key);
        if (keyStart < 0)
            return null;
        int keyEnd = keyStart + key.length();
        int nextNewline = big.indexOf("\n", keyEnd);
        if (nextNewline < 0)
            nextNewline = big.length();
        return big.substring(keyEnd, nextNewline);
    }

    public static Date fromVeventDate(String dateString){
        Date date = null;
        try {
            date = veventForm.parse(dateString);
        } catch (ParseException e) {
            if (!dateString.matches("[0-9]+")){
                Log.i("Parse warning", "Error parsing " + dateString + " with time");
                e.printStackTrace();
            }
        } catch (NullPointerException e){}
        if (date == null) {
            try {
                date = veventFormNoTime.parse(dateString);
            } catch (ParseException e) {
                Log.e("Parse error", "Error parsing " + dateString + " without time");
                e.printStackTrace();
            } catch (NullPointerException e){}
        }
        return date;
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
