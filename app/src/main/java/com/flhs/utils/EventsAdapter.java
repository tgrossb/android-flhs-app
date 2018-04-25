package com.flhs.utils;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flhs.R;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private ArrayList<EventObject> events;
    private int backgroundColor;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView eventView;

        ViewHolder(CardView eventView, int backgroundColor){
            super(eventView);
            this.eventView = eventView;
            this.eventView.setCardBackgroundColor(backgroundColor);
        }

        public void setText(EventObject event){
            TextView descritionView = eventView.findViewById(R.id.description);
            TextView timeView = eventView.findViewById(R.id.time);
            descritionView.setText(event.getDescription());
            timeView.setText(event.getTime());
        }
    }

    public EventsAdapter(ArrayList<EventObject> events, int backgroundColor) {
        this.events = events;
        this.backgroundColor = backgroundColor;
    }

    public static int color(Context c, int id){
        return c.getResources().getColor(id);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CardView event = (CardView) inflater.inflate(R.layout.events_list_item, parent, false);
        return new ViewHolder(event, backgroundColor);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setText(events.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return events.size();
    }

    public void removeAllItems(){
        final int size = events.size();
        events.clear();
        notifyItemRangeChanged(0, size);
    }
}
