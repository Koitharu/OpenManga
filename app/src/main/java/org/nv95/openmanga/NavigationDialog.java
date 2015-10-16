package org.nv95.openmanga;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by nv95 on 16.10.15.
 */
public class NavigationDialog implements DialogInterface.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private Context context;
    protected AlertDialog dialog;
    protected NavigationListener navigationListener;
    //controls
    private SeekBar seekBar;
    private TextView textView;
    private int size, pos;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textView.setText(String.format(context.getString(R.string.current_pos), pos, progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface NavigationListener {
        void onPageChange(int page);
    }

    public NavigationDialog(Context context, int size, int pos) {
        this.size = size;
        this.pos = pos;
        this.context = context;
        View view = View.inflate(context, R.layout.dialog_navigation, null);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        textView = (TextView) view.findViewById(R.id.textView);
        ((TextView) view.findViewById(R.id.textView_summary)).setText(String.format(context.getString(R.string.current_summary),  size));
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(size);
        seekBar.setProgress(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setTitle(R.string.navigate);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(android.R.string.ok, this);
        dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (navigationListener != null && pos != seekBar.getProgress())
            navigationListener.onPageChange(seekBar.getProgress());
    }

    public NavigationDialog setNavigationListener(NavigationListener navigationListener) {
        this.navigationListener = navigationListener;
        return this;
    }
}
