package org.nv95.openmanga.components.reader;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.FileLogger;

import java.util.List;

/**
 * Created by nv95 on 15.11.16.
 */

public class ReaderAdapter extends RecyclerView.Adapter<ReaderAdapter.PageHolder> {

    private final PageLoader mLoader;

    public ReaderAdapter() {
        mLoader = new PageLoader();
    }

    public void setPages(List<MangaPage> pages) {
        mLoader.setPages(pages);
    }

    public PageLoader getLoader() {
        return mLoader;
    }

    @Override
    public PageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PageHolder holder = new PageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false));
        //mLoader.addListener(holder);
        return holder;
    }

    @Override
    public void onViewAttachedToWindow(PageHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(PageHolder holder) {
        super.onViewDetachedFromWindow(holder);
        mLoader.removeListener(holder);
    }

    @Override
    public void onBindViewHolder(PageHolder holder, int position) {
        mLoader.cancelLoading(holder.position);
        mLoader.addListener(holder);
        holder.reset();
        holder.position = position;
        PageWrapper wrapper = mLoader.requestPage(position);
        switch (wrapper.getState()) {
            case PageWrapper.STATE_PROGRESS:
                holder.onLoadingStarted(wrapper);
                break;
            case PageWrapper.STATE_LOADED:
                if (wrapper.mFilename != null) {
                    holder.onLoadingComplete(wrapper);
                } else {
                    holder.onLoadingFail(wrapper);
                }
        }
    }

    @Override
    public void onViewRecycled(PageHolder holder) {
        mLoader.cancelLoading(holder.position);
        holder.reset();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mLoader.getWrappersList().size();
    }

    static class PageHolder extends RecyclerView.ViewHolder implements PageLoadListener, SubsamplingScaleImageView.OnImageEventListener {

        final ProgressBar progressBar;
        final SubsamplingScaleImageView ssiv;
        final TextView textView;
        int position;

        PageHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            ssiv = (SubsamplingScaleImageView) itemView.findViewById(R.id.ssiv);
            textView = (TextView) itemView.findViewById(R.id.textView_holder);
            ssiv.setParallelLoadingEnabled(true);
            ssiv.setOnImageEventListener(this);
        }

        void reset() {
            ssiv.recycle();
            textView.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingStarted(PageWrapper page) {
            if (page.position == position) {
                textView.setText(R.string.loading);
                textView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
            }
        }

        @Override
        public void onProgressUpdated(PageWrapper page, int percent) {
            if (page.position == position) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(percent);
                textView.setText(textView.getContext().getString(R.string.loading_percent, percent));
            }
        }

        @Override
        public void onLoadingComplete(PageWrapper page) {
            if (page.position == position) {
                progressBar.setIndeterminate(true);
                //noinspection ConstantConditions
                ssiv.setImage(ImageSource.uri(page.getFilename()).tilingEnabled());
            }
        }

        @Override
        public void onLoadingFail(PageWrapper page) {
            if (page.position == position) {
                onImageLoadError(page.getError());
            }
        }

        @Override
        public void onLoadingCancelled(PageWrapper page) {
            if (page.position == getAdapterPosition()) {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReady() {

        }

        @Override
        public void onImageLoaded() {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            AlphaAnimation aa = new AlphaAnimation(0.f, 1.f);
            aa.setDuration(250);
            aa.setRepeatCount(0);
            ssiv.startAnimation(aa);
        }

        @Override
        public void onPreviewLoadError(Exception e) {

        }

        @Override
        public void onImageLoadError(Exception e) {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(
                    Html.fromHtml(
                            textView.getContext().getString(
                                    R.string.error_loadimage_html,
                                    FileLogger.getInstance().getFailMessage(textView.getContext(), e)
                            )
                    )
            );
        }

        @Override
        public void onTileLoadError(Exception e) {

        }

        @Override
        public void onPreviewReleased() {

        }
    }
}
