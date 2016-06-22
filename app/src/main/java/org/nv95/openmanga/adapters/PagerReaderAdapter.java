package org.nv95.openmanga.adapters;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.imagecontroller.PageLoadAbs;

import java.util.ArrayList;

/**
 * Created by nv95 on 30.09.15.
 */
public class PagerReaderAdapter extends PagerAdapter implements InternalLinkMovement.OnLinkClickListener {
    private final LayoutInflater inflater;
    private final ArrayList<MangaPage> pages;
    private boolean isLandOrientation;
    private SparseArray<ViewHolder> views = new SparseArray<>();
    private final InternalLinkMovement mLinkMovement;

    public PagerReaderAdapter(Context context, ArrayList<MangaPage> mangaPages) {
        inflater = LayoutInflater.from(context);
        pages = mangaPages;
        mLinkMovement = new InternalLinkMovement(this);
    }

    public void setIsLandOrientation(boolean isLandOrientation) {
        this.isLandOrientation = isLandOrientation;
        notifyDataSetChanged();
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
        View view = inflater.inflate(R.layout.item_page, container, false);
        MangaPage page = getItem(position);
        ViewHolder holder = new ViewHolder();
        holder.position = position;
        holder.isLand = isLandOrientation;
        holder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        holder.ssiv = (SubsamplingScaleImageView) view.findViewById(R.id.ssiv);
//        holder.ssiv.setMinimumScaleType(
//                isLandOrientation ? SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP
//                         : SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
//        );
        holder.ssiv.setScaleAndCenter(isLandOrientation ? holder.ssiv.getMaxScale() -.2f : holder.ssiv.getMinScale(), new PointF(0,0));
        holder.textView = (TextView) view.findViewById(R.id.textView_holder);
        holder.textView.setMovementMethod(mLinkMovement);
        holder.textView.setTag(holder);
//        holder.textView = (TextView) view.findViewById(R.id.textView_progress);

        // Работаю над отображением не трогай
//        String path;
//        try {
//            path = ((MangaProvider) page.provider.newInstance()).getPageImage(page);
//        } catch (Exception e) {
//            path = page.path;
//        }
//        holder.ssiv.setParallelLoadingEnabled(true);
//        ImageLoader.getInstance().loadImage(path, new LoaderImage(holder));
//        holder.loadTask = new PageLoadTask(inflater.getContext(), holder, page);
//        holder.loadTask.executeOnExecutor(executor);
        holder.loadTask = new PageLoad(holder, page);
        holder.loadTask.load();
        views.append(position, holder);
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (!(object instanceof View)) {
            return;
        }
        View view = (View) object;
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder != null) {
            if (holder.loadTask != null) {
                holder.loadTask.cancel();
            }
            holder.ssiv.recycle();
        }
        views.remove(position);
        container.removeView(view);
    }

    public MangaPage getItem(int position) {
        return pages.get(position);
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "retry":
                Object tag = view.getTag();
                if (tag == null || !(tag instanceof ViewHolder)) {
                    return;
                }
                final ViewHolder holder = (ViewHolder) tag;
                MangaPage page = pages.get(holder.position);
                holder.loadTask = new PageLoad(holder, page);
                holder.loadTask.load();
                break;
        }
    }

    private static class ViewHolder {
        ProgressBar progressBar;
        SubsamplingScaleImageView ssiv;
        TextView textView;
        @Nullable
        PageLoad loadTask;
        int position;
        boolean isLand;
    }

    private static class PageLoad extends PageLoadAbs {
        private final ViewHolder viewHolder;

        PageLoad(ViewHolder viewHolder, MangaPage page){
            super(page, viewHolder.ssiv);
            this.viewHolder = viewHolder;
        }

        @Override
        protected void preLoad(){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.progressBar.setIndeterminate(true);
            viewHolder.textView.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
            viewHolder.progressBar.setIndeterminate(false);
        }

        @Override
        protected void onLoadingComplete() {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.ssiv.setScaleAndCenter(viewHolder.isLand ?
                    viewHolder.ssiv.getMaxScale() -.2f : viewHolder.ssiv.getMinScale(), new PointF(0,0));
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            viewHolder.progressBar.setVisibility(View.GONE);
            viewHolder.textView.setVisibility(View.VISIBLE);
            viewHolder.textView.setText(
                    Html.fromHtml(
                            viewHolder.textView.getContext().getString(
                                    R.string.error_loadimage_html,
                                    failReason == null ? "unknown" : failReason.getCause().getMessage()
                            )
                    )
            );
            FileLogger.getInstance().report("# PageLoadTask.onImageLoadError\n page.path: " + page.path);
        }

        @Override
        public void onProgressUpdate(String imageUri, View view, int current, int total) {
            int progress = (current * 100 / total);
            viewHolder.progressBar.setProgress(progress);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        for(int i = 0; i < views.size(); i++) {
            ViewHolder view = views.get(i);
            if(view!= null && view.ssiv!=null) {
//                view.ssiv.setMinimumScaleType(
//                        isLandOrientation ? SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP
//                                : SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
//                );
                view.isLand = isLandOrientation;
//                view.ssiv.resetScaleAndCenter();
                view.ssiv.setScaleAndCenter(isLandOrientation ? view.ssiv.getMaxScale() -.2f: view.ssiv.getMinScale(), new PointF(0,0));
            }
        }
        super.notifyDataSetChanged();
    }
}
