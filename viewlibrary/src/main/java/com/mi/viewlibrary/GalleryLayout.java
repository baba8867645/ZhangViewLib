package com.mi.viewlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现自动轮播
 *
 * @author ZhangTongYu
 */
public class GalleryLayout extends RelativeLayout {

    private List<View> mViewList = new ArrayList<>();

    public GalleryLayout(Context context) {
        this(context, null);
    }

    public GalleryLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public GalleryLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        ViewPager viewPager = new ViewPager(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(viewPager, layoutParams);
    }

    @Override
    public void addView(View child) {
        mViewList.add(child);
    }

    private class PageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return false;
        }
    }
}
