package org.nv95.openmanga.components.pager.imagecontroller;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.text.Html;
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
import org.nv95.openmanga.utils.InternalLinkMovement;

/**
 * Created by admin on 18.08.16.
 */

public class PageHolder implements PageLoader.Callback, InternalLinkMovement.OnLinkClickListener, SubsamplingScaleImageView.OnImageEventListener {

    public static final int SCALE_FIT = 0;
    public static final int SCALE_CROP = 1;
    public static final int SCALE_AUTO = 2;
    public static final int SCALE_SRC = 3;

    public final ViewGroup itemView;
    private final ProgressBar progressBar;
    private final SubsamplingScaleImageView ssiv;
    private final TextView textView;
    private final PageLoader loader;
    private MangaPage mPage;
    private boolean land;
    private int scale;

    public PageHolder(View view) {
        itemView = (ViewGroup) view;
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        ssiv = (SubsamplingScaleImageView) view.findViewById(R.id.ssiv);
        textView = (TextView) view.findViewById(R.id.textView_holder);
        loader = new PageLoader(this);
        textView.setMovementMethod(new InternalLinkMovement(this));
        ssiv.setOnImageEventListener(this);
    }

    public void recycle() {
        loader.cancelLoading();
        ssiv.recycle();
    }

    public void loadPage(MangaPage page, boolean isLandscape, int scaleMode) {
        land = isLandscape;
        scale = scaleMode;
        loader.loadPage(mPage = page);
    }

    @Override
    public void onLoadingStarted() {
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }

    @Override
    public void onProgressUpdated(int percent) {
        textView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percent);
    }

    @Override
    public void onLoadingComplete(String filename) {
        ssiv.setImage(ImageSource.uri(filename).tilingEnabled());
        ssiv.setDoubleTapZoomScale((ssiv.getMaxScale() + ssiv.getMinScale()) / 2.f);
        ssiv.setDoubleTapZoomStyle(SubsamplingScaleImageView.ZOOM_FOCUS_CENTER);
        switch (scale) {
            case SCALE_FIT:
                ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE);
                break;
            case SCALE_CROP:
                ssiv.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                break;
            case SCALE_AUTO:
                ssiv.setMinimumScaleType(
                        (ssiv.getSWidth() > ssiv.getSHeight()) == land ?
                                SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE
                                : SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP
                );
                break;
            case SCALE_SRC:
                ssiv.setScaleAndCenter(ssiv.getMaxScale(), new PointF(0, 0));
                break;
        }
    }

    @Override
    public void onLoadingFail(@Nullable Exception reason) {
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
        textView.setText(
                Html.fromHtml(
                        textView.getContext().getString(
                                R.string.error_loadimage_html,
                                FileLogger.getInstance().getFailMessage(textView.getContext(), reason)
                        )
                )
        );
    }

    @Override
    public void onLinkClicked(TextView view, String scheme, String url) {
        switch (url) {
            case "retry":
                loader.loadPage(mPage);
                break;
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
        if (loader.getStatus() == PageLoader.STATUS_DONE) {
            loader.convert();
        } else {
            onLoadingFail(e);
        }
    }

    @Override
    public void onTileLoadError(Exception e) {
        if (loader.getStatus() == PageLoader.STATUS_DONE) {
            loader.convert();
        } else {
            onLoadingFail(e);
        }
    }

    public PageLoader getLoader() {
        return loader;
    }
}
