/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package android.support.v17.leanback.widget;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementListener;
import android.support.v4.view.ViewCompat;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter.ViewHolder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;

import java.util.List;

final class DetailsOverviewSharedElementHelper extends SharedElementListener {

    private ViewHolder mViewHolder;
    private Activity mActivityToRunTransition;
    private String mSharedElementName;
    private int mRightPanelWidth;
    private int mRightPanelHeight;

    @Override
    public void setSharedElementStart(List<String> sharedElementNames,
            List<View> sharedElements, List<View> sharedElementSnapshots) {
        if (sharedElements.size() < 1) {
            return;
        }
        View overviewView = sharedElements.get(0);
        if (mViewHolder == null || mViewHolder.mOverviewView != overviewView) {
            return;
        }
        View imageView = mViewHolder.mImageView;
        final int width = overviewView.getWidth();
        final int height = overviewView.getHeight();
        imageView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        imageView.layout(0, 0, width, height);
        final View rightPanel = mViewHolder.mRightPanel;
        if (mRightPanelWidth != 0 && mRightPanelHeight != 0) {
            rightPanel.measure(MeasureSpec.makeMeasureSpec(mRightPanelWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mRightPanelHeight, MeasureSpec.EXACTLY));
            rightPanel.layout(width, rightPanel.getTop(), width + mRightPanelWidth,
                    rightPanel.getTop() + mRightPanelHeight);
        } else {
            rightPanel.offsetLeftAndRight(width - rightPanel.getLeft());
        }
        mViewHolder.mActionsRow.setVisibility(View.INVISIBLE);
        mViewHolder.mDetailsDescriptionFrame.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setSharedElementEnd(List<String> sharedElementNames,
            List<View> sharedElements, List<View> sharedElementSnapshots) {
        if (sharedElements.size() < 1) {
            return;
        }
        View overviewView = sharedElements.get(0);
        if (mViewHolder == null || mViewHolder.mOverviewView != overviewView) {
            return;
        }
        mViewHolder.mActionsRow.setVisibility(View.VISIBLE);
        mViewHolder.mDetailsDescriptionFrame.setVisibility(View.VISIBLE);
    }

    void setSharedElementEnterTransition(Activity activity, String sharedElementName) {
        if (activity == null && !TextUtils.isEmpty(sharedElementName) ||
                activity != null && TextUtils.isEmpty(sharedElementName)) {
            throw new IllegalArgumentException();
        }
        if (activity == mActivityToRunTransition &&
                TextUtils.equals(sharedElementName, mSharedElementName)) {
            return;
        }
        if (mActivityToRunTransition != null) {
            ActivityCompat.setEnterSharedElementListener(mActivityToRunTransition, null);
        }
        mActivityToRunTransition = activity;
        mSharedElementName = sharedElementName;
        if (mActivityToRunTransition != null) {
            ActivityCompat.setEnterSharedElementListener(mActivityToRunTransition, this);
            ActivityCompat.postponeEnterTransition(mActivityToRunTransition);
        }
    }

    void onBindToDrawable(ViewHolder vh) {
        // After we got a image drawable,  we can determine size of right panel.
        // We want right panel to have fixed size so that the right panel don't change size
        // when the overview is layout as a small bounds in transition.
        mViewHolder = vh;
        mViewHolder.mRightPanel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mViewHolder.mRightPanel.removeOnLayoutChangeListener(this);
                mRightPanelWidth = mViewHolder.mRightPanel.getWidth();
                mRightPanelHeight = mViewHolder.mRightPanel.getHeight();
            }
        });
        if (mActivityToRunTransition != null) {
            mViewHolder.mRightPanel.postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    if (mActivityToRunTransition == null) {
                        return;
                    }
                    ViewCompat.setTransitionName(mViewHolder.mOverviewView, mSharedElementName);
                    ActivityCompat.startPostponedEnterTransition(mActivityToRunTransition);
                    mActivityToRunTransition = null;
                    mSharedElementName = null;
                }
            });
        }
    }
}
