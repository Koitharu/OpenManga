package org.nv95.openmanga.feature.read.reader;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.read.util.ReaderConfig;
import org.nv95.openmanga.items.MangaPage;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.FileLogger;
import org.nv95.openmanga.utils.InternalLinkMovement;
import org.nv95.openmanga.utils.SsivUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by nv95 on 15.11.16.
 */

public class ReaderAdapter extends RecyclerView.Adapter<ReaderAdapter.PageHolder> {

    private final PageLoader mLoader;
    private final InternalLinkMovement mMovement;

    public ReaderAdapter(Context context, InternalLinkMovement.OnLinkClickListener linkListener) {
        mLoader = new PageLoader(context);
        mMovement = new InternalLinkMovement(linkListener);
        setHasStableIds(true);
    }

    public void setPages(@NonNull List<MangaPage> pages) {
        mLoader.setPages(pages);
    }

    public void setScaleMode(int mode) {
        PageHolder.scaleMode = mode;
    }

    public PageLoader getLoader() {
        return mLoader;
    }

    @Override
    public PageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PageHolder holder = new PageHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false));
        holder.textView.setMovementMethod(mMovement);
        mLoader.addListener(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(PageHolder holder, int position) {
        holder.reset();
        PageWrapper wrapper = mLoader.requestPage(position);
        if (wrapper != null) {
            switch (wrapper.getState()) {
                case PageWrapper.STATE_PROGRESS:
                    holder.onLoadingStarted(wrapper, false);
                    break;
                case PageWrapper.STATE_LOADED:
                    if (wrapper.mFilename != null) {
                        holder.onLoadingComplete(wrapper, false);
                    } else {
                        holder.onLoadingFail(wrapper, false);
                    }
            }
        }
    }

    public void finish() {
        mLoader.clearListeners();
        mLoader.cancelAll();
        mLoader.setEnabled(false);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onViewRecycled(PageHolder holder) {
        holder.reset();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mLoader.getWrappersList().size();
    }

    public PageWrapper getItem(int position) {
        return mLoader.getWrappersList().get(position);
    }

    public void reload(int position) {
        mLoader.drop(position);
        notifyItemChanged(position);
    }

    static class PageHolder extends RecyclerView.ViewHolder implements PageLoadListener,
            SubsamplingScaleImageView.OnImageEventListener, FileConverter.ConvertCallback {

        static int scaleMode;

        final ProgressBar progressBar;
        final SubsamplingScaleImageView ssiv;
        final TextView textView;
        @Nullable
        PageWrapper pageWrapper;

        PageHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            ssiv = itemView.findViewById(R.id.ssiv);
            textView = itemView.findViewById(R.id.textView_holder);
            ssiv.setExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            ssiv.setOnImageEventListener(this);
        }

        void reset() {
            pageWrapper = null;
            ssiv.recycle();
            textView.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingStarted(PageWrapper page, boolean shadow) {
            if (page.position == getAdapterPosition()) {
                textView.setText(textView.getContext().getString(R.string.loading).toUpperCase(Locale.getDefault()));
                textView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
            }
        }

        @Override
        public void onProgressUpdated(PageWrapper page, boolean shadow, int percent) {
            if (page.position == getAdapterPosition()) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(percent);
                textView.setText(textView.getContext().getString(R.string.loading_percent, percent).toUpperCase(Locale.getDefault()));
                progressBar.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoadingComplete(PageWrapper page, boolean shadow) {
            if (page.position == getAdapterPosition()) {
                textView.setText(textView.getContext().getString(R.string.wait).toUpperCase(Locale.getDefault()));
                textView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                pageWrapper = page;
                //noinspection ConstantConditions
                ssiv.setImage(ImageSource.uri(page.getFilename()).tilingEnabled());
            }
        }

        @Override
        public void onLoadingFail(PageWrapper page, boolean shadow) {
            if (page.position == getAdapterPosition()) {
                onImageLoadError(page.getError());
            }
        }

        @Override
        public void onLoadingCancelled(PageWrapper page, boolean shadow) {
            if (page.position == getAdapterPosition()) {
                progressBar.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReady() {

        }

        @Override
        public void onImageLoaded() {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            switch (scaleMode) {
                case ReaderConfig.SCALE_FIT:
                    SsivUtils.setScaleFit(ssiv);
                    break;
                case ReaderConfig.SCALE_FIT_W:
                    SsivUtils.setScaleWidthTop(ssiv);
                    break;
                case ReaderConfig.SCALE_FIT_H:
                    SsivUtils.setScaleHeightLeft(ssiv);
                    break;
                case ReaderConfig.SCALE_FIT_H_REV:
                    SsivUtils.setScaleHeightRight(ssiv);
                    break;
                case ReaderConfig.SCALE_ZOOM:
                    SsivUtils.setScaleZoomSrc(ssiv);
                    break;
            }
            AlphaAnimation aa = new AlphaAnimation(0.f, 1.f);
            aa.setDuration(100);
            aa.setRepeatCount(0);
            ssiv.startAnimation(aa);
        }

        @Override
        public void onPreviewLoadError(Exception e) {

        }

        @Override
        public void onImageLoadError(Exception e) {
            if (pageWrapper != null && !pageWrapper.isConverted() && pageWrapper.mFilename != null) {
                FileConverter.getInstance().convertAsync(pageWrapper.getFilename(), this);
                return;
            }
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(
                    AppHelper.fromHtml(textView.getContext().getString(
                            R.string.error_loadimage_html,
                            FileLogger.getInstance().getFailMessage(textView.getContext(), e)
                    ), true)
            );
        }

        @Override
        public void onTileLoadError(Exception e) {

        }

        @Override
        public void onPreviewReleased() {

        }

        @Override
        public void onConvertDone(boolean success) {
            if (pageWrapper != null) {
                pageWrapper.setConverted();
                if (success) {
                    onLoadingComplete(pageWrapper, false);
                } else {
                    onImageLoadError(new FileConverter.ConvertException());
                }
            }
        }
    }
}
