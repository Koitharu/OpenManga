package org.nv95.openmanga.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
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

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

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

    public static void showcaseTip(Activity activity, @IdRes int viewId, @StringRes int textId, String key) {
        showcaseTip(activity, activity.findViewById(viewId), activity.getString(textId), key);
    }

    public static void showcaseTip(Activity activity, View view, @StringRes int textId, String key) {
        showcaseTip(activity, view, activity.getString(textId), key);
    }

    public static void showcaseTip(Activity activity, View view, String text, String key) {
        new MaterialShowcaseView.Builder(activity)
                .setTarget(view)
                .setDismissText(activity.getString(R.string.got_it).toUpperCase())
                .setContentText(text)
                .setDelay(1000) // optional but starting animations immediately in onCreate can make them choppy
                .singleUse(key) // provide a unique ID used to ensure it is only shown once
                .setDismissTextColor(ContextCompat.getColor(activity, R.color.accent_light))
                .show();
    }

    public static CharSequence[] getTextArray(Context context, int[] ids) {
        CharSequence[] res = new CharSequence[ids.length];
        for (int i = 0; i < ids.length; i++) {
            res[i] = context.getText(ids[i]);
        }
        return res;
    }
}
