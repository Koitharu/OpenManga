package org.nv95.openmanga.feature.settings.sync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.settings.auth.AuthLoginFragment;
import org.nv95.openmanga.feature.settings.main.SettingsActivity2;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.items.SyncDevice;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.utils.AppHelper;
import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.feature.settings.util.PreferencesUtils;
import org.nv95.openmanga.utils.WeakAsyncTask;

import java.util.ArrayList;

/**
 * Created by admin on 24.07.17.
 */

public class SyncSettingsFragment extends PreferenceFragment {

    private final BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int what = intent.getIntExtra("what", -1);
            Preference p;
            switch (what) {
                case SyncService.MSG_UNAUTHORIZED:
                    Activity activity = getActivity();
                    if (activity != null && activity instanceof SettingsActivity2) {
                        Toast.makeText(activity, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                        ((SettingsActivity2) activity).openFragment(new AuthLoginFragment());
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_sync);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
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
            new LoadDevicesTask(this)
                    .attach((BaseAppActivity) context)
                    .start();
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

    private static class LoadDevicesTask extends WeakAsyncTask<SyncSettingsFragment, Void, Void, ArrayList<SyncDevice>> {


        LoadDevicesTask(SyncSettingsFragment syncSettingsFragment) {
            super(syncSettingsFragment);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected ArrayList<SyncDevice> doInBackground(Void... voids) {
            try {
                return SyncHelper.get(getObject().getActivity())
                        .getUserDevices(false);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(@NonNull SyncSettingsFragment f, ArrayList<SyncDevice> devices) {
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
