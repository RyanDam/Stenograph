package com.rstudio.notii_pro.anim;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.rstudio.notii_pro.R;
import com.rstudio.notii_pro.adapters.MainAdapter;

/**
 * Created by Ryan on 11/27/15.
 */
public class ShowUpCard extends RecyclerView.ItemAnimator {

    private Animation anim;
    private Context mContext;

    public ShowUpCard(Context ctx) {
        mContext = ctx;
        anim = AnimationUtils.loadAnimation(mContext, R.anim.card_show_up);
    }

    @Override
    public boolean animateDisappearance(RecyclerView.ViewHolder viewHolder, ItemHolderInfo preLayoutInfo, ItemHolderInfo postLayoutInfo) {
        return false;
    }

    @Override
    public boolean animateAppearance(RecyclerView.ViewHolder viewHolder, ItemHolderInfo preLayoutInfo, ItemHolderInfo postLayoutInfo) {
        ((MainAdapter.ViewHolder) viewHolder).mView.startAnimation(anim);
        return true;
    }

    @Override
    public boolean animatePersistence(RecyclerView.ViewHolder viewHolder, ItemHolderInfo preLayoutInfo, ItemHolderInfo postLayoutInfo) {
        return false;
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, ItemHolderInfo preLayoutInfo, ItemHolderInfo postLayoutInfo) {
        return false;
    }

    @Override
    public void runPendingAnimations() {

    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder item) {

    }

    @Override
    public void endAnimations() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
