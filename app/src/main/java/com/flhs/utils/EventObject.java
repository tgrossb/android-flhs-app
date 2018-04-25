package com.flhs.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class EventObject implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public EventObject createFromParcel(Parcel in){
            return new EventObject(in);
        }

        public EventObject[] newArray(int size){
            return new EventObject[size];
        }
    };
    private String time;
    private String eventDesc;

    public EventObject(Parcel in){
        time = in.readString();
        eventDesc = in.readString();
    }

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
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel in, int flags){
        in.writeString(time);
        in.writeString(eventDesc);
    }

    @Override
    public String toString(){
        return eventDesc + "\n" + time;
    }

    @Override
    public int hashCode(){
        return toString().hashCode();
    }
}
