package org.nv95.openmanga.common.views.preferences;

import android.content.Context;
import android.os.Build;
import android.preference.MultiSelectListPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.storage.db.CategoriesRepository;
import org.nv95.openmanga.core.storage.db.CategoriesSpecification;

import java.util.ArrayList;

/**
 * TODO
 */
public final class CategoriesSelectPreference extends MultiSelectListPreference {

	public CategoriesSelectPreference(Context context) {
		super(context);
		init(context);
	}

	public CategoriesSelectPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public CategoriesSelectPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public CategoriesSelectPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		final ArrayList<Category> categories = CategoriesRepository.get(context).query(new CategoriesSpecification().orderByDate(true));
		final int length = categories == null ? 0 : categories.size();
		final CharSequence[] entries = new CharSequence[length];
		final CharSequence[] entryValues = new CharSequence[length];
		for (int i = 0; i < length; i++) {
			final Category o = categories.get(i);
			entries[i] = o.name;
			entryValues[i] = String.valueOf(o.id);
		}
		setEntries(entries);
		setEntryValues(entryValues);
	}

	/*@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setNeutralButton(R.string.all, this);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_NEUTRAL) {
			if (dialog instanceof AlertDialog) {
				LayoutUtils.checkAll(((AlertDialog) dialog).getListView());
			}
		} else {
			super.onClick(dialog, which);
		}
	}*/
}
