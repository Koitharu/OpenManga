package org.nv95.openmanga.storage.downloaders;

import android.support.annotation.NonNull;

import org.nv95.openmanga.common.utils.network.NetworkUtils;
import org.nv95.openmanga.core.models.MangaPage;
import org.nv95.openmanga.core.providers.MangaProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by koitharu on 25.01.18.
 */

public class SimplePageDownloader extends Downloader<MangaPage> {

	private final MangaProvider mProvider;

	public SimplePageDownloader(@NonNull MangaPage source, @NonNull File destination, MangaProvider provider) {
		super(source, destination);
		mProvider = provider;
	}

	@Override
	protected boolean onDownload(@NonNull MangaPage source, @NonNull File destination) {
		InputStream input = null;
		FileOutputStream output = null;
		try {
			final String pageUrl = mProvider.getImageUrl(source);
			final String domain = MangaProvider.getDomain(source.provider);
			final Request request = new Request.Builder()
					.url(pageUrl)
					.header(NetworkUtils.HEADER_USER_AGENT, NetworkUtils.USER_AGENT_DEFAULT)
					.header(NetworkUtils.HEADER_REFERER, "http://" + domain)
					.get()
					.build();
			final Response response = NetworkUtils.getHttpClient().newCall(request).execute();
			if (!response.isSuccessful()) {
				return false;
			}
			//noinspection ConstantConditions
			input = createInputStream(response.body().byteStream());
			output = new FileOutputStream(destination);
			final byte[] buffer = new byte[512];
			int length;
			while ((length = input.read(buffer)) >= 0) {
				output.write(buffer, 0, length);
				while (isPaused() && !isCancelled()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException ignored) {
					}
				}
				if (isCancelled()) {
					output.close();
					output = null;
					destination.delete();
					return false;
				}
			}
			output.flush();
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		} finally {
			if (input != null) try {
				input.close();
			} catch (IOException ignored) {
			}
			if (output != null) try {
				output.close();
			} catch (IOException ignored) {
			}
		}
	}

	@NonNull
	protected InputStream createInputStream(InputStream delegate) {
		return new BufferedInputStream(delegate);
	}
}
