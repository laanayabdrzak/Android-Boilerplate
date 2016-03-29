package abderrazak.com.recycleviewcardview.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.List;

import abderrazak.com.recycleviewcardview.BaseApplication;
import abderrazak.com.recycleviewcardview.R;
import abderrazak.com.recycleviewcardview.data.model.Movie;
import abderrazak.com.recycleviewcardview.ui.detail.DetailActivity;
import abderrazak.com.recycleviewcardview.util.EventBusHelper;
import abderrazak.com.recycleviewcardview.util.UiUtils;
import abderrazak.com.recycleviewcardview.views.adapters.RVAdapter;
import abderrazak.com.recycleviewcardview.views.widgets.DialogFactory;
import abderrazak.com.recycleviewcardview.views.widgets.SnackBarFactory;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, MainMvpView{


    private static final int ANIM_DURATION_TOOLBAR = 300;
    private static final int ANIM_DURATION_FAB = 400;

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.content)
    View content;
    @Bind(R.id.navigation_view)
    NavigationView navigationView;
    @Nullable
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.rv)
    RecyclerView rv;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.fab1)
    FloatingActionButton fab1;
    @Bind(R.id.fab2)
    FloatingActionButton fab2;
    private RVAdapter adapter;
    private ProgressDialog pDialog;
    private MainPresenter presenter;
    private boolean pendingIntroAnimation;
    private MenuItem menuItem = null;
    private TextView titleApp;
    private Boolean isFabOpen = false;
    private Animation fabOpen,fabClose,rotateForward,rotateBackward;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        presenter = new MainPresenter();
        presenter.atachView(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        }
        presenter.initiateViews();
        presenter.loadMovies();


    }

    @OnClick(R.id.fab1)
    public void onFab1() {
        presenter.showSnackbar("Fab 1");
    }
    @OnClick(R.id.fab2)
    public void onFab2() {
        presenter.showSnackbar("Fab 2");
    }

    @Override
    protected void onResume() {
        super.onResume();
        /** Register ourselves so that we can provide the initial value.**/
        BaseApplication.getBus().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(() -> {
            if (adapter.getItemCount() == 0) presenter.loadDataFromCache();
            return false;
        });
        if (pendingIntroAnimation) {
            pendingIntroAnimation = false;
            presenter.startIntroAnimation();
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
            case R.id.refresh_list:
                presenter.loadMovies();
                return true;
            case R.id.sorted_list:
                presenter.sortList();
                return true;
            case R.id.send_feed_back:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();
        /** Always unregister when an object no longer should be on the bus.**/
        BaseApplication.getBus().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        /** Always detach your presenter from view **/
        presenter.detachView();
        super.onDestroy();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //final List<Movie> filteredMovie = presenter.filter(newText);
        final List<Movie> filteredMovie = presenter.filterWithRx(newText);
        adapter.animateTo(filteredMovie);
        rv.scrollToPosition(0);

        return true;
    }

    @Override
    public void initializeViews() {
        /**
         * Initiate Toolbar
         * **/
        if (toolbar != null)
                setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);

            /**
             * Code using for custom toolbar
             * **/
            LayoutInflater inflater = LayoutInflater.from(this);
            View v = inflater.inflate(R.layout.view_toolbar_title_main, null);

            //if you need to customize anything else about the text, do it here.
            //I'm using a custom TextView with a custom font in my layout xml so all I need to do is set title
            titleApp  = (TextView)v.findViewById(R.id.title_toolbar);
            titleApp.setText("Boilerplate");

            //assign the view to the actionbar
            actionBar.setCustomView(v);
        }

        /**
         * Initiate NavDrawer
         * **/
        final ImageView avatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.avatar);
        avatar.setImageResource(R.drawable .ic_launcher_screen);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            presenter.showSnackbar(menuItem.getTitle() + " pressed");
            menuItem.setChecked(true);
            drawerLayout.closeDrawers();
            return true;
        });

        /**
         * Initiate Recycleview
         * **/
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        /**
         * Initiate Animation
         **/
        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        // listener floatActionBar
        fab.setOnClickListener(v -> presenter.animateFAB());

    }

    @Override
    public void animateFAB() {
        if(isFabOpen){

            fab.startAnimation(rotateBackward);
            fab1.startAnimation(fabClose);
            fab2.startAnimation(fabClose);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;

        } else {

            fab.startAnimation(rotateForward);
            fab1.startAnimation(fabOpen);
            fab2.startAnimation(fabOpen);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;

        }
    }

    @Override
    public void setItems(List<Movie> movieItems) {
        adapter = new RVAdapter(MainActivity.this, movieItems);
        rv.setAdapter(adapter);
        /**
            AlphaInRVAnimationAdapter alphaInAnimationAdapter =  new AlphaInRVAnimationAdapter(adapter);
            alphaInAnimationAdapter.setRecyclerView(rv);
            alphaInAnimationAdapter.setDuration(2000);
            rv.setAdapter(alphaInAnimationAdapter);
         **/
        adapter.setOnItemClickListener((view, movie) -> presenter.onItemClick(movie));
        presenter.startContentAnimation();
    }

    @Override
    public void showDialog() {
        pDialog = DialogFactory.createProgressDialog(this, "Loading...");
        pDialog.show();
    }

    @Override
    public void hideDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public void showSnack(String message) {
        SnackBarFactory.createCustomSnackbar(getApplicationContext(), content, message).show();
    }

    @Override
    public void startIntroAnimation() {
        fab.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_fab_size));
        int actionbarSize = UiUtils.dpToPx(56);
        toolbar.setTranslationY(-actionbarSize);
        titleApp.setTranslationY(-actionbarSize);
        menuItem.getActionView().setTranslationX(-actionbarSize);
        toolbar.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(400);
        titleApp.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(700);
        menuItem.getActionView().animate()
                .translationX(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(600);
    }

    @Override
    public void startContentAnimation() {
             fab.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(400)
                .setDuration(ANIM_DURATION_FAB)
                .start();
    }

    @Override
    public void navigateToDetail(Movie movie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_IMAGE, movie.getThumbnailUrl());
        intent.putExtra(DetailActivity.EXTRA_TITLE, movie.getTitle());
        intent.putExtra(DetailActivity.EXTRA_GENRE, movie.getGenre());


        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, findViewById(R.id.thumbnail), DetailActivity.EXTRA_IMAGE);
        ActivityCompat.startActivity(this, intent, options.toBundle());

    }

    @Subscribe
    @Override
    public void onEventBusHelper(EventBusHelper event) {
        presenter.showSnackbar(event.message);
    }

    @Override
    public Context getContext() {
        return this;
    }
}

