package org.nv95.openmanga.ui.common;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

/**
 * Created by koitharu on 24.12.17.
 */

public class AppImageDownloader extends BaseImageDownloader {

	public AppImageDownloader(Context context) {
		super(context);
	}

	public AppImageDownloader(Context context, int connectTimeout, int readTimeout) {
		super(context, connectTimeout, readTimeout);
	}
}
