package org.nv95.openmanga.components;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by nv95 on 26.10.15.
 */
public class TagSpan extends ClickableSpan {
    protected View.OnClickListener onClickListener;

    @Override
    public void onClick(View widget) {
        if (onClickListener != null) {
            onClickListener.onClick(widget);
        }
    }

    public TagSpan(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(Color.BLUE);
        ds.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public static Spanned ParseHtml(String html, View.OnClickListener onClickListener) {
        //SpannableStringBuilder builder = new SpannableStringBuilder();

        return null;
    }

    public static Spanned ParseString(String string, View.OnClickListener onClickListener) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] tags = string.split(" ");
        int t = 0;
        for (String o:tags) {
            builder.append(o);
            builder.setSpan(new TagSpan(onClickListener), t, builder.length(), Spanned.SPAN_MARK_MARK);
            t = builder.length();
            builder.append(" ");
        }
        return builder;
    }
}
