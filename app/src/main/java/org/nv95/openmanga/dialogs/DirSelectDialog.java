package org.nv95.openmanga.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.adapters.DirAdapter;

import java.io.File;

/**
 * Created by nv95 on 01.01.16.
 */
public class DirSelectDialog implements DialogInterface.OnClickListener, AdapterView.OnItemClickListener {
    private final AlertDialog dialog;
    private final ListView listView;
    private final DirAdapter adapter;
    private final TextView headerUp;
    private OnDirSelectListener dirSelectListener;

    public DirSelectDialog(final Context context) {
        listView = new ListView(context);
        adapter = new DirAdapter(context, LocalMangaProvider.getMangaDir(context));
        headerUp = (TextView) View.inflate(context, R.layout.item_dir, null);
        headerUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hardware_keyboard_return, 0, 0, 0);
        headerUp.setMaxLines(2);
        headerUp.setText(adapter.getCurrentDir().getPath());
        listView.addHeaderView(headerUp);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        dialog = new AlertDialog.Builder(context)
                .setView(listView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, this)
                .setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dirSelectListener != null) {
                            dirSelectListener.onDirSelected(context.getExternalFilesDir("saved"));
                        }
                    }
                })
                .setCancelable(true)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dirSelectListener != null) {
            dirSelectListener.onDirSelected(adapter.getCurrentDir());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            File dir = adapter.getCurrentDir().getParentFile();
            if (dir != null) {
                adapter.setCurrentDir(adapter.getCurrentDir().getParentFile());
            }
        } else {
            adapter.setCurrentDir(adapter.getItem(position - 1));
        }
        headerUp.setText(adapter.getCurrentDir().getPath());
        adapter.notifyDataSetChanged();
    }

    public DirSelectDialog setDirSelectListener(OnDirSelectListener dirSelectListener) {
        this.dirSelectListener = dirSelectListener;
        return this;
    }

    public void show() {
        dialog.show();
    }

    public interface OnDirSelectListener {
        void onDirSelected(File dir);
    }

}
