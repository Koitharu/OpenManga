package org.nv95.openmanga.common.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;

/**
 * Created by koitharu on 24.12.17.
 */

public final class TextUtils {

	@NonNull
	public static String notNull(@Nullable String str) {
		return str == null ? "" : str;
	}

	@NonNull
	public static String concatIgnoreNulls(String... args) {
		final StringBuilder builder = new StringBuilder();
		for (String o : args) {
			if (o != null) {
				builder.append(o);
			}
		}
		return builder.toString();
	}

	public static Spanned fromHtmlCompat(String html) {
		Spanned spanned;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
		} else {
			spanned = Html.fromHtml(html);
		}
		return spanned;
	}

	public static Spanned toUpperCase(@NonNull Spanned s) {
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

	public static String ellipsize(String string, int maxLength) {
		return string.length() <= maxLength ? string : string.substring(0, maxLength - 1) + '…';
	}

	public static String inline(String string) {
		return string.replaceAll("\\s+", " ");
	}
}
