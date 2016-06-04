package org.nv95.openmanga.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.nv95.openmanga.items.SimpleDownload;
import org.nv95.openmanga.utils.MangaStore;

import java.io.File;
import java.util.Date;

/**
 * Created by nv95 on 12.02.16.
 */
@Deprecated
public class MangaSaveHelper {
    private static final String TABLE_STORAGE = "local_storage";
    private static final String TABLE_CHAPTERS = "local_chapters";
    private static final String TABLE_PAGES = "local_pages";

    private final StorageHelper mStorageHelper;
    private final SQLiteDatabase mDatabase;
    private final File mStorageDir;

    public MangaSaveHelper(Context context) {
        mStorageHelper = new StorageHelper(context);
        mDatabase = mStorageHelper.getWritableDatabase();
        mStorageDir = MangaStore.getMangasDir(context);
    }

    public void close() {
        mDatabase.close();
        mStorageHelper.close();
    }

    public MangaSaveBuilder newManga(int id) {
        return new MangaSaveBuilder(id);
    }


    public class MangaSaveBuilder {
        private final ContentValues mContentValues;
        private final int mMangaId;
        private final File mMangaDir;

        private MangaSaveBuilder(int id) {
            mMangaDir = new File(mStorageDir, String.valueOf(id));
            //noinspection ResultOfMethodCallIgnored
            mMangaDir.mkdirs();
            mContentValues = new ContentValues();
            mMangaId = id;
            mContentValues.put("id", String.valueOf(id).hashCode());
            mContentValues.put("path", mMangaId);
        }

        public MangaSaveBuilder name(String name) {
            mContentValues.put("name", name);
            return this;
        }

        public MangaSaveBuilder summary(String summary) {
            mContentValues.put("summary", summary);
            return this;
        }

        public MangaSaveBuilder description(String description) {
            mContentValues.put("description", description);
            return this;
        }

        public MangaSaveBuilder preview(String preview) {
            mContentValues.put("preview", preview);
            return this;
        }

        public MangaSaveBuilder downloadPreview(String url) {
            File dest = new File(mMangaDir, String.valueOf(url.hashCode()));
            new SimpleDownload(url, dest).run();
            mContentValues.put("preview", dest.getPath());
            return this;
        }

        public MangaSaveBuilder subtitle(String subtitle) {
            mContentValues.put("subtitle", subtitle);
            return this;
        }

        public MangaSaveBuilder provider(Class<?> provider) {
            mContentValues.put("provider", provider.getName());
            return this;
        }

        public MangaSaveBuilder timestamp(long timestamp) {
            mContentValues.put("timestamp", timestamp);
            return this;
        }

        public MangaSaveBuilder now() {
            return timestamp(new Date().getTime());
        }

        public void commit() {
            mDatabase.insert(TABLE_STORAGE, null, mContentValues);
        }

        public void dismiss() {
            new DirRemoveHelper(mMangaDir).run();
        }

        public ChapterSaveBuilder newChapter(int id) {
            return new ChapterSaveBuilder(id);
        }

        public class ChapterSaveBuilder {
            private final ContentValues mContentValues;
            private final int mChapterId;
            private final File mChapterDir;

            private ChapterSaveBuilder(int id) {
                mContentValues = new ContentValues();
                mChapterId = id;
                mContentValues.put("id", id);
                mContentValues.put("mangaId", mMangaId);
                mChapterDir = new File(mMangaDir, String.valueOf(mChapterId));
                //noinspection ResultOfMethodCallIgnored
                mChapterDir.mkdirs();
            }

            public ChapterSaveBuilder name(String name) {
                mContentValues.put("name", name);
                return this;
            }

            public void commit() {
                mDatabase.insert(TABLE_CHAPTERS, null, mContentValues);
            }

            public void dissmiss() {
                new DirRemoveHelper(mChapterDir).run();
            }

            public PageSaveBuilder newPage(int id) {
                return new PageSaveBuilder(id);
            }

            public class PageSaveBuilder {
                private final ContentValues mContentValues;
                private final int mPageId;

                private PageSaveBuilder(int id) {
                    mContentValues = new ContentValues();
                    mPageId = id;
                    mContentValues.put("id", mPageId);
                    mContentValues.put("chapterId", mChapterId);
                }

                public PageSaveBuilder page(String page) {
                    mContentValues.put("path", page);
                    return this;
                }

                public PageSaveBuilder downloadPage(String url) {
                    File dest = new File(mChapterDir, String.valueOf(url.hashCode()));
                    new SimpleDownload(url, dest).run();
                    return page(dest.getPath());
                }

                public void commit() {
                    mDatabase.insert(TABLE_PAGES, null, mContentValues);
                }
            }
        }
    }
}
