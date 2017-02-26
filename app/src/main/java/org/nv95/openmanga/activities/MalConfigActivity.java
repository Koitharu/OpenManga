package org.nv95.openmanga.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.MALProvider;
import org.nv95.openmanga.utils.AnimUtils;

/**
 * Created by unravel22 on 26.02.17.
 */

public class MalConfigActivity extends BaseAppActivity implements View.OnClickListener {
    
    private ProgressBar mProgressBar;
    private EditText mEditTextLogin;
    private EditText mEditTextPassword;
    private View mBlockLogin;
    private Button mButtonAuth;
    @Nullable
    private SettingsFragment mSettingsFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_malconf);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();
        
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mEditTextLogin = (EditText) findViewById(R.id.editTextLogin);
        mEditTextPassword = (EditText) findViewById(R.id.editTextPassword);
        mButtonAuth = (Button) findViewById(R.id.buttonAuth);
        mBlockLogin = findViewById(R.id.blockLogin);
        
        mButtonAuth.setOnClickListener(this);
    
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).contains("mal.password")) {
            mSettingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.content, mSettingsFragment)
                    .commit();
        } else {
            mBlockLogin.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onClick(View v) {
        new AuthTask().startLoading(mEditTextLogin.getText().toString(), mEditTextPassword.getText().toString());
    }
    
    private class AuthTask extends LoaderTask<String,Void,Boolean> {
    
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AnimUtils.crossfade(null, mProgressBar);
            mButtonAuth.setEnabled(false);
            mEditTextLogin.setEnabled(false);
            mEditTextPassword.setEnabled(false);
        }
    
        @Override
        protected Boolean doInBackground(String... params) {
            return MALProvider.checkCredentials(params[0], params[1]);
        }
    
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                AnimUtils.crossfade(mBlockLogin, null);
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .edit()
                        .putString("mal.login", mEditTextLogin.getText().toString())
                        .putString("mal.password", mEditTextPassword.getText().toString())
                        .apply();
                mSettingsFragment = new SettingsFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.content, mSettingsFragment)
                        .commit();
            } else {
                AnimUtils.crossfade(mProgressBar, null);
                mButtonAuth.setEnabled(true);
                mEditTextLogin.setEnabled(true);
                mEditTextPassword.setEnabled(true);
                mEditTextPassword.setError(getString(R.string.auth_failed));
                mEditTextPassword.requestFocus();
            }
        }
    }
    
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            addPreferencesFromResource(R.xml.pref_mal);
        }
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Context context = getActivity();
        }
    }
}
