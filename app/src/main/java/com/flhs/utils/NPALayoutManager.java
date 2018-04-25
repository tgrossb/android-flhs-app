package com.flhs.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * No Predictive Animations LinearLayoutManager
 */
public class NPALayoutManager extends LinearLayoutManager {
    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    public NPALayoutManager(Context context){
        super(context);
    }
}