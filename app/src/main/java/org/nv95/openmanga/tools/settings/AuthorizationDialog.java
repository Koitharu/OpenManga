package org.nv95.openmanga.tools.settings;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.WeakAsyncTask;
import org.nv95.openmanga.core.ObjectWrapper;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.utils.network.CookieStore;

/**
 * Created by koitharu on 12.01.18.
 */

public final class AuthorizationDialog extends AppCompatDialogFragment implements View.OnClickListener,
		Handler.Callback {

	private ProgressBar mProgressBar;
	private TextInputLayout mInputLayoutLogin;
	private TextInputLayout mInputLayoutPassword;
	private TextInputEditText mEditTextLogin;
	private TextInputEditText mEditTextPassword;
	private Button mButtonLogin;
	private Button mButtonCancel;

	private String mProviderCName;
	@Nullable
	private AuthTask mAuthTask;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		assert args != null;
		mProviderCName = args.getString("provider");
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_authorization, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mProgressBar = view.findViewById(R.id.progressBar);
		mInputLayoutLogin = view.findViewById(R.id.inputLayout_login);
		mInputLayoutPassword = view.findViewById(R.id.inputLayout_password);
		mEditTextLogin = view.findViewById(R.id.edit_login);
		mEditTextPassword = view.findViewById(R.id.edit_password);
		mButtonLogin = view.findViewById(R.id.button_login);
		mButtonCancel = view.findViewById(R.id.button_cancel);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mButtonCancel.setOnClickListener(this);
		mButtonLogin.setOnClickListener(this);
		mEditTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
					onClick(mButtonLogin);
					return true;
				}
				return false;
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_cancel:
				dismiss();
				break;
			case R.id.button_login:
				mInputLayoutPassword.setError(null);
				final String login = mEditTextLogin.getText().toString().trim();
				final String password = mEditTextPassword.getText().toString();
				if (login.length() == 0) {
					mEditTextLogin.requestFocus();
					return;
				}
				if (password.length() == 0) {
					mEditTextPassword.requestFocus();
					return;
				}
				if (mAuthTask != null && mAuthTask.canCancel()) {
					mAuthTask.cancel(true);
				}
				mAuthTask = new AuthTask(this);
				mAuthTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mProviderCName, login, password);
				break;
		}
	}

	private void setIsReady(boolean isReady) {
		mButtonLogin.setEnabled(isReady);
		mButtonCancel.setEnabled(isReady);
		mInputLayoutLogin.setEnabled(isReady);
		mInputLayoutPassword.setEnabled(isReady);
		mEditTextLogin.setEnabled(isReady);
		mEditTextPassword.setEnabled(isReady);
		mProgressBar.setVisibility(isReady ? View.GONE : View.VISIBLE);
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == 1) {
			final Activity activity = getActivity();
			if (activity != null && activity instanceof Callback) {
				((Callback) activity).onAuthorized();
			}
			dismiss();
			return true;
		}
		return false;
	}

	private static final class AuthTask extends WeakAsyncTask<AuthorizationDialog,String,Void,ObjectWrapper<String>> {

		AuthTask(AuthorizationDialog authorizationDialog) {
			super(authorizationDialog);
		}

		@Override
		protected void onPreExecute(@NonNull AuthorizationDialog authorizationDialog) {
			authorizationDialog.setIsReady(false);
		}

		@Override
		protected ObjectWrapper<String> doInBackground(String... strings) {
			try {
				@SuppressWarnings("ConstantConditions")
				final MangaProvider provider = MangaProvider.get(getObject().getContext(), strings[0]);
				String result = provider.authorize(strings[1], strings[2]);
				return result == null ? ObjectWrapper.<String>badObject() : new ObjectWrapper<>(result);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(@NonNull AuthorizationDialog authorizationDialog, @NonNull ObjectWrapper<String> data) {
			authorizationDialog.setIsReady(true);
			authorizationDialog.mAuthTask = null;
			if (data.isFailed()) {
				authorizationDialog.mInputLayoutPassword.setError(
						data.getError() instanceof ObjectWrapper.BadResultException ?
								authorizationDialog.getString(R.string.auth_failed)
								: authorizationDialog.getString(ErrorUtils.getErrorMessage(data.getError()))
				);
			} else {
				CookieStore.getInstance().put(MangaProvider.getDomain(authorizationDialog.mProviderCName), data.get());
				final Activity activity = authorizationDialog.getActivity();
				if (activity != null && activity instanceof Callback) {
					((Callback) activity).onAuthorized();
				}
				authorizationDialog.dismiss();
			}
		}
	}

	public interface Callback {

		void onAuthorized();
	}
}
