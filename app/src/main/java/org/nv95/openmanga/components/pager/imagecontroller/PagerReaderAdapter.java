package org.nv95.openmanga.components.pager.imagecontroller;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaPage;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class PagerReaderAdapter extends PagerAdapter {

    private final LayoutInflater inflater;
    private final ArrayList<MangaPage> pages;
    private boolean isLandOrientation, isLight;
    private int mScaleMode = PageHolder.SCALE_FIT;
    private boolean mFreezed;

    public PagerReaderAdapter(Context context, ArrayList<MangaPage> mangaPages) {
        inflater = LayoutInflater.from(context);
        pages = mangaPages;
        isLight = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("theme", "0").equals("0");
        mFreezed = false;
    }

    public void setIsLandOrientation(boolean isLandOrientation) {
        this.isLandOrientation = isLandOrientation;
        notifyDataSetChanged();
    }

    public void freeze() {
        mFreezed = true;
    }

    public void unfreeze() {
        if (mFreezed) {
            mFreezed = false;
            notifyDataSetChanged();
        }
    }

    public boolean isFreezed() {
        return mFreezed;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public int getItemPosition(Object object){
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object instanceof PageHolder && view.equals(((PageHolder) object).itemView);
    }

    public void setScaleMode(int scaleMode) {
        mScaleMode = scaleMode;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PageHolder holder = new PageHolder(inflater.inflate(R.layout.item_page, container, false));
        if (isLight) {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
        if (!isFreezed()) {
            holder.loadPage(getItem(position), isLandOrientation, mScaleMode);
        }
        container.addView(holder.itemView, 0);
        return holder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof PageHolder) {
            ((PageHolder) object).recycle();
            container.removeView(((PageHolder) object).itemView);
        }
    }

    public MangaPage getItem(int position) {
        return pages.get(position);
    }
}
