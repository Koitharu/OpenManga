package org.nv95.openmanga.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 11.02.16.
 */
public class FilterSortDialog implements TabLayout.OnTabSelectedListener {
    private static final int TAB_GENRES = 0;
    private static final int TAB_SORT = 1;

    private final Dialog mDialog;
    private final Callback mCallback;
    private final ListView mListView[];
    private final boolean mTabsEnabled[];
    private TabLayout mTabLayout;

    @SuppressLint("InflateParams")
    public FilterSortDialog(Context context, Callback callback) {
        View contentView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_filtersort, null);
        mListView = new ListView[] {
                (ListView) contentView.findViewById(R.id.listView_genre),
                (ListView) contentView.findViewById(R.id.listView_sort)
        };
        mTabsEnabled = new boolean[] {
                false,
                false
        };
        mTabLayout = (TabLayout) contentView.findViewById(R.id.tabLayout);
        mTabLayout.setOnTabSelectedListener(this);
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
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(title);
        tab.setTag(TAB_GENRES);
        mTabLayout.addTab(tab);
        mTabsEnabled[TAB_GENRES] = true;

        mListView[TAB_GENRES].setAdapter(new ArrayAdapter<>(mDialog.getContext(),
                android.R.layout.simple_list_item_single_choice, entries));
        mListView[TAB_GENRES].setItemChecked(selected, true);
        return this;
    }

    public FilterSortDialog sort(String[] entries, int selected) {
        TabLayout.Tab tab = mTabLayout.newTab();
        tab.setText(R.string.action_sort);
        tab.setTag(TAB_SORT);
        mTabLayout.addTab(tab);
        mTabsEnabled[TAB_SORT] = true;
        mListView[TAB_SORT].setAdapter(new ArrayAdapter<>(mDialog.getContext(),
                android.R.layout.simple_list_item_single_choice, entries));
        mListView[TAB_SORT].setItemChecked(selected, true);
        return this;
    }

    public void show() {
        show(0);
    }

    public void show(int initialTab) {
        if (mTabsEnabled[0] || mTabsEnabled[1]) {
            mTabLayout.getTabAt(initialTab).select();
            setSelectedTab(initialTab);
            mDialog.show();
        } else {
            new AlertDialog.Builder(mDialog.getContext())
                    .setMessage(R.string.no_options_available)
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show();
        }
    }

    private void setSelectedTab(int tab) {
        mListView[0].setVisibility(tab == 0 || !mTabsEnabled[1] ? View.VISIBLE : View.GONE);
        mListView[1].setVisibility(tab == 1 || !mTabsEnabled[0] ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        setSelectedTab(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        mListView[tab.getPosition()].setSelection(0);
    }

    public interface Callback {
        void onApply(int genre, int sort, @Nullable String genreName, @Nullable String sortName);
    }
}
