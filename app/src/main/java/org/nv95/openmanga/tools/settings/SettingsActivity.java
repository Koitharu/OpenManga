package org.nv95.openmanga.tools.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;

import org.nv95.openmanga.AppBaseActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.TextUtils;

/**
 * Created by koitharu on 17.01.18.
 */

public class SettingsActivity extends AppBaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	public static final String ACTION_SETTINGS_APPEARANCE = "org.nv95.openmanga.ACTION_SETTINGS_APPEARANCE";

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
			default:
				//TODO
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
		}
	}
}
