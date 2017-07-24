package org.nv95.openmanga.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuAdapter;
import android.support.v7.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by admin on 24.07.17.
 */

@SuppressLint("RestrictedApi")
public class MenuDialog implements DialogInterface.OnClickListener {

    private final AlertDialog mDialog;
    private final MenuBuilder mMenu;
    @Nullable
    private MenuItem.OnMenuItemClickListener mItemClickListener;

    public MenuDialog(Context context, @MenuRes int menuId, @Nullable CharSequence title) {
        mMenu = new MenuBuilder(context);
        new MenuInflater(context).inflate(menuId, mMenu);
        mDialog = new AlertDialog.Builder(context)
                .setAdapter(new MenuAdapter(mMenu, LayoutInflater.from(context), false), this)
                .setTitle(title)
                .create();
    }

    public MenuDialog setOnItemClickListener(MenuItem.OnMenuItemClickListener listener) {
        mItemClickListener = listener;
        return this;
    }

    public void show() {
        mDialog.show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (mItemClickListener != null) {
            mItemClickListener.onMenuItemClick(mMenu.getItem(i));
        }
    }
}
