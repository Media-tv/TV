package com.plex.tv.ui.main;

import android.os.Bundle;
import android.support.v17.leanback.app.BrowseSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;

import com.plex.tv.App;
import com.plex.tv.Config;
import com.plex.tv.R;
import com.plex.tv.dagger.modules.HttpClientModule;
import com.plex.tv.data.Api.TheMovieDbAPI;
import com.plex.tv.data.models.Movie;
import com.plex.tv.data.models.MovieResponse;
import com.plex.tv.ui.base.GlideBackgroundManager;
import com.plex.tv.ui.movies.MoviePresenter;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by <a href="mailto:marcus@gabilheri.com">Marcus Gabilheri</a>
 *
 * @author Marcus Gabilheri
 * @version 1.0
 * @since 10/8/16.
 */
public class MainFragment extends BrowseSupportFragment implements OnItemViewSelectedListener {

    @Inject
    TheMovieDbAPI mDbAPI;

    private GlideBackgroundManager mBackgroundManager;

    private static final int NOW_PLAYING = 0;
    private static final int TOP_RATED = 1;
    private static final int POPULAR = 2;
    private static final int UPCOMING = 3;

    SparseArray<MovieRow> mRows;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        App.instance().appComponent().inject(this);

        // The background manager allows us to manage a dimmed background that does not interfere with the rows
        // It is the preferred way to set the background of a fragment
        mBackgroundManager = new GlideBackgroundManager(getActivity());

        // The brand color will be used as the background for the Headers fragment
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.primary_transparent));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // The TMDB logo on the right corner. It is necessary to show based on their API usage policy
        setBadgeDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.powered_by));

        createDataRows();
        createRows();
        prepareEntranceTransition();
        fetchNowPlayingMovies();
        fetchTopRatedMovies();
        fetchPopularMovies();
        fetchUpcomingMovies();
    }

    /**
     * Creates the data rows objects
     */
    private void createDataRows() {
        mRows = new SparseArray<>();
        MoviePresenter moviePresenter = new MoviePresenter();
        mRows.put(NOW_PLAYING, new MovieRow()
                .setId(NOW_PLAYING)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Now Playing")
                .setPage(1)
        );
        mRows.put(TOP_RATED, new MovieRow()
                .setId(TOP_RATED)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Top Rated")
                .setPage(1)
        );
        mRows.put(POPULAR, new MovieRow()
                .setId(POPULAR)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Popular")
                .setPage(1)
        );
        mRows.put(UPCOMING, new MovieRow()
                .setId(UPCOMING)
                .setAdapter(new ArrayObjectAdapter(moviePresenter))
                .setTitle("Upcoming")
                .setPage(1)
        );
    }

    /**
     * Creates the rows and sets up the adapter of the fragment
     */
    private void createRows() {
        // Creates the RowsAdapter for the Fragment
        // The ListRowPresenter tells to render ListRow objects
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        for (int i = 0; i < mRows.size(); i++) {
            MovieRow row = mRows.get(i);
            // Adds a new ListRow to the adapter. Each row will contain a collection of Movies
            // That will be rendered using the MoviePresenter
            HeaderItem headerItem = new HeaderItem(row.getId(), row.getTitle());
            ListRow listRow = new ListRow(headerItem, row.getAdapter());
            rowsAdapter.add(listRow);
        }
        // Sets this fragments Adapter.
        // The setAdapter method is defined in the BrowseFragment of the Leanback Library
        setAdapter(rowsAdapter);
        setOnItemViewSelectedListener(this);
    }

    /**
     * Fetches now playing movies from TMDB
     */
    private void fetchNowPlayingMovies() {
        mDbAPI.getNowPlayingMovies(Config.API_KEY_URL, mRows.get(NOW_PLAYING).getPage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    bindMovieResponse(response, NOW_PLAYING);
                    startEntranceTransition();
                }, e -> {
                    Timber.e(e, "Error fetching now playing movies: %s", e.getMessage());
                });
    }

    /**
     * Fetches the popular movies from TMDB
     */
    private void fetchPopularMovies() {
        mDbAPI.getPopularMovies(Config.API_KEY_URL, mRows.get(POPULAR).getPage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    bindMovieResponse(response, POPULAR);
                    startEntranceTransition();
                }, e -> {
                    Timber.e(e, "Error fetching popular movies: %s", e.getMessage());
                });
    }

    /**
     * Fetches the upcoming movies from TMDB
     */
    private void fetchUpcomingMovies() {
        mDbAPI.getUpcomingMovies(Config.API_KEY_URL, mRows.get(UPCOMING).getPage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    bindMovieResponse(response, UPCOMING);
                    startEntranceTransition();
                }, e -> {
                    Timber.e(e, "Error fetching upcoming movies: %s", e.getMessage());
                });
    }

    /**
     * Fetches the top rated movies from TMDB
     */
    private void fetchTopRatedMovies() {
        mDbAPI.getTopRatedMovies(Config.API_KEY_URL, mRows.get(TOP_RATED).getPage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    bindMovieResponse(response, TOP_RATED);
                    startEntranceTransition();
                }, e -> {
                    Timber.e(e, "Error fetching top rated movies: %s", e.getMessage());
                });
    }

    /**
     * Binds a movie response to it's adapter
     * @param response
     *      The response from TMDB API
     * @param id
     *      The ID / position of the row
     */
    private void bindMovieResponse(MovieResponse response, int id) {
        MovieRow row = mRows.get(id);
        row.setPage(row.getPage() + 1);
        for(Movie m : response.getResults()) {
            if (m.getPosterPath() != null) { // Avoid showing movie without posters
                row.getAdapter().add(m);
            }
        }
    }

    @Override
    public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        // Check if the item is a movie
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            // Check if the movie has a backdrop
            if(movie.getBackdropPath() != null) {
                mBackgroundManager.loadImage(HttpClientModule.BACKDROP_URL + movie.getBackdropPath());
            } else {
                // If there is no backdrop for the movie we just use a default one
                mBackgroundManager.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.material_bg));
            }
        }
    }
}
