package org.nv95.openmanga.core.activities;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.core.network.NetworkUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by nv95 on 19.02.16.
 */
public abstract class BaseAppActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 112;

    private boolean mActionBarVisible = false;
    private boolean mHomeAsUpEnabled = false;
    private int mTheme = 0;
    @Nullable
    private ArrayList<WeakReference<AsyncTask>> mLoaders;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTheme = LayoutUtils.getAppTheme(this);
        setTheme(LayoutUtils.getAppThemeRes(mTheme));
    }

    public boolean isDarkTheme() {
        return mTheme > 7;
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

    @Deprecated
    protected void setupToolbarScrolling(Toolbar toolbar) {
        setToolbarScrollingLock(toolbar, false);
    }

    @Deprecated
    protected void setToolbarScrollingLock(Toolbar toolbar, boolean lock) {
        if (toolbar == null || !(toolbar.getParent() instanceof AppBarLayout)) {
            return;
        }
        boolean scrolls = !lock && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_toolbars", true);
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

    public boolean checkPermission(String permission) {
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

    public boolean checkConnectionWithSnackbar(View view) {
        if (NetworkUtils.checkConnection(this)) {
            return true;
        } else {
            Snackbar.make(view, R.string.no_network_connection, Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    public void registerLoaderTask(AsyncTask task) {
        if (mLoaders == null) {
            mLoaders = new ArrayList<>();
        }
        mLoaders.add(new WeakReference<>(task));
    }

    @Override
    protected void onDestroy() {
        if (mLoaders != null) {
            for (WeakReference<AsyncTask> o : mLoaders) {
                AsyncTask task = o.get();
                if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
                    task.cancel(true);
                }
            }
        }
        mLoaders = null;
        super.onDestroy();
    }

    protected boolean showcase(final View view, @StringRes int title, @StringRes int body) {
        return showcase(view, title, body, false);
    }

    protected boolean showcase(final View view, @StringRes int title, @StringRes int body, boolean tint) {
        boolean dark = isDarkTheme();
        if (view != null && view.getVisibility() == View.VISIBLE
                && !getSharedPreferences("tips", MODE_PRIVATE).getBoolean(getClass().getSimpleName() + "_" + view.getId(), false)) {
            TapTargetView.showFor(this,
                    TapTarget.forView(view, getString(title), getString(body))
                            .transparentTarget(!tint)
                            .textColorInt(Color.WHITE)
                            .dimColorInt(LayoutUtils.getAttrColor(this, R.attr.colorPrimaryDark))
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
    protected boolean showcase(@IdRes final int menuItemId, @StringRes int title, @StringRes int body) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        return toolbar != null && showcase(toolbar.findViewById(menuItemId), title, body, false);
    }

    /**
     * @return true only once for activity
     */
    protected boolean isFirstStart() {
        SharedPreferences prefs = getSharedPreferences("tips", MODE_PRIVATE);
        if (prefs.getBoolean(getClass().getName(), true)) {
            prefs.edit().putBoolean(getClass().getName(), false).apply();
            return true;
        }
        return false;
    }
}
