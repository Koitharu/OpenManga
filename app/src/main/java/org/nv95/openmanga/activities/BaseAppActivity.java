package org.nv95.openmanga.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 19.02.16.
 */
public abstract class BaseAppActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 112;
    private boolean mActionBarVisible = false;
    private boolean mHomeAsUpEnabled = false;
    private boolean mDarkTheme = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darktheme", false)) {
            setTheme(R.style.AppTheme_Dark);
            mDarkTheme = true;
        }
    }

    public void enableHomeAsUp() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && !mHomeAsUpEnabled) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            mHomeAsUpEnabled = true;
        }
    }

    public void enableHomeAsClose() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && !mHomeAsUpEnabled) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_light);
            mHomeAsUpEnabled = true;
        }
    }

    public void disableTitle() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        mActionBarVisible = true;
    }

    public void setSupportActionBar(@IdRes int toolbarId) {
        setSupportActionBar((Toolbar) findViewById(toolbarId));
    }

    public void hideActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && mActionBarVisible) {
            mActionBarVisible = false;
            actionBar.hide();
        }
    }

    public void showActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && !mActionBarVisible) {
            mActionBarVisible = true;
            actionBar.show();
        }
    }

    public void toggleActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (!mActionBarVisible) {
                mActionBarVisible = true;
                actionBar.show();
            } else {
                mActionBarVisible = false;
                actionBar.hide();
            }
        }
    }

    public boolean isActionBarVisible() {
        return mActionBarVisible;
    }

    public boolean isDarkTheme() {
        return mDarkTheme;
    }

    public void setSubtitle(@Nullable CharSequence subtitle) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subtitle);
        }
    }

    public void setSubtitle(@StringRes int subtitle) {
        setSubtitle(getString(subtitle));
    }

    public void enableTransparentStatusBar(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            final Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            if (color != 0) {
                window.setStatusBarColor(ContextCompat.getColor(this, color));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && mHomeAsUpEnabled) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this,
                permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    REQUEST_PERMISSION);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted(permissions[i]);
                }
            }
        }
    }

    protected void onPermissionGranted(String permission) {

    }

    public void showToast(CharSequence text, int gravity, int delay) {
        final Toast toast = Toast.makeText(this, text, delay);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }

    public void showToast(@StringRes int text, int gravity, int delay) {
        showToast(getString(text), gravity, delay);
    }

    public boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isAvailable() && ni.isConnected();
    }

    public boolean checkConnectionWithToast() {
        if (checkConnection()) {
            return true;
        } else {
            Toast.makeText(this, R.string.no_network_connection, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean checkConnectionWithSnackbar(View view) {
        if (checkConnection()) {
            return true;
        } else {
            Snackbar.make(view, R.string.no_network_connection, Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean checkConnectionWithSnackbar(View view, View.OnClickListener callback) {
        if (checkConnection()) {
            return true;
        } else {
            Snackbar.make(view, R.string.no_network_connection, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry, callback)
                    .show();
            return false;
        }
    }


    public boolean checkConnectionWithDialog(DialogInterface.OnDismissListener onDismissListener) {
        if (checkConnection()) {
            return true;
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_network_connection)
                    .setTitle(R.string.error)
                    .setNegativeButton(R.string.close, null)
                    .setOnDismissListener(onDismissListener)
                    .setCancelable(true)
                    .create().show();
            return false;
        }
    }
}
