package org.nv95.openmanga.common;

public final class TwoLevelProgress {

	private int mFirstMax = 0, mSecondMax = 0;
	private int mFirstPos = 0, mSecondPos = 0;

	public void setFirstMax(int max) {
		mFirstMax = max;
	}

	public void setSecondMax(int max) {
		mSecondMax = max;
	}

	public void setFirstPos(int pos) {
		mFirstPos = pos;
	}

	public void setSecondPos(int pos) {
		mSecondPos = pos;
	}

	public int getPercent() {
		if (mFirstPos > mFirstMax || mSecondPos > mSecondMax || (mSecondPos == mSecondMax && mFirstPos > 0)) {
			throw new IllegalArgumentException();
		}
		double firstPercent = mFirstPos * 100.0 / mFirstMax;
		if (mSecondMax == 0) {
			return (int) firstPercent;
		}
		return (int)((mSecondPos * 100.0 / mSecondMax) + (firstPercent / mSecondMax));
	}
}
