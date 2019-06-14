package org.nv95.openmanga.feature.read.reader;

import android.content.Context;

import org.nv95.openmanga.feature.read.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.InternalLinkMovement;

import java.util.List;

/**
 * Created by admin on 28.07.17.
 */

public interface MangaReader {

    void applyConfig(boolean vertical, boolean reverse, boolean sticky, boolean showNumbers);
    boolean scrollToNext(boolean animate);
    boolean scrollToPrevious(boolean animate);
    int getCurrentPosition();
    void scrollToPosition(int position);
    void setTapNavs(boolean val);

    void addOnPageChangedListener(RecyclerViewPager.OnPageChangedListener listener);

    void setOnOverScrollListener(OnOverScrollListener listener);

    boolean isReversed();

    int getItemCount();

    void initAdapter(Context context, InternalLinkMovement.OnLinkClickListener linkListener);

    PageLoader getLoader();

    void notifyDataSetChanged();

    PageWrapper getItem(int position);

    void setScaleMode(int scaleMode);

    void reload(int position);

    void setPages(List<MangaPage> mangaPages);

    void finish();

    List<MangaPage> getPages();
}
