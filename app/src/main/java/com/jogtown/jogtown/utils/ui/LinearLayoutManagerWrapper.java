package com.jogtown.jogtown.utils.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class LinearLayoutManagerWrapper extends LinearLayoutManager {
    /*
    The reason for creating this is to override supportsPredictiveItemAnimations()
    inorder to use notifydatasetchanged() in recycler view adapter and still preserve
    it's fancy animations.
    Using notifyItemInserted() though preserved animations but it messed up the ordering a bit
     */

    public LinearLayoutManagerWrapper(Context context) {
        super(context);

    }

    public LinearLayoutManagerWrapper(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public LinearLayoutManagerWrapper(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }


}
