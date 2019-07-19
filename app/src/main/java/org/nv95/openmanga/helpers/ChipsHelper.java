package org.nv95.openmanga.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.search.SearchActivity;

public abstract class ChipsHelper {

	private static final View.OnClickListener mGenreClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v instanceof Chip) {
				final String genre = ((Chip) v).getText().toString();
				final Context context = v.getContext();
				//TODO use hashtags
				context.startActivity(new Intent(context, SearchActivity.class).putExtra("query", genre));
			}
		}
	};

	public static void fillGenres(ChipGroup group, String genresString) {
		final String[] genres = genresString.split("[,\\s]+");
		group.removeAllViews();
		for (String genre : genres) {
			final Chip chip = new Chip(group.getContext());
			chip.setCheckable(false);
			chip.setChipBackgroundColorResource(R.color.light_gray);
			chip.setOnClickListener(mGenreClickListener);
			chip.setText(genre);
			group.addView(chip);
		}
	}
}
