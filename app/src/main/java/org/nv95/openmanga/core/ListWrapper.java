package org.nv95.openmanga.core;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by koitharu on 12.01.18.
 */

public class ListWrapper<E> extends ObjectWrapper<ArrayList<E>> {

	public ListWrapper(ArrayList<E> object) {
		super(object);
	}

	public ListWrapper(Throwable error) {
		super(error);
	}

	public boolean isEmpty() {
		return mObject == null || mObject.isEmpty();
	}

	@NonNull
	public static ListWrapper badResult() {
		return new ListWrapper(new BadResultException());
	}

	public int size() {
		return mObject == null ? 0 : mObject.size();
	}
}
