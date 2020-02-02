package org.nv95.openmanga.feature.newchapter.view

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_updates.*
import org.koin.androidx.scope.currentScope
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.nv95.openmanga.R
import org.nv95.openmanga.core.fragment.BaseFragment
import org.nv95.openmanga.core.lifecycle.loadKoinModulesLifecycle
import org.nv95.openmanga.core.util.initToolbar
import org.nv95.openmanga.di.lifecycleFeatureModule
import org.nv95.openmanga.feature.newchapter.adapter.NewChaptersAdapter
import org.nv95.openmanga.feature.newchapter.di.newChapterModule
import org.nv95.openmanga.lists.MangaList


class NewChaptersFragment : BaseFragment() {

	private val newChapterModel by lazy { currentScope.getViewModel<NewChapterViewModel>(this) }

	private val mAdapter by lazy { NewChaptersAdapter(MangaList()) }

	override fun getLayout(): Int = R.layout.fragment_updates

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		loadKoinModulesLifecycle(newChapterModule, lifecycleFeatureModule(this))
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initToolbar(getString(R.string.manga_updates)) {
			inflateMenu(R.menu.updates)
			setOnMenuItemClickListener(::onOptionsItemSelected)
		}

		refreshLayout.setOnRefreshListener {
			showLoader(true)
			newChapterModel.load()
		}

		recyclerView.layoutManager = LinearLayoutManager(context)
		recyclerView.adapter = mAdapter
		initTouchListener()

		initNewChapterObserver()

		showLoader(true)
		newChapterModel.firstLoad()
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.action_checkall -> { showConfirmDialogMarkAll(); true }
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun showConfirmDialogMarkAll() {
		AlertDialog.Builder(context ?: return)
				.setMessage(R.string.mark_all_viewed_confirm)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.yes) { _, _ ->
					newChapterModel.markAllAsViewed()
				}.create().show()
	}

	override fun showLoader(show: Boolean) {
		refreshLayout.post { refreshLayout.isRefreshing = show }
	}

	private fun initTouchListener() {
		ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
				ItemTouchHelper.START or ItemTouchHelper.END) {
			override fun onMove(
					recyclerView: RecyclerView,
					viewHolder: RecyclerView.ViewHolder,
					target: RecyclerView.ViewHolder
			): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val pos = viewHolder.adapterPosition
				val item = mAdapter.getItem(pos)
				newChapterModel.markAsViewed(item.hashCode())
			}

		}).attachToRecyclerView(recyclerView)
	}

	private fun initNewChapterObserver() {
		newChapterModel.mangaList.observe(this, Observer {
			showLoader(false)
			mAdapter.setDataset(it)
			showOrHideTextHolder()
		})
	}

	private fun showOrHideTextHolder() {
		textView_holder.visibility = if (mAdapter.itemCount == 0) View.VISIBLE else View.GONE
	}

}