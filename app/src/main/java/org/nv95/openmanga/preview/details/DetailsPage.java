package org.nv95.openmanga.preview.details;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.common.utils.ErrorUtils;
import org.nv95.openmanga.common.utils.ImageUtils;
import org.nv95.openmanga.common.utils.TextUtils;
import org.nv95.openmanga.core.MangaStatus;
import org.nv95.openmanga.core.models.MangaDetails;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.preview.PageHolder;

/**
 * Created by koitharu on 22.01.18.
 */

public final class DetailsPage extends PageHolder {

	private ImageView mImageViewCover;
	private TextView mTextViewSummary;
	private RatingBar mRatingBar;
	private TextView mTextViewGenres;
	private TextView mTextViewDescription;

	public Button buttonRead;
	public ImageButton buttonFavourite;

	public DetailsPage(@NonNull ViewGroup parent) {
		super(parent, R.layout.page_manga_details);
	}

	@Override
	protected void onViewCreated(@NonNull View view) {
		mImageViewCover = view.findViewById(R.id.imageView_cover);
		mTextViewSummary = view.findViewById(R.id.textView_summary);
		mRatingBar = view.findViewById(R.id.ratingBar);
		mTextViewDescription = view.findViewById(R.id.textView_description);
		mTextViewGenres = view.findViewById(R.id.textView_genres);
		buttonRead = view.findViewById(R.id.button_read);
		buttonFavourite = view.findViewById(R.id.button_favourite);
	}

	public void updateContent(@NonNull MangaHeader mangaHeader, @Nullable MangaDetails mangaDetails) {
		final MangaProvider provider = MangaProvider.get(getContext(), mangaHeader.provider);
		if (mangaDetails == null) { //full info wasn't loaded yet
			ImageUtils.setThumbnail(mImageViewCover, mangaHeader.thumbnail, MangaProvider.getDomain(mangaHeader.provider));
			mTextViewGenres.setText(mangaHeader.genres);
			if (mangaHeader.rating == 0) {
				mRatingBar.setVisibility(View.GONE);
			} else {
				mRatingBar.setVisibility(View.VISIBLE);
				mRatingBar.setRating(mangaHeader.rating / 20);
			}
			mTextViewSummary.setText(formatSummary(null, -1, provider.getName(), mangaHeader.status));
		} else {
			ImageUtils.updateImage(mImageViewCover, mangaDetails.cover, MangaProvider.getDomain(mangaDetails.provider));
			mTextViewGenres.setText(mangaDetails.genres);
			mTextViewDescription.setText(TextUtils.fromHtmlCompat(mangaDetails.description));
			if (mangaDetails.rating != 0) {
				mRatingBar.setVisibility(View.VISIBLE);
				mRatingBar.setRating(mangaDetails.rating / 20);
			}
			mTextViewSummary.setText(formatSummary(mangaDetails.author, mangaDetails.chapters.size(), provider.getName(), mangaDetails.status));
		}
	}

	public void setError(@Nullable Throwable error) {
		mTextViewDescription.setText(ErrorUtils.getErrorMessage(error));
	}

	@NonNull
	private CharSequence formatSummary(@Nullable String author, int chapters, String provider, @MangaStatus int status) {
		final StringBuilder builder = new StringBuilder();
		if (!android.text.TextUtils.isEmpty(author)) {
			builder.append("<b>").append(getString(R.string.author_)).append("</b> ");
			builder.append(author).append("<br/>");
		}
		builder.append("<b>").append(getString(R.string.chapters_count_)).append("</b> ");
		if (chapters == -1) {
			builder.append("?");
		} else {
			builder.append(chapters);
		}
		builder.append("<br/>").append("<b>").append(getString(R.string.source_)).append("</b> ");
		builder.append(provider);
		switch (status) {
			case MangaStatus.STATUS_COMPLETED:
				builder.append("<br/>").append(getString(R.string.status_completed));
				break;
			case MangaStatus.STATUS_ONGOING:
				builder.append("<br/>").append(getString(R.string.status_ongoing));
				break;
			case MangaStatus.STATUS_UNKNOWN:
				break;
		}
		return TextUtils.fromHtmlCompat(builder.toString());
	}
}
