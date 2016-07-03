package org.nv95.openmanga.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 16.10.15.
 */
public class NavigationDialog implements DialogInterface.OnClickListener, SeekBar.OnSeekBarChangeListener {

    protected AlertDialog mDialog;
    protected NavigationListener mNavigationListener;
    private Context mContext;
    //controls
    private SeekBar mSeekBar;
    private TextView mTextView;
    private int mPos;

    public NavigationDialog(Context context, int size, int pos) {
        this.mPos = pos;
        this.mContext = context;
        View view = View.inflate(context, R.layout.dialog_navigation, null);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mTextView = (TextView) view.findViewById(R.id.textView);
        ((TextView) view.findViewById(R.id.textView_summary)).setText(String.format(context.getString(R.string.current_summary), size));
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(size - 1);
        mSeekBar.setProgress(pos);
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
        mDialog = builder.create();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextView.setText(String.format(mContext.getString(R.string.current_pos), mPos + 1, progress + 1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void show() {
        mDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mNavigationListener != null && mPos != mSeekBar.getProgress())
            mNavigationListener.onPageChange(mSeekBar.getProgress());
    }

    public NavigationDialog setNavigationListener(NavigationListener navigationListener) {
        this.mNavigationListener = navigationListener;
        return this;
    }

    public interface NavigationListener {
        void onPageChange(int page);
    }
}
