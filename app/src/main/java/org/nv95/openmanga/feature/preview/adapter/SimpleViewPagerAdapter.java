package org.nv95.openmanga.feature.preview.adapter;

import androidx.viewpager.widget.PagerAdapter;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by unravel22 on 18.02.17.
 */

public class SimpleViewPagerAdapter extends PagerAdapter {
    
    private final ArrayList<Pair<View,String>> mViews;
    
    public SimpleViewPagerAdapter() {
        mViews = new ArrayList<>();
    }
    
    public void addView(View view, String title) {
        mViews.add(new Pair<>(view, title));
    }
    
    @Override
    public int getCount() {
        return mViews.size();
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mViews.get(position).first;
        container.addView(view);
        return view;
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        return mViews.get(position).second;
    }
    
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
