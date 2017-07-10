package org.nv95.openmanga.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.LayoutUtils;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Created by admin on 10.07.17.
 */

public class SyncSettingsActivity extends BaseAppActivity implements Preference.OnPreferenceClickListener {

    private SyncHelper syncHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();

        syncHelper = SyncHelper.get(this);

        getFragmentManager().beginTransaction()
                .add(R.id.content, syncHelper.isAuthorized() ? new SyncSettingsFragment() : new LoginFragment())
                .commit();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "sync.start":
                SyncService.start(this);
                return true;
            default:
                return false;
        }
    }

    public static class SyncSettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Activity activity = getActivity();
            addPreferencesFromResource(R.xml.pref_sync);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Context context = getActivity();
            SyncHelper syncHelper = SyncHelper.get(context);
            Preference p = findPreference("sync.start");
            Date lastSync = syncHelper.getLastSync();
            if (lastSync != null) {
                p.setSummary(context.getString(R.string.last_sync, AppHelper.getReadableDateTimeRelative(lastSync.getTime())));
            }
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) context);
        }
    }

    public static class LoginFragment extends Fragment implements View.OnClickListener {

        private EditText mEditLogin;
        private EditText mEditPassword;
        private Button mButtonLogin;
        private Button mButtonRegister;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_syncauth, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mEditLogin = view.findViewById(R.id.editLogin);
            mEditPassword = view.findViewById(R.id.editPassword);
            mButtonLogin = view.findViewById(R.id.buttonLogin);
            mButtonRegister = view.findViewById(R.id.buttonRegister);
            mButtonRegister.setOnClickListener(this);
            mButtonLogin.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            LayoutUtils.hideSoftKeyboard(mEditPassword);
            new AuthTask((SyncSettingsActivity) getActivity(), view.getId() == R.id.buttonRegister)
                    .executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            mEditLogin.getText().toString().trim(),
                            mEditPassword.getText().toString().trim()
                    );
        }


        private static class AuthTask extends AsyncTask<String, Void, SyncHelper.RESTResponse> implements DialogInterface.OnCancelListener {

            private final WeakReference<SyncSettingsActivity> mActivityRef;
            private final ProgressDialog mProgressDialog;
            private final boolean mRegister;

            AuthTask(SyncSettingsActivity activity, boolean isRegister) {
                mRegister = isRegister;
                mActivityRef = new WeakReference<>(activity);
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(activity.getString(R.string.loading));
                mProgressDialog.setOnCancelListener(this);
            }


            @Override
            protected SyncHelper.RESTResponse doInBackground(String... strings) {
                try {
                    SyncHelper syncHelper = SyncHelper.get(mActivityRef.get());
                    if (mRegister) {
                        return syncHelper.register(strings[0], strings[1]);
                    } else {
                        return syncHelper.authorize(strings[0], strings[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return SyncHelper.RESTResponse.fromThrowable(e);
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog.show();
            }

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                this.cancel(false);
            }

            @Override
            protected void onPostExecute(SyncHelper.RESTResponse restResponse) {
                super.onPostExecute(restResponse);
                mProgressDialog.dismiss();
                SyncSettingsActivity activity = mActivityRef.get();
                if (activity == null) {
                    return;
                }
                if (restResponse.isSuccess()) {
                    Toast.makeText(activity, R.string.successfully, Toast.LENGTH_SHORT).show();
                    activity.getFragmentManager().beginTransaction()
                            .replace(R.id.content, new SyncSettingsFragment())
                            .commit();
                } else {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.auth_failed)
                            .setMessage(restResponse.getMessage())
                            .setPositiveButton(android.R.string.ok, null)
                            .create().show();
                }
            }
        }
    }
}
