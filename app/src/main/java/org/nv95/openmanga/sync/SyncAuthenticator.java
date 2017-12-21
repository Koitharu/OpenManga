package org.nv95.openmanga.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.sync.ui.SyncAuthActivity;

/**
 * Created by koitharu on 18.12.17.
 */

public class SyncAuthenticator extends AbstractAccountAuthenticator {

	public static final String ACCOUNT_TYPE = "org.nv95.openmanga.SYNC";
	public static final String TOKEN_DEFAULT = "default";

	private final Context mContext;
	private final Handler mHandler;

	SyncAuthenticator(Context context) {
		super(context);
		mContext = context;
		mHandler = new Handler();
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
		return null;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		if (isAccountExists()) {
			final Bundle result = new Bundle();
			result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION);
			result.putString(AccountManager.KEY_ERROR_MESSAGE, mContext.getString(R.string.allowed_only_one_account));
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mContext, R.string.allowed_only_one_account, Toast.LENGTH_SHORT).show();
				}
			});
			return result;
		}

		final Intent intent = new Intent(mContext, SyncAuthActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

		// Extract the username and password from the Account Manager, and ask
		// the server for an appropriate AuthToken.
		final AccountManager am = AccountManager.get(mContext);

		String authToken = am.peekAuthToken(account, authTokenType);

		// Lets give another try to authenticate the user
		if (TextUtils.isEmpty(authToken)) {
			final String password = am.getPassword(account);
			if (password != null) {
				authToken = SyncClient.authenticate(account.name, password);
			}
		}

		// If we get an authToken - we return it
		if (!TextUtils.isEmpty(authToken)) {
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
			return result;
		}

		return addAccount(response, account.type, authTokenType, new String[0], options);
	}


	@Override
	public String getAuthTokenLabel(String s) {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
		return null;
	}

	private boolean isAccountExists() {
		return AccountManager.get(mContext).getAccountsByType(ACCOUNT_TYPE).length != 0;
	}
}
