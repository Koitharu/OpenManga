package org.nv95.openmanga;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;

/**
 * Created by nv95 on 11.02.16.
 */
public class FilterSortDialog {
    private static final int TAB_GENRES = 0;
    private static final int TAB_SORT = 1;

    private final Dialog mDialog;
    private final Callback mCallback;
    private final ListView mListView[];
    private final TabHost mTabHost;

    @SuppressLint("InflateParams")
    public FilterSortDialog(Context context, Callback callback) {
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_filtersort, null);
        mListView = new ListView[] {
                (ListView) contentView.findViewById(R.id.listView_genre),
                (ListView) contentView.findViewById(R.id.listView_sort)
        };
        mTabHost = (TabHost) contentView.findViewById(R.id.tabHost);
        mTabHost.setup();
        mDialog = new AlertDialog.Builder(context)
                .setView(contentView)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int genre = mListView[TAB_GENRES].getCheckedItemPosition();
                        int sort = mListView[TAB_SORT].getCheckedItemPosition();
                        String genreName = genre <= 0 ?
                                null :
                                (String) mListView[TAB_GENRES].getItemAtPosition(genre);
                        String sortName = sort == -1 ?
                                null :
                                (String) mListView[TAB_SORT].getItemAtPosition(sort);
                        mCallback.onApply(
                                genre, sort,
                                genreName, sortName
                        );
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        mCallback = callback;
    }

    public FilterSortDialog genres(String[] entries, int selected, String title) {
        TabHost.TabSpec spec = mTabHost.newTabSpec("genre");

        spec.setContent(R.id.listView_genre);
        spec.setIndicator(title);
        mTabHost.addTab(spec);

        mListView[TAB_GENRES].setAdapter(new ArrayAdapter<>(mDialog.getContext(),
                android.R.layout.simple_list_item_single_choice, entries));
        mListView[TAB_GENRES].setItemChecked(selected, true);
        return this;
    }

    public FilterSortDialog sort(String[] entries, int selected) {
        TabHost.TabSpec spec = mTabHost.newTabSpec("sort");

        spec.setContent(R.id.listView_sort);
        spec.setIndicator(mDialog.getContext().getString(R.string.action_sort));
        mTabHost.addTab(spec);
        mListView[TAB_SORT].setAdapter(new ArrayAdapter<>(mDialog.getContext(),
                android.R.layout.simple_list_item_single_choice, entries));
        mListView[TAB_SORT].setItemChecked(selected, true);
        return this;
    }

    public void show() {
        show(0);
    }

    public void show(int initialTab) {
        mTabHost.setCurrentTab(initialTab);
        mDialog.show();
    }

    public interface Callback {
        void onApply(int genre, int sort, @Nullable String genreName, @Nullable String sortName);
    }
}
