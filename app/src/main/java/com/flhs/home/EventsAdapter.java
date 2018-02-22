package com.flhs.home;

import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flhs.R;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {
    private ArrayList<String> events;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout event;

        ViewHolder(LinearLayout event){
            super(event);
            this.event = event;
        }

        public void setText(String text){
            TextView innerText = event.findViewById(R.id.eventsListTextView);
            innerText.setText(text);
        }
    }

    EventsAdapter(ArrayList<String> events) {
        this.events = events;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LinearLayout event = (LinearLayout) inflater.inflate(R.layout.events_today_list_view_item, parent, false);
        return new ViewHolder(event);
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

    /*
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolderItem item;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.events_today_list_view_item, parent, false);
                item = new ListViewHolderItem();
                //Too lazy to make different ListViewHolderItem.... courseName will be the TextView we will use :-)
                item.courseName = convertView.findViewById(R.id.eventsListTextView);
                convertView.setTag(item);
            } else {
                item = (ListViewHolderItem) convertView.getTag();
            }

            item.courseName.setText(events.get(position));
            return convertView;
        }
 */
}
