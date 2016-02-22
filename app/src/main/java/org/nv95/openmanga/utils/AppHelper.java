package org.nv95.openmanga.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.text.format.DateUtils;
import android.view.View;

import org.nv95.openmanga.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by nv95 on 18.12.15.
 */
public class AppHelper {

    public static String getRawString(Context context, int res) {
        try {
            Resources resources = context.getResources();
            InputStream is = resources.openRawResource(res);
            String myText;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                baos.write(i);
                i = is.read();
            }
            myText = baos.toString();
            is.close();
            return myText;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    public static String getReadableDateTime(long milliseconds) {
        DateFormat formatter = SimpleDateFormat.getDateTimeInstance(); //new SimpleDateFormat("dd/MM/yyyy hh:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return formatter.format(calendar.getTime());
    }

    public static String getReadableDateTimeRelative(long milliseconds) {
        return DateUtils.getRelativeTimeSpanString(milliseconds, System.currentTimeMillis(), 0L,
                DateUtils.FORMAT_ABBREV_ALL).toString();
    }

    @Nullable
    public static File getFileFromUri(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int columnIndex = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return new File(cursor.getString(columnIndex));
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return new File(uri.getPath());
        }
        return null;
    }

    public static boolean showTip(View view, @StringRes int textId) {
        return showTip(view, view.getContext().getString(textId), "tip" + textId);
    }

    public static boolean showTip(View view, String text, final String key) {
        final SharedPreferences prefs = view.getContext().getSharedPreferences("tips", Context.MODE_PRIVATE);
        if (!prefs.getBoolean(key, false)) {
            Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.got_it, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            prefs.edit().putBoolean(key, true).apply();
                        }
                    }).show();
            return true;
        } else {
            return false;
        }
    }
}
