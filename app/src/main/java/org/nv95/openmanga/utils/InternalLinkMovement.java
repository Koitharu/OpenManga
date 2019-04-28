package org.nv95.openmanga.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.Nullable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 12.01.16.
 */
public class InternalLinkMovement extends LinkMovementMethod {

    @Nullable
    private final OnLinkClickListener mLinkClickListener;

    public InternalLinkMovement(@Nullable OnLinkClickListener linkClickListener) {
        this.mLinkClickListener = linkClickListener;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    Selection.removeSelection(buffer);
                    processClick(widget.getContext(), link[0].getURL(), widget);
                } else {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    private void processClick(Context context, String url, TextView textView) {
        String[] parts = url.split(":");
        if (parts.length != 2) {
            FileLogger.getInstance().report("Invalid link: " + url);
            return;
        }
        switch (parts[0]) {
            case "activity":
                String packageName = context.getPackageName();
                context.startActivity(new Intent().setComponent(new ComponentName(
                        packageName, packageName + "." + parts[1]
                )));
                break;
            case "http":
            case "https":
                context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
                break;
            case "mailto":
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]
                        {parts[1]});
                context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_mail)));
                break;
            case "settings":
                switch (parts[1]) {
                    case "wifi":
                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        break;
                    case "network":
                        context.startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                        break;
                }
                break;
            default:
                if (mLinkClickListener != null) {
                    mLinkClickListener.onLinkClicked(textView, parts[0], parts[1]);
                }
        }
    }

    public interface OnLinkClickListener {
        void onLinkClicked(TextView view, String scheme, String url);
    }
}
