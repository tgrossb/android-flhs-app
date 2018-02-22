package com.flhs.utils;

public class EventObject {
    private String time;
    private String eventDesc;

    public EventObject(String time, String eventDesc) {
        this.time = time;
        this.eventDesc = eventDesc;
    }

    @Override
    public String toString(){
        return eventDesc + "\n" + time;
    }
}	
