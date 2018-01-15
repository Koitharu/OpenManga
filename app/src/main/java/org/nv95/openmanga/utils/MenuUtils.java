package org.nv95.openmanga.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import org.nv95.openmanga.R;
import org.nv95.openmanga.content.MangaHeader;
import org.nv95.openmanga.content.shelf.Category;
import org.nv95.openmanga.content.storage.db.CategoriesRepository;
import org.nv95.openmanga.content.storage.db.CategoriesSpecification;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koitharu on 26.12.17.
 */

public final class MenuUtils {

	public static void buildOpenWithSubmenu(Context context,  MangaHeader mangaHeader, MenuItem menuItem) {
		final Uri uri = Uri.parse(mangaHeader.url);
		final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		buildIntentSubmenu(context, intent, menuItem);
	}

	public static void buildShareSubmenu(Context context,  MangaHeader mangaHeader, MenuItem menuItem) {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, mangaHeader.url);
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mangaHeader.name);
		buildIntentSubmenu(context, intent, menuItem);
	}

	private static void buildIntentSubmenu(Context context, Intent intent, MenuItem menuItem) {
		final SubMenu menu = menuItem.getSubMenu();
		final PackageManager pm = context.getPackageManager();
		final List<ResolveInfo> allActivities = pm.queryIntentActivities(intent, 0);
		if (allActivities.isEmpty()) {
			menuItem.setVisible(false);
		} else {
			menuItem.setVisible(true);
			menu.clear();
			for (ResolveInfo o : allActivities) {
				MenuItem item = menu.add(o.loadLabel(pm));
				item.setIcon(o.loadIcon(pm));
				ComponentName name = new ComponentName(o.activityInfo.applicationInfo.packageName,
						o.activityInfo.name);
				Intent i = new Intent(intent);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				i.setComponent(name);
				item.setIntent(i);
			}
		}
	}

	public static void buildCategoriesSubmenu(Context context, MenuItem menuItem) {
		final SubMenu menu = menuItem.getSubMenu();
		final CategoriesRepository categoriesRepository = new CategoriesRepository(context);
		final ArrayList<Category> categories = categoriesRepository.query(new CategoriesSpecification().orderByName(false));
		if (categories == null) {
			return;
		}
		if (categories.isEmpty()) {
			Category defaultCategory = Category.createDefault(context);
			categories.add(defaultCategory);
			categoriesRepository.add(defaultCategory);
		}
		for (Category category : categories) {
			MenuItem item = menu.add(R.id.group_categories, category.id, Menu.NONE, category.name);
			item.setCheckable(true);
		}
	}

	public static void setRadioCheckable(MenuItem menuItem, @IdRes int group_id) {
		final SubMenu menu = menuItem.getSubMenu();
		if (menu != null) {
			menu.setGroupCheckable(group_id, true, true);
		}
	}
}
