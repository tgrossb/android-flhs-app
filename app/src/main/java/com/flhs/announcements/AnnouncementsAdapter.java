package com.flhs.announcements;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flhs.R;
import com.flhs.utils.EventObject;

import java.util.ArrayList;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder> {
    private ArrayList<String> announcements;
    private int backgroundColor;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView announcementView;

        ViewHolder(CardView announcementView, int backgroundColor){
            super(announcementView);
            this.announcementView = announcementView;
            this.announcementView.setCardBackgroundColor(backgroundColor);
        }

        public void setText(String text){
            TextView descritionView = announcementView.findViewById(R.id.description);
            descritionView.setText(text);
            announcementView.findViewById(R.id.time).setVisibility(View.GONE);
        }
    }

    AnnouncementsAdapter(ArrayList<String> announcements, int backgroundColor) {
        this.announcements = announcements;
        this.backgroundColor = backgroundColor;
    }


    // Create new views (invoked by the layout manager)
    @Override
    public AnnouncementsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CardView event = (CardView) inflater.inflate(R.layout.events_list_item, parent, false);
        return new ViewHolder(event, backgroundColor);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setText(announcements.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return announcements.size();
    }

    public void removeAllItems(){
        for (int c=announcements.size()-1; c>=0; c--) {
            announcements.remove(c);
            notifyItemChanged(c);
            notifyItemRangeChanged(c, announcements.size());
        }
    }
}