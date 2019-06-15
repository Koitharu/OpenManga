package org.nv95.openmanga.feature.preview.dialog;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.MangaSaveHelper;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.LocalMangaProvider;

import java.util.ArrayList;

/**
 * Created by nv95 on 27.09.16.
 */

public class ChaptersSelectDialog implements Toolbar.OnMenuItemClickListener, View.OnClickListener {

    private final AlertDialog mDialog;
    private final View mContentView;

    private final ListView mListView;
    private final Toolbar mToolbar;

    public ChaptersSelectDialog(Context context) {
        mContentView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_chapters, null, false);
        mListView = mContentView.findViewById(R.id.listView);
        mToolbar = mContentView.findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_cancel_light);
        mToolbar.inflateMenu(R.menu.chapters);
        mToolbar.setNavigationOnClickListener(this);
        mToolbar.setOnMenuItemClickListener(this);

        mDialog = new AlertDialog.Builder(context)
                .setView(mContentView)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    public void showSave(MangaSummary mangaSummary, @StringRes int title) {
        mToolbar.setTitle(title);
        mToolbar.setSubtitle(mContentView.getContext().getString(R.string.chapters_total, mangaSummary.chapters.size()));
        mListView.setAdapter(new ArrayAdapter<>(
                mContentView.getContext(),
                R.layout.item_multiple_choice,
                mangaSummary.getChapters().getNames()
        ));
        checkUnsaved(mangaSummary);
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, mContentView.getContext().getString(R.string.action_save), new SaveClickListener(mangaSummary));
        mDialog.show();
    }

    public void showRemove(MangaSummary mangaSummary, OnChaptersRemoveListener listener) {
        mToolbar.setTitle(R.string.action_remove);
        mToolbar.setSubtitle(mContentView.getContext().getString(R.string.chapters_total, mangaSummary.chapters.size()));
        mListView.setAdapter(new ArrayAdapter<>(
                mContentView.getContext(),
                R.layout.item_multiple_choice,
                mangaSummary.getChapters().getNames()
        ));
        checkAll();
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, mContentView.getContext().getString(R.string.action_remove), new RemoveClickListener(mangaSummary, listener));
        mDialog.show();
    }

    private void checkAll() {
        for (int i=mListView.getCount() - 1;i>=0;i--) {
            mListView.setItemChecked(i, true);
        }
    }

    private void checkUnsaved(MangaSummary mangaSummary) {
        ArrayList<Integer> ids = LocalMangaProvider.getInstance(mDialog.getContext())
                .getLocalChaptersNumbers(mangaSummary.id);
        for (int i=mListView.getCount() - 1;i>=0;i--) {
            mListView.setItemChecked(i, !ids.contains(mangaSummary.chapters.get(i).number));
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_checkall:
                for (int i=mListView.getCount() - 1;i>=0;i--) {
                    mListView.setItemChecked(i, !mListView.isItemChecked(i));
                }
                break;

        }
        return true;
    }

    @Override
    public void onClick(View v) {
        mDialog.dismiss();
    }

    private class SaveClickListener implements DialogInterface.OnClickListener {

        private final MangaSummary mMangaSummary;

        private SaveClickListener(MangaSummary manga) {
            mMangaSummary = manga;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            MangaSummary copy = new MangaSummary(mMangaSummary);
            copy.chapters.clear();
            for (int i=0;i<mMangaSummary.chapters.size();i++) {
                if (mListView.isItemChecked(i)) {
                    copy.chapters.add(mMangaSummary.chapters.get(i));
                }
            }
            dialog.dismiss();
            new MangaSaveHelper(mContentView.getContext()).save(copy);
        }
    }

    private class RemoveClickListener implements DialogInterface.OnClickListener {

        private final MangaSummary mMangaSummary;
        private final OnChaptersRemoveListener mListener;

        private RemoveClickListener(MangaSummary manga, OnChaptersRemoveListener listener) {
            mListener = listener;
            mMangaSummary = manga;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            int checked = mListView.getCheckedItemCount();
            if (checked == 0) {
                return;
            }
            int full = mMangaSummary.getChapters().size();
            if (full == checked) {
                mListener.onChaptersRemove(null);
                return;
            }
            long[] ids = new long[checked];
            int j = 0;
            for (int i=0;i<mMangaSummary.chapters.size();i++) {
                if (mListView.isItemChecked(i)) {
                    ids[j++] = mMangaSummary.chapters.get(i).id;
                }
            }
            mListener.onChaptersRemove(ids);
        }
    }

    public interface OnChaptersRemoveListener {
        void onChaptersRemove(@Nullable long[] ids);
    }
}