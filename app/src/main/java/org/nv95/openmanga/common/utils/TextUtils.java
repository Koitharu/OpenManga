package org.nv95.openmanga.common.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Created by koitharu on 24.12.17.
 */

public abstract class TextUtils {

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

	@NonNull
	public static String formatFileSize(long size) {
		if(size <= 0) return "0 B";
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
		final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(' ');
		return new DecimalFormat("#,##0.#", symbols).format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
