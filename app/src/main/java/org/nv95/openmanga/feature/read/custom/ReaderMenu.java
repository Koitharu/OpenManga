package org.nv95.openmanga.feature.read.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.feature.read.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.dialogs.NavigationListener;
import org.nv95.openmanga.items.Bookmark;
import org.nv95.openmanga.items.MangaChapter;
import org.nv95.openmanga.feature.manga.domain.MangaInfo;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.BookmarksProvider;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.HistoryProvider;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.NewChaptersProvider;
import org.nv95.openmanga.utils.ChangesObserver;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by nv95 on 17.11.16.
 */

public class ReaderMenu extends FrameLayout implements View.OnClickListener, View.OnLongClickListener,
        RecyclerViewPager.OnPageChangedListener, PopupMenu.OnMenuItemClickListener, NavigationListener {

    private boolean mIsStatusBar;
    private int mLimitYStatusBar;

    private MangaSummary mManga;

    private ImageView[] mButtons;
    private TextView mTitle;
    private ProgressBar mProgressBar;
    private View[] mMenus;
    private boolean mVisible;
    private TreeSet<Integer> mBookmarks;

    private PopupMenu mSaveMenu;
    private PopupMenu mOptionsMenu;

    @Nullable
    private Callback mCallback;

    public ReaderMenu(Context context) {
        super(context);
        init(context);
    }

    public ReaderMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReaderMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ReaderMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void setCallback(@Nullable Callback callback) {
        mCallback = callback;
    }

    private void init(Context context) {
        mVisible = false;
        mBookmarks = new TreeSet<>();
        LayoutInflater.from(context).inflate(R.layout.readermenu, this, true);
        mLimitYStatusBar = getContext().getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
        mProgressBar = findViewById(R.id.progressBar);
        mTitle = findViewById(android.R.id.title);
        mMenus = new View[] {
                findViewById(R.id.menu_top),
                findViewById(R.id.menu_bottom)
        };
        mButtons = new ImageView[] {
                findViewById(android.R.id.home),
                findViewById(R.id.menuitem_favourite),
                findViewById(R.id.menuitem_unfavourite),
                findViewById(R.id.menuitem_save),
                findViewById(R.id.menuitem_bookmark),
                findViewById(R.id.menuitem_rotation),
                findViewById(R.id.menuitem_settings),
                findViewById(R.id.menuitem_thumblist),
                findViewById(R.id.menuitem_unbookmark),
        };
        for (ImageView o : mButtons) {
            o.setOnClickListener(this);
            o.setOnLongClickListener(this);
        }
        mTitle.setOnClickListener(this);
        mProgressBar.setOnClickListener(this);
        mSaveMenu = new PopupMenu(context, mButtons[3]);
        mSaveMenu.inflate(R.menu.save);
        mSaveMenu.setOnMenuItemClickListener(this);

        mOptionsMenu = new PopupMenu(context, mButtons[6]);
        mOptionsMenu.inflate(R.menu.read_prefs);
        mOptionsMenu.setOnMenuItemClickListener(this);
    }

    /**
     * Для устройств с системными кнопками
     * При проведении снизу в верх или сверзу вниз у края экрана, для отображения системных кнопок
     * @param ev
     * @return
     */

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mVisible) {
                    hide();
                }
                if (ev.getY() < mLimitYStatusBar || (getHeight() - ev.getY() < (mLimitYStatusBar * 2))) {
                    mIsStatusBar = true;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                if (mIsStatusBar)
                    return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsStatusBar = false;
                break;
        }
        return mIsStatusBar || super.onTouchEvent(ev);
    }
    
    public void setData(MangaSummary manga) {
        mManga = manga;
        mTitle.setText(manga.name);
        mBookmarks.clear();
    }

    public void updateMenu() {
        int fav = FavouritesProvider.getInstance(getContext()).getCategory(mManga);
        mButtons[1].setVisibility(fav == -1 ? VISIBLE : GONE);
        mButtons[2].setVisibility(fav != -1 ? VISIBLE : GONE);
    }

    public void show() {
        updateMenu();
        if (mVisible) {
            return;
        }
        mVisible = true;
        requestLayout();
        if (mCallback != null) {
            mCallback.onVisibilityChanged(true);
        }
        mMenus[0].setTranslationY(-mMenus[0].getHeight());
        mMenus[1].setTranslationY(mMenus[1].getHeight());
        ViewPropertyAnimatorCompat[] animators = new ViewPropertyAnimatorCompat[] {
                ViewCompat.animate(mMenus[0]).translationY(0f).setDuration(250).withStartAction(mShowRunnable),
                ViewCompat.animate(mMenus[1]).translationY(0f).setDuration(250).withStartAction(mShowRunnable)
        };
        animators[0].start();
        animators[1].start();
    }

    public void hide() {
        if (!mVisible) {
            return;
        }
        mVisible = false;
        if (mCallback != null) {
            mCallback.onVisibilityChanged(false);
        }
        ViewPropertyAnimatorCompat[] animators = new ViewPropertyAnimatorCompat[] {
                ViewCompat.animate(mMenus[0]).translationY(-mMenus[0].getHeight()).setDuration(250).withEndAction(mHideRunnable),
                ViewCompat.animate(mMenus[1]).translationY(mMenus[1].getHeight()).setDuration(250).withEndAction(mHideRunnable)
        };
        animators[0].start();
        animators[1].start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menuitem_unfavourite:
            case R.id.menuitem_favourite:
                final FavouritesProvider favouritesProvider = FavouritesProvider.getInstance(getContext());
                FavouritesProvider.dialog(getContext(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hide();
                        if (which == DialogInterface.BUTTON_NEUTRAL) {
                            if (favouritesProvider.remove(mManga)) {
                                ChangesObserver.getInstance().emitOnFavouritesChanged(mManga, -1);
                                Snackbar.make(ReaderMenu.this, R.string.unfavourited, Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            NewChaptersProvider.getInstance(getContext())
                                    .storeChaptersCount(mManga.id, mManga.getChapters().size());
                            ChangesObserver.getInstance().emitOnFavouritesChanged(mManga, which);
                            Snackbar.make(ReaderMenu.this, R.string.favourited, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }, mManga);
                break;
            case R.id.menuitem_settings:
                Menu menu = mOptionsMenu.getMenu();
                menu.findItem(R.id.action_webmode).setChecked(
                        HistoryProvider.getInstance(view.getContext()).isWebMode(mManga)
                );
                mOptionsMenu.show();
                break;
            case R.id.menuitem_save:
                menu = mSaveMenu.getMenu();
                if (LocalMangaProvider.class.equals(mManga.provider)) {
                    menu.findItem(R.id.action_save).setVisible(false);
                    menu.findItem(R.id.action_save_more).setVisible(mManga.status == MangaInfo.STATUS_ONGOING);
                } else {
                    menu.findItem(R.id.action_save).setVisible(true);
                    menu.findItem(R.id.action_save_more).setVisible(false);
                }
                mSaveMenu.show();
                break;
            default:
                if (mCallback != null) {
                    mCallback.onActionClick(view.getId());
                }
        }
    }

    public void onBookmarkAdded(Bookmark bookmark) {
        mBookmarks.add(bookmark.page);
        if (bookmark.page == mProgressBar.getProgress()) {
            mButtons[4].setVisibility(GONE);
            mButtons[8].setVisibility(VISIBLE);
        }
    }

    public void onBookmarkRemoved(int pos) {
        mBookmarks.remove(pos);
        if (pos == mProgressBar.getProgress()) {
            mButtons[8].setVisibility(GONE);
            mButtons[4].setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view instanceof ImageView) {
            CharSequence title = view.getContentDescription();
            Toast toast = Toast.makeText(view.getContext(), title, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return true;
        }
        return false;
    }

    @Override
    public void OnPageChanged(int oldPosition, int newPosition) {
        mProgressBar.setProgress(newPosition);
        if (mBookmarks.contains(newPosition)) {
            mButtons[4].setVisibility(GONE);
            mButtons[8].setVisibility(VISIBLE);
        } else {
            mButtons[8].setVisibility(GONE);
            mButtons[4].setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_webmode:
                menuItem.setChecked(!menuItem.isChecked());
                HistoryProvider.getInstance(getContext()).setWebMode(mManga, menuItem.isChecked());
            default:
                if (mCallback != null) {
                    mCallback.onActionClick(menuItem.getItemId());
                }
        }
        return true;
    }

    public void onChapterChanged(MangaChapter chapter, int pagesCount) {
        mBookmarks.clear();
        mProgressBar.setMax(pagesCount);
        ArrayList<Bookmark> bookmarks = BookmarksProvider.getInstance(getContext())
                .getAll(mManga.id, chapter.number);
        for (Bookmark o : bookmarks) {
            mBookmarks.add(o.page);
        }
    }

    @Override
    public void onPageChange(int page) {
        if (mCallback != null) {
            mCallback.onPageChanged(page);
        }
    }

    public boolean isVisible() {
        return mVisible;
    }

    public interface Callback {
        void onActionClick(int id);
        void onPageChanged(int index);
        void onVisibilityChanged(boolean visible);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.visible = mVisible;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState)state;
        super.onRestoreInstanceState(ss.getSuperState());
        mVisible = ss.visible;
        mMenus[0].setVisibility(mVisible ? VISIBLE : GONE);
        mMenus[1].setVisibility(mVisible ? VISIBLE : GONE);
    }

    static class SavedState extends BaseSavedState {

        boolean visible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            visible = in.readByte() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (visible ? 1 : 0));
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            mMenus[0].setVisibility(VISIBLE);
            mMenus[1].setVisibility(VISIBLE);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mMenus[0].setVisibility(INVISIBLE);
            mMenus[1].setVisibility(INVISIBLE);
        }
    };
}
