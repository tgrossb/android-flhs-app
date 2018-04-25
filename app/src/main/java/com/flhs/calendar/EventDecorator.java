package com.flhs.calendar;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.LineBackgroundSpan;

import com.flhs.utils.ParserA;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.io.Serializable;
import java.util.ArrayList;

import com.flhs.utils.EventObject;

public class EventDecorator implements Parcelable, DayViewDecorator {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public EventDecorator createFromParcel(Parcel in){
            return new EventDecorator(in);
        }

        public EventDecorator[] newArray(int size){
            return new EventDecorator[size];
        }
    };

    private CalendarDay decDay;
    private int eventsCount;

    public EventDecorator(Parcel in){
        decDay = in.readParcelable(CalendarDay.class.getClassLoader());
        eventsCount = in.readInt();
    }

    public EventDecorator(CalendarDay decDay, int eventsCount) {
        this.decDay = decDay;
        this.eventsCount = eventsCount;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(decDay);
    }

    public void decorate(DayViewFacade view) {
        view.addSpan(new MultiDotSpan(5f, eventsCount));
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeParcelable(decDay, flags);
        dest.writeInt(eventsCount);
    }

    class MultiDotSpan implements LineBackgroundSpan {
        private float radius;
        private int count;

        public MultiDotSpan(float radius, int count) {
            this.radius = radius;
            this.count = count;
        }

        @Override
        public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top,
                                   int baseline, int bottom, CharSequence charSequence, int start, int end, int lineNum) {
            float padding = radius * 2;
            int eachWidth = (int) (radius * 2 + padding);
            int totalWidth = eachWidth * count;
            int center = (left + right) / 2 - totalWidth / 2 + eachWidth / 2;
            for (int drawn = 0; drawn < count; center += eachWidth, drawn++)
                canvas.drawCircle(center, bottom + radius, radius, paint);
        }
    }
}