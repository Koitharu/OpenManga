package org.nv95.openmanga.preview.details;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.preview.PageHolder;

/**
 * Created by koitharu on 22.01.18.
 */

public final class DetailsPage extends PageHolder {

	public ImageView mImageViewCover;
	public TextView mTextViewSummary;
	public RatingBar mRatingBar;
	public TextView mTextViewGenres;
	public TextView mTextViewDescription;
	public Button mButtonRead;
	public ImageButton mButtonFavourite;

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
		mButtonRead = view.findViewById(R.id.button_read);
		mButtonFavourite = view.findViewById(R.id.button_favourite);

	}
}
