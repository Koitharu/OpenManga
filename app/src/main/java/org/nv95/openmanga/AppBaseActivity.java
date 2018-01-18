package org.nv95.openmanga;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.nv95.openmanga.common.utils.ThemeUtils;


/**
 * Created by koitharu on 21.12.17.
 */

public abstract class AppBaseActivity extends AppCompatActivity {

	private static final int REQUEST_PERMISSION = 112;

	private boolean mActionBarVisible = false;
	private boolean mHomeAsUpEnabled = false;
	private int mTheme = 0;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTheme = ThemeUtils.getAppTheme(this);
		setTheme(ThemeUtils.getAppThemeRes(mTheme));
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

	protected void stub() {
		Toast.makeText(this, "Not implemented", Toast.LENGTH_SHORT).show();
	}
}

