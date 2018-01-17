package org.nv95.openmanga.sync.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.sync.RESTResponse;
import org.nv95.openmanga.sync.SyncAuthenticator;
import org.nv95.openmanga.sync.SyncClient;
import org.nv95.openmanga.common.utils.network.NetworkUtils;

/**
 * Created by koitharu on 18.12.17.
 */

public class SyncAuthActivity extends AppCompatAuthActivity implements View.OnClickListener {

	private UserLoginTask mAuthTask = null;
	private TextInputLayout mLayoutPassword;
	private TextInputEditText mEditLogin;
	private TextInputEditText mEditPassword;
	private Button mButtonSignIn;
	private Button mButtonRegisterMode;
	private Button mButtonRegister;
	private Button mButtonBack;
	private View mViewProgress;
	private View mViewForm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_syncauth);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		enableHomeAsUp();

		mEditLogin = findViewById(R.id.editLogin);
		mEditPassword = findViewById(R.id.editPassword);
		mLayoutPassword = findViewById(R.id.inputLayoutPassword);
		mButtonSignIn = findViewById(R.id.buttonLogin);
		mButtonRegister = findViewById(R.id.buttonRegister);
		mButtonRegisterMode = findViewById(R.id.buttonRegisterMode);
		mButtonBack = findViewById(R.id.buttonBack);

		mEditPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
					attemptLogin(mButtonRegister.getVisibility() == View.VISIBLE);
					return true;
				}
				return false;
			}
		});

		mButtonRegister.setOnClickListener(this);
		mButtonSignIn.setOnClickListener(this);
		mButtonRegisterMode.setOnClickListener(this);
		mButtonBack.setOnClickListener(this);

		mViewForm = findViewById(R.id.login_form);
		mViewProgress = findViewById(R.id.login_progress);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.buttonLogin:
				attemptLogin(false);
				break;
			case R.id.buttonRegister:
				attemptLogin(true);
				break;
			case R.id.buttonRegisterMode:
				mButtonSignIn.setVisibility(View.GONE);
				mButtonRegister.setVisibility(View.VISIBLE);
				mButtonRegisterMode.setVisibility(View.GONE);
				mButtonBack.setVisibility(View.VISIBLE);
				break;
			case R.id.buttonBack:
				mButtonSignIn.setVisibility(View.VISIBLE);
				mButtonRegister.setVisibility(View.GONE);
				mButtonRegisterMode.setVisibility(View.VISIBLE);
				mButtonBack.setVisibility(View.GONE);
				break;
		}
	}

	private void attemptLogin(boolean wantRegister) {
		if (mAuthTask != null) {
			return;
		}
		mEditLogin.setError(null);
		mEditPassword.setError(null);
		mLayoutPassword.setError(null);

		String login = mEditLogin.getText().toString().trim();
		String password = mEditPassword.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (wantRegister && !TextUtils.isEmpty(password) && password.length() < 4) {
			mLayoutPassword.setError(getString(R.string.password_too_short));
			focusView = mEditPassword;
			cancel = true;
		}

		if (TextUtils.isEmpty(login)) {
			mEditLogin.setError(getString(R.string.login_required));
			focusView = mEditLogin;
			cancel = true;
		}

		if (cancel) {
			if (focusView != null) {
				focusView.requestFocus();
			}
		} else {
			showProgress(true);
			mAuthTask = new UserLoginTask(this, login, password, wantRegister);
			mAuthTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void showProgress(final boolean show) {

		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

		mViewForm.setVisibility(show ? View.GONE : View.VISIBLE);
		mViewForm.animate().setDuration(shortAnimTime).alpha(
				show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mViewForm.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		});

		mViewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
		mViewProgress.animate().setDuration(shortAnimTime).alpha(
				show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mViewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		});
	}

	private void finishLogin(String login, String password, String token) {
		final AccountManager accountManager = AccountManager.get(this);
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, login);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, SyncAuthenticator.ACCOUNT_TYPE);
		intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);
		final Account account = new Account(login, SyncAuthenticator.ACCOUNT_TYPE);
		accountManager.addAccountExplicitly(account, password, null);
		accountManager.setAuthToken(account, SyncAuthenticator.TOKEN_DEFAULT, token);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}


	public static class UserLoginTask extends WeakAsyncTask<SyncAuthActivity, Void, Void, RESTResponse> {

		private final String mLogin;
		private final String mPassword;
		private final boolean mWantRegister;

		UserLoginTask(SyncAuthActivity authActivity, String login, String password, boolean wantRegister) {
			super(authActivity);
			mLogin = login;
			mPassword = password;
			mWantRegister = wantRegister;
		}

		@Override
		protected RESTResponse doInBackground(Void... params) {
			return NetworkUtils.restQuery(
					BuildConfig.SYNC_URL + "/user",
					null,
					mWantRegister ? "PUT" : "POST",
					"login", mLogin,
					"password", mPassword,
					"device",
					SyncClient.getDeviceSummary()
			);
		}

		@Override
		protected void onPostExecute(@NonNull SyncAuthActivity authActivity, RESTResponse response) {
			authActivity.mAuthTask = null;
			authActivity.showProgress(false);

			if (response.isSuccess()) {
				try {
					final String token = response.getData().getString("token");
					authActivity.finishLogin(mLogin, mPassword, token);
				} catch (JSONException e) {
					e.printStackTrace();
					authActivity.mLayoutPassword.setError(authActivity.getString(R.string.error));
					authActivity.mEditPassword.requestFocus();
				}
			} else {
				authActivity.mLayoutPassword.setError(response.getMessage());
				authActivity.mEditPassword.requestFocus();
			}
		}


		@Override
		protected void onTaskCancelled(@NonNull SyncAuthActivity authActivity) {
			authActivity.mAuthTask = null;
			authActivity.showProgress(false);
		}
	}
}
