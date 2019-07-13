package org.nv95.openmanga.items;

import androidx.annotation.Nullable;

import org.nv95.openmanga.core.network.NetworkUtils;
import org.nv95.openmanga.helpers.SpeedMeasureHelper;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.staff.MangaProviderManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by nv95 on 12.02.16.
 */
public class SimpleDownload implements Runnable {

    private final String mSourceUrl;
    private final File mDestination;
    @Nullable
    private final Class<? extends MangaProvider> mProvider;
    @Nullable
    private SpeedMeasureHelper mSpeedMeasureHelper;

    public SimpleDownload(String sourceUrl, File destination) {
        this(sourceUrl, destination, null);
    }

    public SimpleDownload(String sourceUrl, File destination, @Nullable Class<? extends MangaProvider> provider) {
        this.mSourceUrl = sourceUrl;
        this.mDestination = destination;
        mProvider = provider;
    }

    public SimpleDownload setSpeedMeasureHelper(SpeedMeasureHelper helper) {
        mSpeedMeasureHelper = helper;
        return this;
    }

    @Override
    public void run() {
        InputStream input = null;
        OutputStream output = null;
        try {
            final OkHttpClient client = NetworkUtils.getHttpClient();
            final Request.Builder request = new Request.Builder().url(mSourceUrl).get();
            MangaProviderManager.prepareRequest(mSourceUrl, request, mProvider);
            input = client.newCall(request.build()).execute().body().byteStream();
            output = new FileOutputStream(mDestination);
            byte[] data = new byte[4096];
            int count;
            long total = 0;
            if (mSpeedMeasureHelper != null) {
                mSpeedMeasureHelper.reset();
            }
            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
            }
            if (mSpeedMeasureHelper != null && total > 0) {
                mSpeedMeasureHelper.measure(total);
            }
        } catch (Exception e) {
            if (mDestination.exists()) {
                mDestination.delete();
            }
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
        }
    }

    public boolean isSuccess() {
        return mDestination.exists();
    }

    public File getDestination() {
        return mDestination;
    }

    public String getSourceUrl() {
        return mSourceUrl;
    }
}
