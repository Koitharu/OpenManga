package org.nv95.openmanga;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.nv95.openmanga.components.EndlessScrollListener;
import org.nv95.openmanga.providers.LocalMangaProvider;
import org.nv95.openmanga.providers.MangaInfo;
import org.nv95.openmanga.providers.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;

import java.io.IOException;

/**
 * Created by nv95 on 30.09.15.
 *
 */
public class MangaListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private ListView listView;
    private MangaListAdapter adapter;
    private MangaProvider provider;
    private MangaList list;
    private ProgressBar progressBar;
    private EndlessScrollListener endlessScroller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mangalist,
                container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listView.setOnScrollListener(endlessScroller = new EndlessScrollListener(inflater.inflate(R.layout.footer_loadpage, null)) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                //progressBar.setVisibility(View.VISIBLE);
                new ListLoadTask().execute(page);
                return true;
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        //loadFooter = inflater.inflate(R.layout.footer_loadpage, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        provider = new LocalMangaProvider(getActivity());
        adapter = new MangaListAdapter(getActivity(),list = new MangaList(), false);
        listView.setAdapter(adapter);
        //((LocalMangaProvider)provider).test();
        new ListLoadTask().execute();
    }

    public MangaProvider getProvider() {
        return provider;
    }

    public void setProvider(MangaProvider provider) {
        this.provider = provider;
        list.clear();
        adapter.notifyDataSetChanged();
        endlessScroller.setEnabled(provider.hasFeatures(MangaProviderManager.FUTURE_MULTIPAGE));
        new ListLoadTask().execute();
    }

    /*protected boolean nextPage() {
        if (listView.getFooterViewsCount() != 0)
            return false;
        page++;
        String s = getText(R.string.loading_page) + " " + page;
        ((TextView)loadFooter.findViewById(R.id.textView)).setText(s);
        listView.addFooterView(loadFooter);
        new ListLoadTask().execute(page);
        return true;
    }*/

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), MangaPreviewActivity.class);
        MangaInfo info = adapter.getMangaInfo(position);
        intent.putExtras(info.toBundle());
        startActivity(intent);
    }


    private class ListLoadTask extends AsyncTask<Integer, Void, MangaList> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (list.size() == 0)
                progressBar.setVisibility(View.VISIBLE);
            /*if (listView.getFooterViewsCount() != 0)
                listView.removeFooterView(loadFooter);*/
        }

        @Override
        protected void onPostExecute(MangaList mangaInfos) {
            super.onPostExecute(mangaInfos);
            progressBar.setVisibility(View.GONE);
            if (mangaInfos == null) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
            } else if (mangaInfos.size() == 0) {
                Toast.makeText(getActivity(), "No manga found", Toast.LENGTH_SHORT).show();
            } else {
                list.addAll(mangaInfos);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected MangaList doInBackground(Integer... params) {
            try {
                return provider.getList(params.length > 0 ? params[0] : 0);
            } catch (IOException e) {
                return null;
            }
        }
    }



}
