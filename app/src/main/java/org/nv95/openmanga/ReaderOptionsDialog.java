package org.nv95.openmanga;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Spinner;

/**
 * Created by nv95 on 10.10.15.
 */
public class ReaderOptionsDialog implements DialogInterface.OnClickListener {
  protected AlertDialog dialog;
  protected OnOptionsChangedListener optionsChangedListener;
  //controls
  protected Spinner spinnerDirection;
  protected SwitchCompat switchKeepScreen;
  protected SwitchCompat switchVolkeyScroll;
  private Context context;

  public ReaderOptionsDialog(Context context) {
    this.context = context;
    View view = View.inflate(context, R.layout.dialog_readopts, null);
    spinnerDirection = (Spinner) view.findViewById(R.id.spinner_direction);
    switchKeepScreen = (SwitchCompat) view.findViewById(R.id.switch_keepscreen);
    switchVolkeyScroll = (SwitchCompat) view.findViewById(R.id.switch_volkeyscroll);
    //loading prefs
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    spinnerDirection.setSelection(prefs.getInt("scroll_direction", 0));
    switchKeepScreen.setChecked(prefs.getBoolean("keep_screen", false));
    switchVolkeyScroll.setChecked(prefs.getBoolean("volkeyscroll", false));
    //--
    view.findViewById(R.id.textView_keepscreen).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switchKeepScreen.performClick();
      }
    });
    view.findViewById(R.id.textView_direction).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        spinnerDirection.performClick();
      }
    });
    view.findViewById(R.id.textView_volkeyscroll).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switchVolkeyScroll.performClick();
      }
    });
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setView(view);
    builder.setTitle(R.string.action_reading_options);
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
    SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    prefEditor.putInt("scroll_direction", spinnerDirection.getSelectedItemPosition());
    prefEditor.putBoolean("keep_screen", switchKeepScreen.isChecked());
    prefEditor.putBoolean("volkeyscroll", switchVolkeyScroll.isChecked());
    prefEditor.apply();
    if (optionsChangedListener != null)
      optionsChangedListener.onOptionsChanged();
  }

  public ReaderOptionsDialog setOptionsChangedListener(OnOptionsChangedListener optionsChangedListener) {
    this.optionsChangedListener = optionsChangedListener;
    return this;
  }

  public interface OnOptionsChangedListener {
    void onOptionsChanged();
  }
}
