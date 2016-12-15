package org.nv95.openmanga.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
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

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import org.nv95.openmanga.R;

import java.util.ArrayList;

/**
 * Created by nv95 on 19.02.16.
 */
public abstract class BaseAppActivity extends AppCompatActivity {

    public static final int APP_THEME_LIGHT = 0;
    public static final int APP_THEME_DARK = 1;
    public static final int APP_THEME_BLACK = 2;

    private static final int REQUEST_PERMISSION = 112;

    private boolean mActionBarVisible = false;
    private boolean mHomeAsUpEnabled = false;
    private int mTheme = 0;
    @Nullable
    private ArrayList<AsyncTask> mLoaders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTheme = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme", "0"));
        if (mTheme != APP_THEME_LIGHT) {
            setTheme(mTheme == APP_THEME_BLACK ? R.style.AppTheme_Black : R.style.AppTheme_Dark);
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
        mActionBarVisible = toolbar != null;
    }

    void setupToolbarScrolling(Toolbar toolbar) {
        if (toolbar == null || !(toolbar.getParent() instanceof AppBarLayout)) {
            return;
        }
        boolean scrolls = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_toolbars", true);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(scrolls ? AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS : 0);
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
        return mTheme != APP_THEME_LIGHT;
    }

    public int getActivityTheme() {
        return mTheme;
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
            Window window = getWindow();
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

    boolean checkPermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(this,
                permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
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

    protected AsyncTask registerLoaderTask(AsyncTask task) {
        if (mLoaders == null) {
            mLoaders = new ArrayList<>();
        }
        mLoaders.add(task);
        return task;
    }

    protected void unregisterLoaderTask(AsyncTask task) {
        if (mLoaders != null) {
            mLoaders.remove(task);
        }
    }

    @Override
    protected void onDestroy() {
        if (mLoaders != null) {
            for (AsyncTask o : mLoaders) {
                if (o != null && o.getStatus() != AsyncTask.Status.FINISHED) {
                    o.cancel(true);
                }
            }
        }
        mLoaders = null;
        super.onDestroy();
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

    boolean showcase(final View view, @StringRes int title, @StringRes int body) {
        return showcase(view, title, body, false);
    }

    boolean showcase(final View view, @StringRes int title, @StringRes int body, boolean tint) {
        if (view != null && view.getVisibility() == View.VISIBLE
                && !getSharedPreferences("tips", MODE_PRIVATE).getBoolean(getClass().getSimpleName() + "_" + view.getId(), false)) {
            TapTargetView.showFor(this,
                    TapTarget.forView(view, getString(title), getString(body))
                            .transparentTarget(!tint)
                            .tintTarget(tint),
                    new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                        @Override
                        public void onTargetClick(TapTargetView view1) {
                            super.onTargetClick(view1);
                        }
                    });
            SharedPreferences prefs = getSharedPreferences("tips", MODE_PRIVATE);
            prefs.edit().putBoolean(BaseAppActivity.this.getClass().getSimpleName() + "_" + view.getId(), true).apply();
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param menuItemId
     * @param title
     * @param body
     * @return true if showcase shown
     */
    boolean showcase(@IdRes final int menuItemId, @StringRes int title, @StringRes int body) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        return toolbar != null && showcase(toolbar.findViewById(menuItemId), title, body, true);
    }

    /**
     * @return true only once for activity
     */
    boolean isFirstStart() {
        SharedPreferences prefs = getSharedPreferences("tips", MODE_PRIVATE);
        if (prefs.getBoolean(getClass().getName(), true)) {
            prefs.edit().putBoolean(getClass().getName(), false).apply();
            return true;
        }
        return false;
    }

    protected abstract class LoaderTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

        @CallSuper
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            registerLoaderTask(this);
        }

        @CallSuper
        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            unregisterLoaderTask(this);
        }

        @CallSuper
        @Override
        protected void onCancelled(Result result) {
            super.onCancelled(result);
            unregisterLoaderTask(this);
        }

        @CallSuper
        @Override
        protected void onCancelled() {
            super.onCancelled();
            unregisterLoaderTask(this);
        }

        @SafeVarargs
        @MainThread
        public final AsyncTask<Params, Progress, Result> startLoading(Params... params) {
            return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
    }
}
