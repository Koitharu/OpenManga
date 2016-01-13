package org.nv95.openmanga.components;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.nv95.openmanga.R;

/**
 * Created by nv95 on 13.01.16.
 */
public class BottomSheet extends Dialog implements DialogInterface {
    private final Context context;
    private final ListView listView;
    private final View buttonbar;
    private final TextView textViewTitle;
    private final Button[] buttons;
    private final OnClickListener[] clickListeners;

    public BottomSheet(Context context) {
        super(context, R.style.MaterialDialogSheet);
        this.context = context;
        View view = View.inflate(context, R.layout.bottomsheet, null);
        listView = (ListView) view.findViewById(R.id.listView);
        buttonbar = view.findViewById(R.id.buttonbar);
        textViewTitle = (TextView) view.findViewById(R.id.textView_title);
        buttons = new Button[] {
                (Button) view.findViewById(R.id.button_neutral),
                (Button) view.findViewById(R.id.button_negative),
                (Button) view.findViewById(R.id.button_positive)
        };
        clickListeners = new OnClickListener[3];
        setContentView(view);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.BOTTOM);
    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_neutral:
                    if (clickListeners[0] != null) {
                        clickListeners[0].onClick(BottomSheet.this, BUTTON_NEUTRAL);
                    }
                    break;
                case R.id.button_negative:
                    dismiss();
                    if (clickListeners[1] != null) {
                        clickListeners[1].onClick(BottomSheet.this, BUTTON_NEGATIVE);
                    }
                    break;
                case R.id.button_positive:
                    dismiss();
                    if (clickListeners[2] != null) {
                        clickListeners[2].onClick(BottomSheet.this, BUTTON_POSITIVE);
                    }
                    break;
            }
        }
    };

    public BottomSheet setPositiveButton(String text, @Nullable OnClickListener onClickListener) {
        buttonbar.setVisibility(View.VISIBLE);
        buttons[2].setVisibility(View.VISIBLE);
        buttons[2].setText(text);
        clickListeners[2] = onClickListener;
        buttons[2].setOnClickListener(onButtonClick);
        return this;
    }

    public BottomSheet setPositiveButton(int textId, @Nullable OnClickListener onClickListener) {
        return setPositiveButton(context.getString(textId), onClickListener);
    }

    public BottomSheet setNegativeButton(String text, @Nullable OnClickListener onClickListener) {
        buttonbar.setVisibility(View.VISIBLE);
        buttons[1].setVisibility(View.VISIBLE);
        buttons[1].setText(text);
        clickListeners[1] = onClickListener;
        buttons[1].setOnClickListener(onButtonClick);
        return this;
    }

    public BottomSheet setNegativeButton(int textId, @Nullable OnClickListener onClickListener) {
        return setNegativeButton(context.getString(textId), onClickListener);
    }

    public BottomSheet setNeutralButton(String text, @Nullable OnClickListener onClickListener) {
        buttonbar.setVisibility(View.VISIBLE);
        buttons[0].setVisibility(View.VISIBLE);
        buttons[0].setText(text);
        clickListeners[0] = onClickListener;
        buttons[0].setOnClickListener(onButtonClick);
        return this;
    }

    public BottomSheet setNeutralButton(int textId, @Nullable OnClickListener onClickListener) {
        return setNeutralButton(context.getString(textId), onClickListener);
    }

    public BottomSheet setAdapter(ListAdapter adapter) {
        listView.setAdapter(adapter);
        return this;
    }

    public BottomSheet setItems(String[] items, @LayoutRes int layoutId) {
        listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
        listView.setAdapter(new ArrayAdapter<>(context, layoutId, items));
        return this;
    }

    public BottomSheet setOnItemClickListener(final DialogInterface.OnClickListener listener) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onClick(BottomSheet.this, position);
            }
        });
        return this;
    }

    public BottomSheet setOnItemCheckListener(final DialogInterface.OnMultiChoiceClickListener listener) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onClick(BottomSheet.this, position, listView.isItemChecked(position));
            }
        });
        return this;
    }

    public BottomSheet setSheetTitle(@StringRes int resId) {
        textViewTitle.setVisibility(View.VISIBLE);
        textViewTitle.setText(resId);
        return this;
    }

    public BottomSheet setMultiChoiceItems(String[] items, boolean[] checkedItems) {
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_multiple_choice, items));
        for (int i=0;i<checkedItems.length;i++) {
            listView.setItemChecked(i, checkedItems[i]);
        }
        return this;
    }

    public void checkAll(boolean checked) {
        for (int i=listView.getCount()-1;i>=0;i--) {
            listView.setItemChecked(i, checked);
        }
    }
}
