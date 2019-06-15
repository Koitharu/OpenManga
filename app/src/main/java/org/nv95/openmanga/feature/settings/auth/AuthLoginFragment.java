package org.nv95.openmanga.feature.settings.auth;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.activities.BaseAppActivity;
import org.nv95.openmanga.feature.settings.main.SettingsActivity2;
import org.nv95.openmanga.feature.settings.sync.SyncSettingsFragment;
import org.nv95.openmanga.helpers.SyncHelper;
import org.nv95.openmanga.items.RESTResponse;
import org.nv95.openmanga.services.SyncService;
import org.nv95.openmanga.utils.LayoutUtils;
import org.nv95.openmanga.utils.ProgressAsyncTask;

/**
 * Created by admin on 24.07.17.
 */

public class AuthLoginFragment extends Fragment implements View.OnClickListener {

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
        new AuthTask((SettingsActivity2) getActivity(), view.getId() == R.id.buttonRegister)
                .executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        login,
                        password
                );
    }


    private static class AuthTask extends ProgressAsyncTask<String, Void, RESTResponse> implements DialogInterface.OnCancelListener {


        private final boolean mRegister;

        AuthTask(SettingsActivity2 activity, boolean isRegister) {
            super(activity);
            mRegister = isRegister;
        }


        @Override
        protected RESTResponse doInBackground(String... strings) {
            try {
                SyncHelper syncHelper = SyncHelper.get(getActivity());
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
        protected void onPostExecute(@NonNull BaseAppActivity activity, RESTResponse restResponse) {
            if (restResponse.isSuccess()) {
                Toast.makeText(activity, R.string.successfully, Toast.LENGTH_SHORT).show();
                ((SettingsActivity2)activity).openFragment(new SyncSettingsFragment());
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
