package org.nv95.openmanga.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.nv95.openmanga.R;
import org.nv95.openmanga.items.MangaInfo;

import java.io.File;

/**
 * Created by nv95 on 28.01.16.
 */
public class ContentShareHelper {

    private final Context mContext;
    private final Intent mIntent;

    public ContentShareHelper(Context context) {
        mContext = context;
        mIntent = new Intent(Intent.ACTION_SEND);
    }

    public void share(MangaInfo manga) {
        mIntent.setType("text/plain");
        mIntent.putExtra(Intent.EXTRA_TEXT, manga.path);
        mIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, manga.name);
        mContext.startActivity(Intent.createChooser(mIntent, mContext.getString(R.string.action_share)));
    }

    public void shareImage(File file) {
        mIntent.setType("image/*");
        mIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        mContext.startActivity(Intent.createChooser(mIntent, mContext.getString(R.string.action_share)));
    }

    public void exportFile(File file) {
        mIntent.setType("file/*");
        mIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        mContext.startActivity(Intent.createChooser(mIntent, mContext.getString(R.string.export_file)));
    }
}
