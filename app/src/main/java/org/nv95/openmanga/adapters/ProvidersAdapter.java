package org.nv95.openmanga.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import org.nv95.openmanga.R;
import org.nv95.openmanga.providers.staff.MangaProviderManager;
import org.nv95.openmanga.providers.staff.ProviderSummary;

/**
 * Created by nv95 on 12.07.16.
 */

public class ProvidersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final int ITEM_VIEW = 0;
    private static final int ITEM_FOOTER = 1;

    private final MangaProviderManager mProviderManager;
    private final String[] languages;

    public ProvidersAdapter(Context context) {
        mProviderManager = new MangaProviderManager(context);
        languages = context.getResources().getStringArray(R.array.languages);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW) {
            ProviderHolder holder = new ProviderHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_provider, parent, false)
            );
            holder.checkbox.setOnClickListener(this);
            return holder;
        } else {
            return new FooterHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.footer_disclaimer, parent, false)
            );
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProviderHolder) {
            ProviderSummary item = MangaProviderManager.providers[position];
            ((ProviderHolder)holder).text1.setText(item.name);
            ((ProviderHolder)holder).text2.setText(languages[item.lang]);
            ((ProviderHolder)holder).checkbox.setChecked(mProviderManager.isProviderEnabled(item.name));
            ((ProviderHolder)holder).checkbox.setTag(item);
        }
    }

    @Override
    public int getItemCount() {
        return MangaProviderManager.providers.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position < MangaProviderManager.providers.length ? ITEM_VIEW : ITEM_FOOTER;
    }

    @Override
    public void onClick(View v) {
        ProviderSummary item = (ProviderSummary) v.getTag();
        mProviderManager.setProviderEnabled(
                item.name,
                ((Checkable)v).isChecked()
        );
    }

    static class ProviderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView text1;
        final TextView text2;
        final SwitchCompat checkbox;

        public ProviderHolder(View itemView) {
            super(itemView);
            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
            checkbox = (SwitchCompat) itemView.findViewById(android.R.id.checkbox);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            checkbox.performClick();
        }
    }

    static class FooterHolder extends RecyclerView.ViewHolder {

        public FooterHolder(View itemView) {
            super(itemView);
        }
    }
}
