package com.flhs.utils;

public class EventObject {
    private String time;
    private String eventDesc;

    public EventObject(String time, String eventDesc) {
        this.time = time;
        this.eventDesc = eventDesc;
    }

    public String getDescription(){
        return eventDesc;
    }

    public String getTime(){
        if (time.equals("00:00:00"))
            return "All day";
        return time;
    }

    @Override
    public String toString(){
        return eventDesc + "\n" + time;
    }
}	
