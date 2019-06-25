package org.nv95.openmanga.feature.main.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.main.adapter.FastHistoryAdapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.lists.MangaList;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.utils.QuickReadTask;
import org.nv95.openmanga.utils.choicecontrol.OnHolderClickListener;

/**
 * Created by admin on 19.07.17.
 */

public class FastHistoryDialog implements OnHolderClickListener {

    private final Dialog mDialog;
    private final RecyclerView mRecyclerView;
    private final HistoryProvider mProvider;
    private final TextView mTextViewHolder;

    public FastHistoryDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_fasthist, null, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mTextViewHolder = view.findViewById(R.id.textView_holder);
        mDialog = new Dialog(context, R.style.FullScreenDialog);
        if (context instanceof Activity) {
            mDialog.setOwnerActivity((Activity) context);
        }
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(view);
        final int color = ContextCompat.getColor(context, R.color.transparent_dark);
        final Window window = mDialog.getWindow();
        assert window != null;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            view.setFitsSystemWindows(true);
        }
        mProvider = HistoryProvider.getInstance(context);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.startAnimation(AnimationUtils.loadAnimation(mDialog.getContext(), R.anim.up_to_bottom));
                mDialog.dismiss();
            }
        });
    }

    public void show(int maxItems) {
        MangaList list = mProvider.getLast(maxItems);
        if (list.isEmpty()) {
            mTextViewHolder.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setAdapter(new FastHistoryAdapter(list, this));
            mRecyclerView.startAnimation(AnimationUtils.loadAnimation(mDialog.getContext(), R.anim.up_from_bottom));
        }
        mDialog.show();
    }

    @Override
    public boolean onClick(RecyclerView.ViewHolder viewHolder) {
        FastHistoryAdapter adapter = (FastHistoryAdapter) mRecyclerView.getAdapter();
        MangaInfo mangaInfo = adapter.getItem(viewHolder.getAdapterPosition());
        new QuickReadTask(mDialog.getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mangaInfo);
        mDialog.dismiss();
        return true;
    }

    @Override
    public boolean onLongClick(RecyclerView.ViewHolder viewHolder) {
        return false;
    }
}
