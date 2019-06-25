package org.nv95.openmanga.feature.sync.chapter.worker

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.nv95.openmanga.R
import org.nv95.openmanga.core.extention.getString
import org.nv95.openmanga.core.sources.ConnectionSource
import org.nv95.openmanga.feature.newchapter.NewChaptersActivity
import org.nv95.openmanga.helpers.MangaSaveHelper
import org.nv95.openmanga.helpers.NotificationHelper
import org.nv95.openmanga.items.MangaUpdateInfo
import org.nv95.openmanga.providers.FavouritesProvider
import org.nv95.openmanga.providers.NewChaptersProvider
import org.nv95.openmanga.services.SyncService
import org.nv95.openmanga.utils.FileLogger
import org.nv95.openmanga.utils.OneShotNotifier
import timber.log.Timber


class SyncChapterWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val connectionSource: ConnectionSource by inject()

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(applicationContext) }
    private val chaptersCheckEnabled get() = prefs.getBoolean("chupd", true)
    private val chapterCheckWifiOnly get() = prefs.getBoolean("chupd.wifionly", true)
    private val chapterCheckSave get() = prefs.getBoolean("chupd.save", false)

    companion object {
        private const val TAG = "SyncChapterWorker"
        private const val NOTIFICATION_ID = 678
    }

    override val coroutineContext: CoroutineDispatcher = Dispatchers.IO

    override suspend fun doWork(): Result {

        Timber.tag(TAG).d(tags.joinToString())

        // TODO refactoring
        if (chaptersCheckEnabled && connectionSource.isConnectionAvailable(chapterCheckWifiOnly)) {
            val res = NewChaptersProvider.getInstance(applicationContext).checkForNewChapters()
            if (chapterCheckSave && res != null) {
                val favs = FavouritesProvider.getInstance(applicationContext)
                val saveHelper = MangaSaveHelper(applicationContext)
                val mangas = favs.getList(0, 0, 0)
                for (o in res) {
                    try {
                        val manga = mangas?.getById(o.mangaId)
                        if (manga != null) {
                            val summary = favs.getDetailedInfo(manga)
                            if (summary != null) {
                                saveHelper.saveLast(summary, o.newChapters)
                            }
                        }
                    } catch (e: Exception) {
                        FileLogger.getInstance().report("AUTOSAVE", e)
                    }

                }
            }
            handleNewChapter(res)
        }

        Timber.tag(TAG).d("success")
        return Result.success()
    }

    private fun handleNewChapter(newChapter: Array<MangaUpdateInfo>?) {
        if (newChapter?.isNotEmpty() == true) {
            var sum = 0
            for (o in newChapter) {
                sum += o.chapters - o.lastChapters
            }

            val notificationBuilder: NotificationCompat.Builder = NotificationHelper(applicationContext)
                    .title(R.string.new_chapters)
                    .icon(R.drawable.ic_stat_star)
                    .image(R.mipmap.ic_launcher)
                    .intentActivity(Intent(applicationContext, NewChaptersActivity::class.java), 1)
                    .text(String.format(getString(R.string.new_chapters_count), sum))
                    .builder()

            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notificationBuilder.setStyle(NotificationCompat.InboxStyle().also {
                    for (o in newChapter) {
                        it.addLine((o.chapters - o.lastChapters).toString() + " - " + o.mangaName)
                    }
                    it.setSummaryText(String.format(getString(R.string.new_chapters_count), sum))
                }).build()
            } else {
                notificationBuilder.build()
            }
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
            OneShotNotifier(applicationContext).notify(NOTIFICATION_ID, notification)
        }
        SyncService.sync(applicationContext)
    }

}