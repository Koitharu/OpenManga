package org.nv95.openmanga.utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

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
            String res = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int columnIndex = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    res = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                // Eat it
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (res != null) {
                return new File(res);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return new File(uri.getPath());
        }
        return null;
    }

    public static String[] getStringArray(Context context, int[] ids) {
        String[] res = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            res[i] = context.getString(ids[i]);
        }
        return res;
    }

    public static String ellipsize(String str, int len) {
        if (str.length() <= len) {
            return str;
        } else {
            return str.substring(0, len - 1) + '…';
        }
    }

    public static Spanned spannedToUpperCase(@NonNull Spanned s) {
        Object[] spans = s.getSpans(0,
                s.length(), Object.class);
        SpannableString spannableString = new SpannableString(s.toString().toUpperCase());

        // reapply the spans to the now uppercase string
        for (Object span : spans) {
            spannableString.setSpan(span,
                    s.getSpanStart(span),
                    s.getSpanEnd(span),
                    0);
        }

        return spannableString;
    }

    public static Spanned fromHtml(String html, boolean allCaps) {
        Spanned spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(html);
        }
        if (allCaps) {
            spanned = spannedToUpperCase(spanned);
        }
        return spanned;
    }

    /**
     * Ignore nulls
     */
    @NonNull
    public static String concatStr(String... args) {
        final StringBuilder builder = new StringBuilder();
        for (String o : args) {
            if (o != null) {
                builder.append(o);
            }
        }
        return builder.toString();
    }
    
    public static boolean isEnglish(CharSequence str) {
        final Pattern pattern = Pattern.compile("^[A-Za-z0-9. -]+$");
        return pattern.matcher(str).matches();
    }
}
