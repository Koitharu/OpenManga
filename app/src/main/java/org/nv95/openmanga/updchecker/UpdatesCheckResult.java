package org.nv95.openmanga.updchecker;

import android.support.annotation.Nullable;

import org.nv95.openmanga.core.models.MangaUpdateInfo;

import java.util.ArrayList;

public class UpdatesCheckResult {

	private final ArrayList<MangaUpdateInfo> mUpdates = new ArrayList<>();
	private int mFails = 0;
	@Nullable
	private Exception mException = null;

	public int getNewChaptersCount() {
		int total = 0;
		for (MangaUpdateInfo o : mUpdates) {
			total += o.newChapters;
		}
		return total;
	}

	public int getFails() {
		return mFails;
	}

	public boolean isSuccess() {
		return mException == null && (mFails == 0 || !mUpdates.isEmpty());
	}

	@Nullable
	public Exception getError() {
		return mException;
	}

	public ArrayList<MangaUpdateInfo> getUpdates() {
		return mUpdates;
	}

	public void add(MangaUpdateInfo info) {
		mUpdates.add(info);
	}

	public void fail() {
		mFails++;
	}

	public void error(Exception e) {
		mException = e;
	}
}
