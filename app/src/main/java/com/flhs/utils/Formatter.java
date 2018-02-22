package com.flhs.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Formatter {
    //Hey android team! I got made this formatter for the sports news thing you guys are working on! Using the url generated from the the Format part of the "Awesome
    //Sports News Website!!!" thing and I made a class with the method that you can use yourselves to set up a url to generate a table that you want for the sports schedules! Cool, eh? :)
    private String calDay;
    private String calMonth;
    private String calYear;

    // Cant fix these yet
    public int day;
    public int month;
    public int year;

    public Formatter() {
        Date today = new Date();

        //Getting Current day, month, year
        day = Integer.parseInt(new SimpleDateFormat("d", Locale.US).format(today));
        month = Integer.parseInt(new SimpleDateFormat("M", Locale.US).format(today));
        year = Integer.parseInt(new SimpleDateFormat("y", Locale.US).format(today));

        calDay = new SimpleDateFormat("dd", Locale.US).format(today);
        calMonth = new SimpleDateFormat("MM", Locale.US).format(today);
        calYear = new SimpleDateFormat("yyyy", Locale.US).format(today);
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
/*
    public String SportsNewsURLFormat(int EndDay, int EndMonth, int EndYear) {
        //0 for any of the arguments is supposed to mean that I want it to be the instance that today would be.....
        if (EndDay == 0) {
            EndDay = day;
        }
        if (EndMonth == 0) {
            EndMonth = month;
        }
        if (EndYear == 0) {
            EndYear = year;
        }
        //Phew! Set all the EndThingies to a legitimate date if it wasn't already inputted that way....
        //Setting all the StartThingies now....
        int StartDay = day;
        int StartMonth = month;
        int StartYear = year;
        String finalurl = "http://sportspakdb.lhric.org/gameschedule.asp?school=FL&sport=&level=&startMonth=" + StartMonth + "&startDay=" + StartDay + "&startYear=" + StartYear + "&endMonth=" + EndMonth + "&endDay=" + EndDay + "&endYear=" + EndYear + "&printthis=1&ShowScores=false&AllVisitors=false&Run=1";
        return finalurl;
    }

    public String CalendarPrintURLFormat() {
        String calendarURL = "http://bcsdny.org/calendar_events.cfm?printpage=1&notop&cat=0&location=0&themonth=" + month + "&theyear=" + year + "&AddSportingEvents=0";
        return calendarURL;

    }
*/
    public static String googleDriveViewerURLFormat(String endstring) { //Using Google Drive Viewer to read files like PDFS.... :) :D 8)
        return "http://docs.google.com/gview?embedded=true&url=" + endstring;
    }

    public String getFLHSCalendarURL() {
        return getFLHSCalendarURL(calDay, calMonth, calYear);
    }

    public String getFLHSCalendarURL(int month, int year){
        return getFLHSCalendarURL("1", "" + month, "" + year);
    }

    public String getFLHSCalendarURL(String day, String month, String year){
/*
        return "https://www.bcsdny.org//site/Default.aspx?PageID=10&EventDateID=2891&CurrentView=month&" +
                "CurrentDate=19&CurrentMonth=1&CurrentYear=2018" +
                "#calendar2047/" + year + "" + month + "" + day + "/month";
*/
        return "https://www.bcsdny.org//site/Default.aspx?PageID=10&EventDateID=2891&CurrentView=month&CurrentDate=19&CurrentMonth=1&CurrentYear=2018#calendar2047/20180220/month";
    }
}