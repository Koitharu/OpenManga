package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.nv95.openmanga.R;
import org.nv95.openmanga.components.reader.recyclerpager.RecyclerViewPager;
import org.nv95.openmanga.items.MangaSummary;
import org.nv95.openmanga.providers.FavouritesProvider;
import org.nv95.openmanga.providers.NewChaptersProvider;
import org.nv95.openmanga.utils.ChangesObserver;
import org.nv95.openmanga.utils.LayoutUtils;

import java.util.ArrayList;

/**
 * Created by nv95 on 17.11.16.
 */

public class ReaderMenuPanel extends LinearLayout implements View.OnClickListener, View.OnLongClickListener,
        RecyclerViewPager.OnPageChangedListener {

    private MangaSummary mManga;
    private LayoutParams mButtonLPs;
    private final ArrayList<ImageView> mButtons = new ArrayList<>();
    @Nullable
    private OnClickListener mClickListener;

    public ReaderMenuPanel(Context context) {
        super(context);
        init(context);
    }

    public ReaderMenuPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ReaderMenuPanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ReaderMenuPanel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mButtonLPs = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mButtonLPs.weight = 1;
        mClickListener = null;
    }

    public void setOnClickListener(OnClickListener listener) {
        mClickListener = listener;
    }

    public void setData(MangaSummary manga) {
        mManga = manga;
        removeAllViews();
        mButtons.clear();
        mButtons.add(addButton(R.id.menuitem_favourite, R.drawable.ic_favorite_light, R.string.action_favourite));
        mButtons.add(addButton(R.id.menuitem_rotation, R.drawable.ic_screen_rotation_white, R.string.action_settings));
        mButtons.add(addButton(R.id.menuitem_thumbgrid, R.drawable.ic_view_grid_white, R.string.action_settings));
        mButtons.add(addButton(R.id.menuitem_settings, R.drawable.ic_settings_white, R.string.action_settings));
    }

    public void updateMenu() {
        int fav = FavouritesProvider.getInstance(getContext()).getCategory(mManga);
        mButtons.get(0).setImageResource(fav == -1 ? R.drawable.ic_favorite_outline_light : R.drawable.ic_favorite_light);
        mButtons.get(0).setContentDescription(getContext().getString(fav == -1 ? R.string.action_favourite : R.string.action_unfavourite));
        mButtons.get(0).setId(fav == -1 ? R.id.menuitem_favourite : R.id.menuitem_unfavourite);

    }

    public void show() {
        updateMenu();
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    private ImageView addButton(int id, int icon, int title) {
        ImageView imageView = new ImageView(getContext());
        imageView.setLayoutParams(mButtonLPs);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(icon);
        ViewCompat.setBackground(imageView, LayoutUtils.getSelectableBackground(imageView.getContext()));
        imageView.setContentDescription(imageView.getContext().getString(title));
        imageView.setId(id);
        imageView.setOnClickListener(this);
        imageView.setOnLongClickListener(this);
        this.addView(imageView);
        return imageView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menuitem_unfavourite:
                if (FavouritesProvider.getInstance(getContext()).remove(mManga)) {
                    hide();
                    Snackbar.make(this, R.string.unfavourited, Snackbar.LENGTH_SHORT).show();
                    ChangesObserver.getInstance().emitOnFavouritesChanged(mManga, -1);
                }
                break;
            case R.id.menuitem_favourite:
                FavouritesProvider.dialog(getContext(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hide();
                        NewChaptersProvider.getInstance(getContext())
                                .storeChaptersCount(mManga.id, mManga.getChapters().size());
                        Snackbar.make(ReaderMenuPanel.this, R.string.favourited, Snackbar.LENGTH_SHORT).show();
                        ChangesObserver.getInstance().emitOnFavouritesChanged(mManga, which);
                    }
                }, mManga);
                break;
            case R.id.menuitem_settings:
                hide();
            default:
                if (mClickListener != null) {
                    mClickListener.onClick(view);
                }
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
        hide();
    }
}
