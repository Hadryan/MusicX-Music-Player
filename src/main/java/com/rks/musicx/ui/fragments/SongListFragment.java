package com.rks.musicx.ui.fragments;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.SortOrder;
import com.rks.musicx.data.loaders.TrackLoader;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.ItemOffsetDecoration;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.SongListAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class SongListFragment extends miniFragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<List<Song>> {

    private FastScrollRecyclerView rv;
    private SongListAdapter songListAdapter;
    private int trackloader = -1;
    private Helper helper;
    private SearchView searchView;
    private List<Song> songList;

    private BaseRecyclerViewAdapter.OnItemClickListener listClick = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                    rv.smoothScrollToPosition(position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(trackloader, SongListFragment.this, SongListFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                    break;

            }
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener gridClick = new BaseRecyclerViewAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.album_artwork:
                case R.id.album_info:
                    ((MainActivity) getActivity()).onSongSelected(songListAdapter.getSnapshot(), position);
                    rv.smoothScrollToPosition(position);
                    break;
                case R.id.menu_button:
                    helper.showMenu(trackloader, SongListFragment.this, SongListFragment.this, ((MainActivity) getActivity()), position, view, getContext(), songListAdapter);
                    break;

            }
        }
    };


    public static SongListFragment newInstance() {
        return new SongListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.common_rv, container, false);
        rv = (FastScrollRecyclerView) rootView.findViewById(R.id.commonrv);
        songListAdapter = new SongListAdapter(getContext());
        String ateKey = Helper.getATEKey(getContext());
        int colorAccent = Config.accentColor(getContext(), ateKey);
        rv.setPopupBgColor(colorAccent);
        rv.setItemAnimator(new DefaultItemAnimator());
        loadTrak();
        songView();
        setHasOptionsMenu(true);
        helper = new Helper(getContext());
        rv.setAdapter(songListAdapter);
        songList = new ArrayList<>();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.song_search));
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search song");
        if (Extras.getInstance().songView()) {
            menu.findItem(R.id.grid_view).setVisible(true);
        } else {
            menu.findItem(R.id.grid_view).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Extras extras = Extras.getInstance();
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                load();
                break;
            case R.id.menu_sort_by_za:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                load();
                break;
            case R.id.menu_sort_by_year:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                load();
                break;
            case R.id.menu_sort_by_artist:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                load();
                break;
            case R.id.menu_sort_by_album:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                load();
                break;
            case R.id.menu_sort_by_duration:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                load();
                break;
            case R.id.menu_sort_by_date:
                extras.setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
                load();
                break;
            case R.id.bytwo:
                Extras.getInstance().setSongGrid(2);
                loadGridView();
                break;
            case R.id.bythree:
                Extras.getInstance().setSongGrid(3);
                loadGridView();
                break;
            case R.id.byfour:
                Extras.getInstance().setSongGrid(4);
                loadGridView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }


    private void loadTrak() {
        getLoaderManager().initLoader(trackloader, null, this);
    }

    /*
    reload track
    */
    @Override
    public void load() {
        getLoaderManager().restartLoader(trackloader, null, this);
        songListAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Song> filterlist = helper.filter(songList, newText);
        songListAdapter.setFilter(filterlist);
        return true;
    }


    @Override
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        TrackLoader tracksloader = new TrackLoader(getActivity());
        if (id == trackloader) {
            tracksloader.filteralbumsong(MediaStore.Audio.Media.IS_MUSIC + " !=0", null);
            tracksloader.setSortOrder(Extras.getInstance().getSongSortOrder());
            return tracksloader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data == null) {
            return;
        }
        songList = data;
        songListAdapter.addDataList(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        songListAdapter.notifyDataSetChanged();
    }

    private void loadGridView() {
        if (Extras.getInstance().getSongGrid() == 2) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            rv.setLayoutManager(layoutManager);
            load();
        } else if (Extras.getInstance().getSongGrid() == 3) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            rv.setLayoutManager(layoutManager);
            load();
        } else if (Extras.getInstance().getSongGrid() == 4) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
            rv.setLayoutManager(layoutManager);
            load();
        }
    }

    private void songView() {
        if (Extras.getInstance().songView()) {
            songListAdapter.setLayoutId(R.layout.item_grid_view);
            songListAdapter.setOnItemClickListener(gridClick);
            rv.addItemDecoration(new ItemOffsetDecoration(2));
            loadGridView();
        } else {
            songListAdapter.setLayoutId(R.layout.song_list);
            songListAdapter.setOnItemClickListener(listClick);
            CustomLayoutManager customLayoutManager = new CustomLayoutManager(getContext());
            customLayoutManager.setSmoothScrollbarEnabled(true);
            rv.setLayoutManager(customLayoutManager);
            rv.addItemDecoration(new DividerItemDecoration(getActivity(), 75, false));
        }
    }


}
