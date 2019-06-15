package org.nv95.openmanga.core.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by admin on 23.07.17.
 */

public class NetworkStateListener extends BroadcastReceiver {

    private final OnNetworkStateListener mListener;

    public NetworkStateListener(OnNetworkStateListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mListener.onNetworkStatusChanged(activeNetwork != null && activeNetwork.isConnected());
    }

    public interface OnNetworkStateListener {
        void onNetworkStatusChanged(boolean isConnected);
    }
}
