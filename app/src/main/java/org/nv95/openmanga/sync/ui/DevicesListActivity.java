package org.nv95.openmanga.sync.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.WeakAsyncTask;
import org.nv95.openmanga.content.RESTResponse;
import org.nv95.openmanga.sync.SyncAuthenticator;
import org.nv95.openmanga.sync.SyncClient;
import org.nv95.openmanga.sync.SyncDevice;
import org.nv95.openmanga.ui.AppBaseActivity;

import java.util.ArrayList;

/**
 * Created by koitharu on 18.12.17.
 */

public class DevicesListActivity extends AppBaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<SyncDevice>>,DevicesAdapter.OnItemClickListener {

	private AccountManager mAccountManager;
	private Account mAccount;

	private RecyclerView mRecyclerView;
	private ProgressBar mProgressBar;
	private TextView mTextViewHolder;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices);
		setSupportActionBar(R.id.toolbar);
		enableHomeAsUp();

		mRecyclerView = findViewById(R.id.recyclerView);
		mProgressBar = findViewById(R.id.progressBar);
		mTextViewHolder = findViewById(R.id.textView_holder);

		mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

		mAccountManager = AccountManager.get(this);
		Account[] accounts = mAccountManager.getAccountsByType(SyncAuthenticator.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			finish();
			return;
		}
		mAccount = accounts[0];

		getLoaderManager().initLoader(0, null, this);
		getLoaderManager().getLoader(0).forceLoad();
	}


	@Override
	public Loader<ArrayList<SyncDevice>> onCreateLoader(int i, Bundle bundle) {
		AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(mAccount, SyncAuthenticator.TOKEN_DEFAULT, null, this, null,null);
		return new DevicesLoader(this, future);
	}

	@Override
	public void onLoadFinished(Loader<ArrayList<SyncDevice>> loader, ArrayList<SyncDevice> syncDevices) {
		mProgressBar.setVisibility(View.GONE);
		if (syncDevices.isEmpty()) {
			mTextViewHolder.setVisibility(View.VISIBLE);
		} else {
			DevicesAdapter adapter = new DevicesAdapter(syncDevices, this);
			mRecyclerView.setAdapter(adapter);
		}
	}

	@Override
	public void onLoaderReset(Loader<ArrayList<SyncDevice>> loader) {

	}

	@Override
	public void onItemClick(final SyncDevice item) {
		new AlertDialog.Builder(this)
				.setMessage(getString(R.string.device_detach_confirm, item.name))
				.setPositiveButton(R.string.detach, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						mProgressBar.setVisibility(View.VISIBLE);
						new DeviceDetachTask(DevicesListActivity.this)
								.start(item.id);
					}
				})
				.setNegativeButton(android.R.string.cancel, null)
				.create().show();
	}

	private static class DevicesLoader extends AsyncTaskLoader<ArrayList<SyncDevice>> {

		private final AccountManagerFuture<Bundle> mAccountFuture;

		DevicesLoader(Context context, AccountManagerFuture<Bundle> accountFuture) {
			super(context);
			mAccountFuture = accountFuture;
		}

		@Override
		public ArrayList<SyncDevice> loadInBackground() {
			try {
				String token = mAccountFuture.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				return new SyncClient(token).getAttachedDevices();
			} catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
	}

	private static class DeviceDetachTask extends WeakAsyncTask<DevicesListActivity,Integer, Void, RESTResponse> {

		private final AccountManagerFuture<Bundle> mAccountFuture;

		DeviceDetachTask(DevicesListActivity activity) {
			super(activity);
			mAccountFuture = activity.mAccountManager.getAuthToken(activity.mAccount, SyncAuthenticator.TOKEN_DEFAULT,
					null, activity, null,null);
		}

		@Override
		protected RESTResponse doInBackground(Integer... integers) {
			try {
				String token = mAccountFuture.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				return new SyncClient(token).detachDevice(integers[0]);
			} catch (Exception e) {
				e.printStackTrace();
				return RESTResponse.fromThrowable(e);
			}
		}

		@Override
		protected void onPostExecute(@NonNull DevicesListActivity activity, RESTResponse restResponse) {
			if (restResponse.isSuccess()) {
				activity.getLoaderManager().getLoader(0).onContentChanged();
				Snackbar.make(activity.mRecyclerView, R.string.device_detached, Snackbar.LENGTH_SHORT).show();
			} else {
				activity.mProgressBar.setVisibility(View.GONE);
				Snackbar.make(activity.mRecyclerView, restResponse.getMessage(), Snackbar.LENGTH_SHORT).show();
			}
		}
	}
}
