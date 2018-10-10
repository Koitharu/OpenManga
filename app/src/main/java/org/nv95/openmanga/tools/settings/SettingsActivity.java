package org.nv95.openmanga.tools.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.TextUtils;
import org.nv95.openmanga.updchecker.JobSetupReceiver;
import org.nv95.openmanga.updchecker.UpdatesCheckService;

/**
 * Created by koitharu on 17.01.18.
 */

public class SettingsActivity extends AppBaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
		Preference.OnPreferenceClickListener {

	public static final String ACTION_SETTINGS_APPEARANCE = "org.nv95.openmanga.ACTION_SETTINGS_APPEARANCE";
	public static final String ACTION_SETTINGS_READER = "org.nv95.openmanga.ACTION_SETTINGS_READER";
	public static final String ACTION_SETTINGS_SHELF = "org.nv95.openmanga.ACTION_SETTINGS_SHELF";
	public static final String ACTION_SETTINGS_MANGAUPDATES = "org.nv95.openmanga.ACTION_SETTINGS_MANGAUPDATES";

	public static final int RESULT_RESTART = Activity.RESULT_FIRST_USER + 1;

	private PreferenceFragment mFragment;
	private View mContent;
	private SharedPreferences mDefaultPreferences;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();
		mContent = findViewById(R.id.content);
		mDefaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		final String action = TextUtils.notNull(getIntent().getAction());
		switch (action) {
			case Intent.ACTION_MANAGE_NETWORK_USAGE:
				mFragment = new NetworkSettingsFragment();
				break;
			case ACTION_SETTINGS_APPEARANCE:
				mFragment = new AppearanceSettingsFragment();
				break;
			case ACTION_SETTINGS_READER:
				mFragment = new ReaderSettingsFragment();
				break;
			case ACTION_SETTINGS_SHELF:
				mFragment = new ShelfSettingsFragment();
				break;
			case ACTION_SETTINGS_MANGAUPDATES:
				mFragment = new MangaUpdatesSettingsFragment();
				break;
			default:
				finish();
		}
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.content, mFragment)
				.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mDefaultPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		mDefaultPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onStop();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		switch (key) {
			case "theme":
				setResult(RESULT_RESTART);
				break;
			case "mangaupdates.enabled":
				JobSetupReceiver.setup(this);
				break;
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		switch (preference.getKey()) {
			case "mangaupdates.check_now":
				UpdatesCheckService.runForce(this);
				Snackbar.make(mFragment.getView(), R.string.checking_new_chapters, Snackbar.LENGTH_SHORT).show();
				return true;
			default:
				return false;
		}
	}
}
