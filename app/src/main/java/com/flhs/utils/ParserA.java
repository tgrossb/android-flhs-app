/*
 * Author:Drew Gregory,
 * for: parseEventsToday(), printSportsToday(), parseSchedules() 
 */
package com.flhs.utils;


import java.io.IOException;
import java.util.ArrayList;

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

    public static ArrayList<eventobject> parseEventsToday() {
        ArrayList<eventobject> date = new ArrayList<>();
        Document Magic;
        try {
            Magic = Jsoup.connect(new Formatter().getFLHSCalendarURL()).get();
            Elements tableDateBox = Magic.getElementsByAttributeValue("style", "background-color: #ffffcc");
            Elements dateBoxText = tableDateBox.get(0).child(1).child(0).child(1).child(1).children();
            for (int eventcalindex = 0; eventcalindex < dateBoxText.size(); eventcalindex++) {
                Element todayevent = dateBoxText.get(eventcalindex);
                String atags = todayevent.getElementsByTag("a").text();
                String aTagsPlusTime = todayevent.text();
                String Time = "";
                if (!atags.equals("") && !aTagsPlusTime.equals("")) {
                    Time = aTagsPlusTime.substring(atags.length(), aTagsPlusTime.length());
                }
                if (!atags.equals("")) {
                    if (!atags.startsWith("FLMS") && !atags.startsWith("BHES") && !atags.startsWith("PRES") && !atags.startsWith("BVES") && !atags.startsWith("MKES") && !atags.startsWith("WPES")) {
                        eventobject tempEventObject = new eventobject();
                        tempEventObject.setDate(Time);
                        tempEventObject.setEventsDesc(0, atags);
                        date.add(tempEventObject);
                    }
                }
            }
        } catch (IOException e) {
            date = null;
        }

        return date;

    }

    public static ArrayList<SportEvent> printSportsToday() {
        ArrayList<SportEvent> SportsNewsEvents;
        String url = "http://sportspak.swboces.org/sportspak/oecgi3.exe/O4W_SPAKONLINE_HOME"; //Day, Month, Year, 0 if I want today's..... Change 13 to 0 after testing....
        try {
            Document SportsFormatToPrintWebpage = Jsoup.connect(url).get();
            Element spanResults = SportsFormatToPrintWebpage.getElementById("DAILY_EVENTS");
            SportsNewsEvents = new ArrayList<SportEvent>();
            if (spanResults != null) {
                Elements EventRows = spanResults.child(1).children();
                for (int EventRowIndex = 1; EventRowIndex < EventRows.size(); EventRowIndex++) { //I put it at 1 because the first row is just the header...... so I skipped 0....
                    Element EventRow = EventRows.get(EventRowIndex);
                    if (EventRow.child(4).text().contains("FOX LANE HS") || EventRow.child(3).text().contains("FOX LANE HS")) {
                        SportEvent Game = new SportEvent();
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
                        Game.setSport(EventRow.child(2).text() + " " + EventRow.child(1).text());
                        Game.setVisitingTeam(EventRow.child(4).text());
                        Game.setHomeTeam(EventRow.child(3).text());
                        Game.setTime(EventRow.child(0).text());
                        SportsNewsEvents.add(Game);
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            SportsNewsEvents = null;
        }
        return SportsNewsEvents;
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
