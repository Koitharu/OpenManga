package org.nv95.openmanga.feature.main.dialog;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.utils.LayoutUtils;

/**
 * Created by nv95 on 09.10.16.
 */

public class PageNumberDialog implements DialogInterface.OnClickListener, SeekBar.OnSeekBarChangeListener, DialogInterface.OnShowListener {

    private AlertDialog mDialog;
    private NavigationListener mNavigationListener;
    private Context mContext;
    //controls
    private EditText mEditText;
    private TextView mTextView;

    public PageNumberDialog(Context context) {
        this.mContext = context;
        View view = View.inflate(context, R.layout.dialog_pagenumber, null);
        mEditText = view.findViewById(R.id.editText);
        mTextView = view.findViewById(R.id.textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setTitle(R.string.navigate);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton(R.string.navigate, this);
        mDialog = builder.create();
        mDialog.setOnShowListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextView.setText(mContext.getString(R.string.goto_page, progress + 1, seekBar.getMax()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void show(int current) {
        mEditText.setText(String.valueOf(current));
        mDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mNavigationListener != null)
            try {
                int num = Integer.parseInt(mEditText.getText().toString());
                if (num < 0) {
                    Toast.makeText(mContext, R.string.invalid_value, Toast.LENGTH_SHORT).show();
                } else {
                    mNavigationListener.onPageChange(num);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(mContext, R.string.invalid_value, Toast.LENGTH_SHORT).show();
            }
    }

    public PageNumberDialog setNavigationListener(NavigationListener navigationListener) {
        this.mNavigationListener = navigationListener;
        return this;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mEditText.setSelection(mEditText.getText().length());
        LayoutUtils.showSoftKeyboard(mEditText);
    }
}

