package org.nv95.openmanga.common.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.Category;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaFavourite;
import org.nv95.openmanga.core.storage.db.CategoriesRepository;
import org.nv95.openmanga.core.storage.db.CategoriesSpecification;
import org.nv95.openmanga.core.storage.db.FavouritesRepository;

import java.util.ArrayList;

/**
 * Created by koitharu on 19.01.18.
 */

public final class FavouriteDialog implements DialogInterface.OnClickListener {

	private final AlertDialog.Builder mBuilder;
	private final FavouritesRepository mFavouritesRepository;
	private final ArrayList<Category> mCategories;
	private final MangaDetails mManga;
	@Nullable
	private OnFavouriteListener mListener;

	public FavouriteDialog(@NonNull Context context, @NonNull MangaDetails manga) {
		mListener = null;
		mBuilder = new AlertDialog.Builder(context);
		mFavouritesRepository = new FavouritesRepository(context);
		mManga = manga;
		final CategoriesRepository categoriesRepository = new CategoriesRepository(context);
		mCategories = categoriesRepository.query(new CategoriesSpecification().orderByDate(false));
		final MangaFavourite favourite = mFavouritesRepository.get(manga);
		final String[] items = new String[mCategories.size()];
		int selected = -1;
		for (int i = 0; i < mCategories.size(); i++) {
			Category o = mCategories.get(i);
			items[i] = o.name;
			if (favourite != null && favourite.categoryId == o.id) {
				selected = i;
			}
		}
		mBuilder.setSingleChoiceItems(items, selected, this);
		mBuilder.setTitle(R.string.action_favourite);
		mBuilder.setPositiveButton(android.R.string.ok, this);
		mBuilder.setNegativeButton(android.R.string.cancel, this);
		mBuilder.setNeutralButton(R.string.remove, this);
		mBuilder.setCancelable(true);
	}

	public FavouriteDialog setListener(@Nullable OnFavouriteListener listener) {
		mListener = listener;
		return this;
	}

	public void show() {
		mBuilder.create().show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:

				break;
			case DialogInterface.BUTTON_NEGATIVE:

				break;
			case DialogInterface.BUTTON_NEUTRAL:
				mFavouritesRepository.remove(mManga);
				if (mListener != null) {
					mListener.onFavouritesChanged(mManga, null);
				}
				break;
			default:
				final Category category = mCategories.get(which);
				final MangaFavourite favourite = MangaFavourite.from(
						mManga,
						category.id,
						mManga.chapters.size()
				);
				if (!mFavouritesRepository.update(favourite)) {
					mFavouritesRepository.add(favourite);
				}
				if (mListener != null) {
					mListener.onFavouritesChanged(mManga, category);
				}
		}
	}

	public interface OnFavouriteListener {

		void onFavouritesChanged(MangaDetails manga, @Nullable Category category);
	}
}
