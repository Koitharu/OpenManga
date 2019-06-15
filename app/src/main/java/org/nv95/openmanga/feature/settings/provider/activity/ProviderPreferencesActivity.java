package org.nv95.openmanga.feature.settings.provider.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.providers.staff.ProviderSummary;
import org.nv95.openmanga.providers.staff.Providers;

import java.lang.reflect.Method;

/**
 * Created by nv95 on 21.11.16.
 */

public class ProviderPreferencesActivity extends BaseAppActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        enableHomeAsUp();
        int pid = getIntent().getIntExtra("provider", -1);
        if (pid == -1) {
            finish();
            return;
        }
        ProviderSummary ps = Providers.getById(pid);
        if (ps == null) {
            finish();
            return;
        }
        setTitle(ps.name);
        setSubtitle(R.string.action_settings);
        ProviderPrefFragment fragment = new ProviderPrefFragment();
        fragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    public static class ProviderPrefFragment extends PreferenceFragment {

        private ProviderSummary mProvider;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mProvider = Providers.getById(getArguments().getInt("provider"));
            getPreferenceManager().setSharedPreferencesName("prov_" + mProvider.aClass.getSimpleName());
            addPreferencesFromResource(mProvider.preferences);

            Preference testPref = findPreference("auth_test");
            if (testPref != null) {
                testPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
                        new TestAuthTask(getActivity()).executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                prefs.getString("login",""),
                                prefs.getString("password",""),
                                prefs.getString("domain","")
                        );
                        return true;
                    }
                });
            }
        }

        private class TestAuthTask extends AsyncTask<String,Void,Boolean> implements DialogInterface.OnCancelListener {

            private final Activity mActivity;
            private final ProgressDialog mDialog;

            TestAuthTask(Activity activity) {
                this.mActivity = activity;
                mDialog = new ProgressDialog(mActivity);
                mDialog.setOwnerActivity(mActivity);
                mDialog.setMessage(mActivity.getString(R.string.wait));
                mDialog.setOnCancelListener(this);
                mDialog.setCancelable(true);
                mDialog.setCanceledOnTouchOutside(true);
                mDialog.setIndeterminate(true);
            }

            @Override
            protected void onPreExecute() {
                mDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(String... strings) {
                try {
                    Method m = mProvider.aClass.getMethod("auth", String.class, String.class, String.class);
                    return (Boolean) m.invoke(null, strings[0], strings[1], strings[2]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                int msg = R.string.error;
                if (Boolean.TRUE.equals(aBoolean)) {
                    msg = R.string.successfully;
                } else if (Boolean.FALSE.equals(aBoolean)) {
                    msg = R.string.auth_failed;
                }
                mDialog.dismiss();
                new AlertDialog.Builder(mActivity)
                        .setMessage(msg)
                        .setPositiveButton(R.string.close, null)
                        .create()
                        .show();
            }

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                this.cancel(false);
            }
        }
    }
}
