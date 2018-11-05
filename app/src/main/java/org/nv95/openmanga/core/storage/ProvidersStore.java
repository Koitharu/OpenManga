package org.nv95.openmanga.core.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseBooleanArray;

import org.nv95.openmanga.common.utils.CollectionsUtils;
import org.nv95.openmanga.core.models.ProviderHeader;
import org.nv95.openmanga.core.providers.DesumeProvider;
import org.nv95.openmanga.core.providers.ExhentaiProvider;
import org.nv95.openmanga.core.providers.MangaFoxProvider;
import org.nv95.openmanga.core.providers.MintmangaProvider;
import org.nv95.openmanga.core.providers.NudeMoonProvider;
import org.nv95.openmanga.core.providers.ReadmangaruProvider;
import org.nv95.openmanga.core.providers.SelfmangaProvider;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by koitharu on 17.01.18.
 */

public final class ProvidersStore {

	private static final ProviderHeader[] sProviders = new ProviderHeader[]{
			new ProviderHeader(ReadmangaruProvider.CNAME, ReadmangaruProvider.DNAME),	//0
			new ProviderHeader(MintmangaProvider.CNAME, MintmangaProvider.DNAME),		//1
			new ProviderHeader(DesumeProvider.CNAME, DesumeProvider.DNAME),				//2
			new ProviderHeader(ExhentaiProvider.CNAME, ExhentaiProvider.DNAME),			//3
			new ProviderHeader(SelfmangaProvider.CNAME, SelfmangaProvider.DNAME),		//4
			new ProviderHeader(NudeMoonProvider.CNAME, NudeMoonProvider.DNAME),			//5
			new ProviderHeader(MangaFoxProvider.CNAME, MangaFoxProvider.DNAME),			//6
			//new ProviderHeader(MangarawProvider.CNAME, MangarawProvider.DNAME)		//7
	};

	private final SharedPreferences mPreferences;

	public ProvidersStore(Context context) {
		mPreferences = context.getSharedPreferences("providers", Context.MODE_PRIVATE);
	}

	public ArrayList<ProviderHeader> getAllProvidersSorted() {
		final ArrayList<ProviderHeader> list = new ArrayList<>(sProviders.length);
		final int[] order = CollectionsUtils.convertToInt(mPreferences.getString("order", "").split("\\|"), -1);
		for (int o : order) {
			ProviderHeader h = CollectionsUtils.getOrNull(sProviders, o);
			if (h != null) {
				list.add(h);
			}
		}
		for (ProviderHeader h : sProviders) {
			if (!list.contains(h)) {
				list.add(h);
			}
		}
		return list;
	}

	public ArrayList<ProviderHeader> getUserProviders() {
		final ArrayList<ProviderHeader> list = getAllProvidersSorted();
		final int[] disabled = getDisabledIds();
		Iterator<ProviderHeader> iterator = list.iterator();
		while (iterator.hasNext()) {
			ProviderHeader h = iterator.next();
			if (CollectionsUtils.indexOf(disabled, h.hashCode()) != -1) {
				iterator.remove();
			}
		}
		return list;
	}

	public void save(ArrayList<ProviderHeader> providers, SparseBooleanArray enabled) {
		final Integer[] order = new Integer[providers.size()];
		for (int i = 0; i < sProviders.length; i++) {
			ProviderHeader h = sProviders[i];
			int p = providers.indexOf(h);
			if (p != -1) {
				order[i] = p;
			}
		}
		final ArrayList<Integer> disabled = new ArrayList<>();
		for (int i=0;i<providers.size();i++) {
			if (!enabled.get(i, true)) {
				disabled.add(providers.get(i).hashCode());
			}
		}
		mPreferences.edit()
				.putString("order", TextUtils.join("|", order))
				.putString("disabled", TextUtils.join("|", disabled))
				.apply();
	}

	@NonNull
	public int[] getDisabledIds() {
		return CollectionsUtils.convertToInt(mPreferences.getString("disabled", "").split("\\|"), -1);
	}
}
