package org.nv95.openmanga.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.nv95.openmanga.MangaPreviewActivity;
import org.nv95.openmanga.R;
import org.nv95.openmanga.adapters.MangaListAdapter;
import org.nv95.openmanga.components.SimpleAnimator;
import org.nv95.openmanga.items.MangaInfo;
import org.nv95.openmanga.items.MangaList;
import org.nv95.openmanga.providers.MangaProvider;
import org.nv95.openmanga.providers.MangaProviderManager;

/**
 * Created by nv95 on 30.09.15.
 */
public class MangaListFragment extends Fragment implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener {
  @Nullable
  private static ListLoadTask taskInstance;

  private AbsListView absListView;
  private boolean grid = true;
  private MangaListAdapter adapter;
  private MangaProvider provider;
    //listView.addFooterView(endlessScroller.getFooter());
  private MangaList list = new MangaList();
  private ProgressBar progressBar;
  private EndlessScroller endlessScroller;
  private MangaListListener listListener;
  private LinearLayout messageBlock;
  private AbsListView.OnScrollListener scrollListener;

  public void update(boolean cleanList) {
    if (list != null && adapter != null) {
      ListLoadTask task = new ListLoadTask();
      endlessScroller.reset();
      list.clear();
      if (cleanList) {
        try {
          adapter.notifyDataSetChanged();
        } catch (Exception ignored) {
        }
      }
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_mangalist,
            container, false);
    endlessScroller = new EndlessScroller(view) {
      @Override
      public boolean onNextPage(int page) {
        if (endlessScroller.hasNextPage() && provider.hasFeature(MangaProviderManager.FUTURE_MULTIPAGE)) {
          new ListLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);
          return true;
        } else {
          return false;
        }
      }
    };
    grid = PreferenceManager.getDefaultSharedPreferences(inflater.getContext()).getBoolean("grid", true);
    messageBlock = (LinearLayout) view.findViewById(R.id.block_message);
    ListView listView = (ListView) view.findViewById(R.id.listView);
    GridView gridView = (GridView) view.findViewById(R.id.gridView);
    listView.setOnItemClickListener(this);
    gridView.setOnItemClickListener(this);
    listView.setOnScrollListener(endlessScroller);
    gridView.setOnScrollListener(endlessScroller);
    listView.setMultiChoiceModeListener(this);
    gridView.setMultiChoiceModeListener(this);
    listView.setVisibility(grid ? View.GONE : View.VISIBLE);
    gridView.setVisibility(grid ? View.VISIBLE : View.GONE);
    progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    absListView = grid ? gridView : listView;
    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Activity activity = getActivity();
    if (activity instanceof MangaListListener) {
      listListener = (MangaListListener) getActivity();
    } else {
      throw new NullPointerException("List listener not found");
    }
    int defProvider = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(activity)
        .getString("defsection", String.valueOf(MangaProviderManager.PROVIDER_LOCAL)));
    if (getArguments() != null) {
      defProvider = getArguments().getInt("provider", MangaProviderManager.PROVIDER_LOCAL);
    }
    provider = new MangaProviderManager(activity).getMangaProvider(defProvider);
    adapter = new MangaListAdapter(activity, list, grid);
    absListView.setAdapter(adapter);
    new ListLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  public MangaProvider getProvider() {
    return provider;
  }

  public void setProvider(MangaProvider provider) {
    messageBlock.setVisibility(View.GONE);
    this.provider = provider;
    list.clear();
    adapter.notifyDataSetChanged();
    endlessScroller.reset();
    new ListLoadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  public boolean isGridLayout() {
    return grid;
  }

  public void setGridLayout(boolean useGrid) {
    if (useGrid != grid) {
      grid = useGrid;
      final int pos = absListView.getFirstVisiblePosition();
      absListView.setAdapter(null);
      ListView listView = (ListView) getView().findViewById(R.id.listView);
      GridView gridView = (GridView) getView().findViewById(R.id.gridView);
      listView.setVisibility(useGrid ? View.GONE : View.VISIBLE);
      gridView.setVisibility(useGrid ? View.VISIBLE : View.GONE);
      absListView = useGrid ? gridView : listView;
      absListView.setAdapter(adapter);
      adapter.setGrid(grid);
      absListView.setSelection(pos);
      PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean("grid", grid).apply();
    }
  }

  public AbsListView.OnScrollListener getScrollListener() {
    return scrollListener;
  }

  public void setScrollListener(AbsListView.OnScrollListener scrollListener) {
    this.scrollListener = scrollListener;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_mangalist, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.action_listmode).setTitle(grid ? R.string.switch_to_list : R.string.switch_to_grid);
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_listmode:
        setGridLayout(!grid);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Intent intent = new Intent(getActivity(), MangaPreviewActivity.class);
    MangaInfo info = adapter.getMangaInfo(position);
    intent.putExtras(info.toBundle());
    startActivity(intent);
  }

  @Override
  public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
    String title = getString(R.string.selected) + " " + absListView.getCheckedItemCount();
    mode.setTitle(title);
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    mode.getMenuInflater().inflate(R.menu.actionmode_mangas, menu);
    menu.findItem(R.id.group_remove).setVisible(provider.hasFeature(MangaProviderManager.FEAUTURE_REMOVE));
    return menu.hasVisibleItems();
  }

  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_remove:
        provider.remove(absListView.getCheckedItemIds());
        mode.finish();
        return true;
      case R.id.action_cancel:
        mode.finish();
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onDestroyActionMode(ActionMode mode) {

  }

  public interface MangaListListener {
    MangaList onListNeeded(MangaProvider provider, int page) throws Exception;

    String onEmptyList(MangaProvider provider);
  }

  private class ListLoadTask extends AsyncTask<Integer, Void, MangaList> {
    public ListLoadTask() {
      if (taskInstance != null) {
        if (taskInstance.getStatus() == Status.RUNNING) {
          taskInstance.cancel(true);
        }
      }
      taskInstance = this;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      messageBlock.setVisibility(View.GONE);
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
        if (list.size() == 0) {
          Toast.makeText(getActivity(), R.string.loading_error, Toast.LENGTH_SHORT).show();
          ((TextView) messageBlock.findViewById(R.id.textView)).setText(listListener.onEmptyList(provider));
          messageBlock.setVisibility(View.VISIBLE);
        }
        endlessScroller.loadingFail();
        adapter.notifyDataSetInvalidated();
      } else if (mangaInfos.size() == 0) {
        if (list.size() == 0) {
          ((TextView) messageBlock.findViewById(R.id.textView)).setText(listListener.onEmptyList(provider));
          messageBlock.setVisibility(View.VISIBLE);
        }
        endlessScroller.loadingFail();
        adapter.notifyDataSetInvalidated();
      } else {
        list.addAll(mangaInfos);
        adapter.notifyDataSetChanged();
        endlessScroller.loadingDone();
      }
      taskInstance = null;
    }

    @Override
    protected MangaList doInBackground(Integer... params) {
      try {
        return listListener.onListNeeded(provider, params.length > 0 ? params[0] : 0);
      } catch (Exception e) {
        return null;
      }
    }
  }

  protected abstract class EndlessScroller implements AbsListView.OnScrollListener {
    boolean nextPage;
    RotateAnimation rotate;
    private int page;
    private boolean loading;
    private View footer;
    private ImageView imageView;

    public EndlessScroller(View view) {
      footer = view.findViewById(R.id.frame_footer);
      imageView = (ImageView) footer.findViewById(R.id.imageView_footer);
      loading = true;
      nextPage = true;
      rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
      rotate.setDuration(500);
      rotate.setRepeatCount(Animation.INFINITE);
      rotate.setInterpolator(new LinearInterpolator());
      footer.setVisibility(View.INVISIBLE);
    }

    public View getFooter() {
      return footer;
    }

    public int getPage() {
      return page;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
      if (scrollListener != null) {
        scrollListener.onScrollStateChanged(view, scrollState);
      }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
      if (scrollListener != null) {
        scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
      }
      if (!loading && nextPage && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
        loading = onNextPage(page + 1);
        if (loading && list.size() != 0) {
          new SimpleAnimator(footer).forceGravity(Gravity.BOTTOM).show();
          imageView.startAnimation(rotate);
        }
      }
    }

    public void loadingDone() {
      page++;
      loading = false;
      new SimpleAnimator(footer).forceGravity(Gravity.BOTTOM).hide();
    }

    public void reset() {
      page = 0;
      loading = true;
      new SimpleAnimator(footer).forceGravity(Gravity.BOTTOM).hide();
      nextPage = true;
    }

    public void loadingFail() {
      new SimpleAnimator(footer).forceGravity(Gravity.BOTTOM).hide();
      nextPage = false;
    }

    public boolean hasNextPage() {
      return nextPage;
    }

    public abstract boolean onNextPage(int page);
  }
}
