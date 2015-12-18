package org.nv95.openmanga.components;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by nv95 on 26.10.15.
 */
public class TagSpan extends ClickableSpan {
    protected OnTagClickListener onClickListener;
    private String tag;

    public interface OnTagClickListener {
        void onTagClick(String tag);
    }

    @Override
    public void onClick(View widget) {
        if (onClickListener != null) {
            onClickListener.onTagClick(tag.trim());
        }
    }

    public TagSpan(String tag, OnTagClickListener onClickListener) {
        this.onClickListener = onClickListener;
        this.tag = tag;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.bgColor = Color.BLUE;
        ds.setColor(Color.WHITE);
        ds.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public OnTagClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnTagClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Nullable
    public static Spanned ParseString(@Nullable String string, OnTagClickListener onClickListener) {
        if (string == null || string.length() == 0) {
            return null;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] tags = string.replace(",","").split(" ");
        int t = 0;
        for (String o:tags) {
            builder.append(' ').append(o).append(' ');
            builder.setSpan(new TagSpan(o, onClickListener), t, builder.length(), Spanned.SPAN_MARK_MARK);
            builder.append(" ");
            t = builder.length();
        }
        return builder;
    }
}
