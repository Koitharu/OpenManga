package org.nv95.openmanga.feature.settings.main.dialog;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.MangaStore;

import java.io.File;

/**
 * Created by nv95 on 01.01.16.
 */
public class DirSelectDialog implements DialogInterface.OnClickListener, AdapterView.OnItemClickListener {

    private final AlertDialog mDialog;
    private final DirAdapter mAdapter;
    private final TextView mHeaderUp;
    private OnDirSelectListener mDirSelectListener;

    public DirSelectDialog(final Context context) {
        ListView listView = new ListView(context);
        mAdapter = new DirAdapter(context, MangaStore.getMangasDir(context));
        mHeaderUp = (TextView) View.inflate(context, R.layout.item_dir, null);
        mHeaderUp.setCompoundDrawablesWithIntrinsicBounds(LayoutUtils.getThemedIcons(context, R.drawable.ic_arrow_up)[0],
                null, null, null);
        mHeaderUp.setMaxLines(2);
        mHeaderUp.setText(mAdapter.getCurrentDir().getPath());
        listView.addHeaderView(mHeaderUp);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        mDialog = new AlertDialog.Builder(context)
                .setView(listView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, this)
                /*.setNeutralButton(R.string.resetZoom, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDirSelectListener != null) {
                            mDirSelectListener.onDirSelected(context.getExternalFilesDir("saved"));
                        }
                    }
                })*/
                .setCancelable(true)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mDirSelectListener != null) {
            mDirSelectListener.onDirSelected(mAdapter.getCurrentDir());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            File dir = mAdapter.getCurrentDir().getParentFile();
            if (dir != null) {
                mAdapter.setCurrentDir(mAdapter.getCurrentDir().getParentFile());
            }
        } else {
            mAdapter.setCurrentDir(mAdapter.getItem(position - 1));
        }
        mHeaderUp.setText(mAdapter.getCurrentDir().getPath());
        mAdapter.notifyDataSetChanged();
    }

    public DirSelectDialog setDirSelectListener(OnDirSelectListener dirSelectListener) {
        this.mDirSelectListener = dirSelectListener;
        return this;
    }

    public void show() {
        mDialog.show();
    }

    public interface OnDirSelectListener {
        void onDirSelected(File dir);
    }

}
