package org.nv95.openmanga.feature.read.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.read.adapter.ThumbnailsAdapter;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.feature.read.reader.PageLoadListener;
import org.nv95.openmanga.feature.read.reader.PageLoader;
import org.nv95.openmanga.feature.read.reader.PageWrapper;
import org.nv95.openmanga.items.ThumbSize;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 18.11.16.
 */

public class ThumbnailsDialog implements DialogInterface.OnDismissListener, PageLoadListener, NavigationListener {

    private final Dialog mDialog;
    private final RecyclerView mRecyclerView;
    private final PageLoader mLoader;
    @Nullable
    private ThumbnailsAdapter mAdapter;
    @Nullable
    private NavigationListener mNavigationListener;

    @SuppressLint("InflateParams")
    public ThumbnailsDialog(Context context, PageLoader loader) {
        mRecyclerView = (RecyclerView) LayoutInflater.from(context).inflate(R.layout.dialog_thumbs, null, false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, LayoutUtils.getOptimalColumnsCount(context.getResources(), ThumbSize.THUMB_SIZE_MEDIUM)));
        mDialog = new Dialog(context, R.style.FullScreenDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(mRecyclerView);
        final int color = ContextCompat.getColor(context, R.color.transparent_dark);
        final Window window = mDialog.getWindow();
        assert window != null;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mRecyclerView.setFitsSystemWindows(true);
        }
        mDialog.setOnDismissListener(this);
        mLoader = loader;
    }

    public ThumbnailsDialog setNavigationListener(@Nullable NavigationListener listener) {
        mNavigationListener = listener;
        return this;
    }

    public void show(int currentpos) {
        mAdapter = new ThumbnailsAdapter(mLoader.getWrappersList());
        mAdapter.setNavigationListener(this);
        mAdapter.setCurrentPosition(currentpos);
        mRecyclerView.setAdapter(mAdapter);
        mLoader.addListener(this);
        mRecyclerView.scrollToPosition(currentpos);
        mDialog.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        mLoader.removeListener(this);
    }

    @Override
    public void onLoadingStarted(PageWrapper page, boolean shadow) {

    }

    @Override
    public void onProgressUpdated(PageWrapper page, boolean shadow, int percent) {

    }

    @Override
    public void onLoadingComplete(PageWrapper page, boolean shadow) {
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(page.position);
        }
    }

    @Override
    public void onLoadingFail(PageWrapper page, boolean shadow) {

    }

    @Override
    public void onLoadingCancelled(PageWrapper page, boolean shadow) {

    }

    @Override
    public void onPageChange(int page) {
        if (mNavigationListener != null) {
            mDialog.dismiss();
            mNavigationListener.onPageChange(page);
        }
    }
}
