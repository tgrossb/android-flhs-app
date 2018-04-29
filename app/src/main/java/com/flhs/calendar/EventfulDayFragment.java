package com.flhs.calendar;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flhs.R;
import com.flhs.utils.EventObject;
import com.flhs.utils.EventsAdapter;
import com.flhs.utils.NPALayoutManager;
import com.flhs.utils.ParserA;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class EventfulDayFragment extends Fragment {
    public final static String DAY = "d", EVENTS = "e";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.eventful_day_display_fragment, container, false);
        view.findViewById(R.id.swiperefresh).setEnabled(false);

        Bundle args = getArguments();

        ArrayList<EventObject> events;
        try {
            events = args.getParcelableArrayList(EVENTS);
        } catch (NullPointerException e){
            events = new ArrayList<>();
            e.printStackTrace();
        }

        if (events.size() == 0)
            events.add(EventObject.noEvents());

        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new NPALayoutManager(getActivity()));
        recycler.setAdapter(new EventsAdapter(events, EventsAdapter.color(getActivity(), R.color.soft_red)));
        recycler.setClickable(false);

        return view;
    }

    public static EventfulDayFragment newInstance(String today, ArrayList<EventObject> events){
        EventfulDayFragment frag = new EventfulDayFragment();
        Bundle b = new Bundle();
        b.putString(DAY, today);
        b.putParcelableArrayList(EVENTS, events);
        frag.setArguments(b);
        return frag;
    }
}
