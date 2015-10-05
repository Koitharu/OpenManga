package org.nv95.openmanga;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.nv95.openmanga.providers.MangaPage;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class PagerReaderAdapter extends PagerAdapter {
    private LayoutInflater inflater;
    private ArrayList<MangaPage> pages;

    public PagerReaderAdapter(Context context, ArrayList<MangaPage> mangaPages) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pages = mangaPages;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.item_page, null);
        MangaPage page = pages.get(position);
        if (page.getLoadTask() == null) {
            PageLoadTask task = new PageLoadTask(inflater.getContext(), (SubsamplingScaleImageView) view.findViewById(R.id.ssiv),
                    (ProgressBar) view.findViewById(R.id.progressBar), (TextView) view.findViewById(R.id.textView_progress), pages.get(position));
            page.setLoadTask(task);
            task.execute();
        }
        ((ViewPager) container).addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        PageLoadTask task = pages.get(position).getLoadTask();
        if (task != null)
            task.cancel(true);
        ((ViewPager) container).removeView((View) object);
    }
}
