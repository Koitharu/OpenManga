package org.nv95.openmanga.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.items.RESTResponse;
import org.nv95.openmanga.items.SyncDevice;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.NetworkUtils;
import org.nv95.openmanga.utils.PreferencesUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by admin on 10.07.17.
 */

public class SyncSettingsActivity extends BaseAppActivity implements Preference.OnPreferenceClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(R.id.toolbar);
        enableHomeAsUp();

        SyncHelper syncHelper = SyncHelper.get(this);

        getFragmentManager().beginTransaction()
                .add(R.id.content, syncHelper.isAuthorized() ? new SyncSettingsFragment() : new LoginFragment())
                .commit();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case "sync.start":
                SyncService.start(this);
                return true;
            case "sync.username":
                new AlertDialog.Builder(this)
                        .setMessage(R.string.logout_confirm)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new LogoutTask(SyncSettingsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create().show();
                return true;
            default:
                try {
                    if (key.startsWith("sync.dev")) {
                        int devId = Integer.parseInt(key.substring(9));
                        detachDevice(devId, preference);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
        }
    }

    private void detachDevice(final int devId, final Preference p) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.device_detach_confirm, p.getTitle().toString()))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        p.setSelectable(false);
                        new DetachTask(p).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, devId);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create().show();

    }

    public static class SyncSettingsFragment extends PreferenceFragment {

        private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int what = intent.getIntExtra("what", -1);
                Preference p;
                switch (what) {
                    case SyncService.MSG_UNAUTHORIZED:
                        Activity activity = getActivity();
                        if (activity != null) {
                            Toast.makeText(activity, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                            activity.getFragmentManager().beginTransaction()
                                    .replace(R.id.content, new LoginFragment())
                                    .commit();
                        }
                        break;
                    case SyncService.MSG_HIST_STARTED:
                        p = findPreference("sync.history");
                        p.setSummary(R.string.sync_started);
                        p.setEnabled(false);
                        break;
                    case SyncService.MSG_HIST_FAILED:
                        p = findPreference("sync.history");
                        p.setSummary(R.string.sync_failed);
                        p.setEnabled(true);
                        break;
                    case SyncService.MSG_HIST_FINISHED:
                        p = findPreference("sync.history");
                        p.setSummary(R.string.sync_finished);
                        p.setEnabled(true);
                        break;
                    case SyncService.MSG_FAV_STARTED:
                        p = findPreference("sync.favourites");
                        p.setSummary(R.string.sync_started);
                        p.setEnabled(false);
                        break;
                    case SyncService.MSG_FAV_FAILED:
                        p = findPreference("sync.favourites");
                        p.setSummary(R.string.sync_failed);
                        p.setEnabled(true);
                        break;
                    case SyncService.MSG_FAV_FINISHED:
                        p = findPreference("sync.favourites");
                        p.setSummary(R.string.sync_finished);
                        p.setEnabled(true);
                        break;
                }
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_sync);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Context context = getActivity();
            SyncHelper syncHelper = SyncHelper.get(context);
            Preference p = findPreference("sync.start");
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) context);

            p = findPreference("sync.history");
            long lastSync = syncHelper.getLastHistorySync();
            p.setSummary(context.getString(R.string.last_sync, lastSync == 0 ? context.getString(R.string.unknown) : AppHelper.getReadableDateTimeRelative(lastSync)));

            p = findPreference("sync.favourites");
            lastSync = syncHelper.getLastFavouritesSync();
            p.setSummary(context.getString(R.string.last_sync, lastSync == 0 ? context.getString(R.string.unknown) : AppHelper.getReadableDateTimeRelative(lastSync)));

            PreferencesUtils.bindPreferenceSummary(findPreference("sync.interval"));

            p = findPreference("sync.username");
            PreferencesUtils.bindPreferenceSummary(p);
            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) context);

            if (NetworkUtils.checkConnection(context)) {
                new LoadDevicesTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            Activity activity = getActivity();
            activity.registerReceiver(mEventReceiver, new IntentFilter(SyncService.SYNC_EVENT));
        }

        @Override
        public void onStop() {
            Activity activity = getActivity();
            activity.unregisterReceiver(mEventReceiver);
            super.onStop();
        }

        private static class LoadDevicesTask extends AsyncTask<Void, Void, ArrayList<SyncDevice>> {

            private final WeakReference<SyncSettingsFragment> mFragmentRef;

            LoadDevicesTask(SyncSettingsFragment fragment) {
                mFragmentRef = new WeakReference<>(fragment);
            }

            @Override
            protected ArrayList<SyncDevice> doInBackground(Void... voids) {
                try {
                    return SyncHelper.get(mFragmentRef.get().getActivity())
                            .getUserDevices(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(ArrayList<SyncDevice> devices) {
                super.onPostExecute(devices);
                SyncSettingsFragment f = mFragmentRef.get();
                if (f == null) {
                    return;
                }
                if (devices == null) {
                    View v = f.getView();
                    if (v != null) {
                        Snackbar.make(v, R.string.server_inaccessible, Snackbar.LENGTH_INDEFINITE).show();
                    } else {
                        Toast.makeText(f.getActivity(), R.string.server_inaccessible, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Context c = f.getActivity();
                    PreferenceScreen ps = f.getPreferenceScreen();
                    PreferenceCategory cat = new PreferenceCategory(c);
                    cat.setTitle(c.getString(R.string.sync_devices, devices.size()));
                    ps.addPreference(cat);
                    for (SyncDevice o : devices) {
                        Preference p = new Preference(c);
                        p.setTitle(o.name);
                        p.setSummary(AppHelper.getReadableDateTime(c, o.created_at));
                        p.setKey("sync.dev." + o.id);
                        if (c instanceof Preference.OnPreferenceClickListener) {
                            p.setOnPreferenceClickListener((Preference.OnPreferenceClickListener) c);
                        } else {
                            p.setSelectable(false);
                        }
                        cat.addPreference(p);
                    }
                }
            }
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
            final String login = mEditLogin.getText().toString().trim();
            final String password = mEditPassword.getText().toString().trim();
            if (login.isEmpty()) {
                LayoutUtils.showSoftKeyboard(mEditLogin);
                return;
            }
            if (password.isEmpty()) {
                LayoutUtils.showSoftKeyboard(mEditPassword);
                return;
            }
            LayoutUtils.hideSoftKeyboard(mEditPassword);
            new AuthTask((SyncSettingsActivity) getActivity(), view.getId() == R.id.buttonRegister)
                    .executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            login,
                            password
                    );
        }


        private static class AuthTask extends AsyncTask<String, Void, RESTResponse> implements DialogInterface.OnCancelListener {

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
            protected RESTResponse doInBackground(String... strings) {
                try {
                    SyncHelper syncHelper = SyncHelper.get(mActivityRef.get());
                    if (mRegister) {
                        return syncHelper.register(strings[0], strings[1]);
                    } else {
                        return syncHelper.authorize(strings[0], strings[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return RESTResponse.fromThrowable(e);
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
            protected void onPostExecute(RESTResponse restResponse) {
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
                    SyncService.start(activity);
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

    private static class DetachTask extends AsyncTask<Integer, Void, RESTResponse> {

        private final WeakReference<Preference> mPrefRef;

        DetachTask(Preference preference) {
            mPrefRef = new WeakReference<>(preference);
        }

        @Override
        protected RESTResponse doInBackground(Integer... integers) {
            try {
                return SyncHelper.get(mPrefRef.get().getContext()).detachDevice(integers[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return RESTResponse.fromThrowable(e);
            }
        }

        @Override
        protected void onPostExecute(RESTResponse restResponse) {
            super.onPostExecute(restResponse);
            Preference p = mPrefRef.get();
            if (p == null) {
                return;
            }
            if (restResponse.isSuccess()) {
                p.setEnabled(false);
                p.setSummary(R.string.device_detached);
                Toast.makeText(p.getContext(), R.string.device_detached, Toast.LENGTH_SHORT).show();
            } else {
                p.setSelectable(true);
                Toast.makeText(p.getContext(), restResponse.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class LogoutTask extends AsyncTask<Integer, Void, RESTResponse> {

        private final ProgressDialog mProgressDialog;
        private final WeakReference<SyncSettingsActivity> mActivityRef;

        LogoutTask(SyncSettingsActivity activity) {
            mActivityRef = new WeakReference<>(activity);
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage(activity.getString(R.string.loading));
            mProgressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected RESTResponse doInBackground(Integer... integers) {
            try {
                return SyncHelper.get(mActivityRef.get()).logout();
            } catch (Exception e) {
                e.printStackTrace();
                return RESTResponse.fromThrowable(e);
            }
        }

        @Override
        protected void onPostExecute(RESTResponse restResponse) {
            super.onPostExecute(restResponse);
            mProgressDialog.dismiss();
            SyncSettingsActivity a = mActivityRef.get();
            if (a == null) {
                return;
            }
            if (restResponse.isSuccess()) {
                a.getFragmentManager().beginTransaction()
                        .replace(R.id.content, new LoginFragment())
                        .commit();
            } else {
                Toast.makeText(a, restResponse.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
