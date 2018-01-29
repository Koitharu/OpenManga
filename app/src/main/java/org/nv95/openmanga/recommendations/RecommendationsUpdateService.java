package org.nv95.openmanga.recommendations;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.nv95.openmanga.R;
import org.nv95.openmanga.core.models.MangaHeader;
import org.nv95.openmanga.core.models.MangaRecommendation;
import org.nv95.openmanga.core.models.ProviderHeader;
import org.nv95.openmanga.core.providers.MangaProvider;
import org.nv95.openmanga.core.storage.ProvidersStore;
import org.nv95.openmanga.core.storage.db.RecommendationsRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by koitharu on 29.01.18.
 */

public final class RecommendationsUpdateService extends IntentService {

	public static final String ACTION_RECOMMENDATIONS_UPDATED = "org.nv95.openmanga.ACTION_RECOMMENDATIONS_UPDATED";

	public RecommendationsUpdateService() {
		super("recommendations");
		setIntentRedelivery(true);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		final RecommendationsRepository repository = RecommendationsRepository.get(this);
		final ArrayList<ProviderHeader> providers = new ProvidersStore(this).getUserProviders();
		final Random random = new Random();
		repository.clear();
		for (ProviderHeader o : providers) {
			final MangaProvider provider = MangaProvider.get(this, o.cName);
			//popular
			int sort = MangaProvider.findSortIndex(provider, R.string.sort_popular);
			if (sort == -1) {
				sort = MangaProvider.findSortIndex(provider, R.string.sort_rating);
			}
			if (sort != -1) {
				try {
					ArrayList<MangaHeader> list = provider.query(null, random.nextInt(4), sort, new String[0]);
					Collections.shuffle(list);
					int count = Math.min(10, list.size());
					for (int i = 0; i < count; i++) {
						repository.add(new MangaRecommendation(
								list.get(i),
								R.string.sort_popular
						));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//newest
			sort = MangaProvider.findSortIndex(provider, R.string.sort_latest);
			if (sort != -1) {
				try {
					ArrayList<MangaHeader> list = provider.query(null, random.nextInt(4), sort, new String[0]);
					Collections.shuffle(list);
					int count = Math.min(10, list.size());
					for (int i = 0; i < count; i++) {
						repository.add(new MangaRecommendation(
								list.get(i),
								R.string.sort_latest
						));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//newest
			sort = MangaProvider.findSortIndex(provider, R.string.sort_updated);
			if (sort != -1) {
				try {
					ArrayList<MangaHeader> list = provider.query(null, random.nextInt(4), sort, new String[0]);
					Collections.shuffle(list);
					int count = Math.min(10, list.size());
					for (int i = 0; i < count; i++) {
						repository.add(new MangaRecommendation(
								list.get(i),
								R.string.sort_updated
						));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		sendBroadcast(new Intent(ACTION_RECOMMENDATIONS_UPDATED));
	}
}
