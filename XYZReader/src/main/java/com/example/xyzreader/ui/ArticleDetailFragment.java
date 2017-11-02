package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.XYZReaderPreferences;
import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.photo) ImageView mPhotoView;
    @BindView(R.id.share_fab) FloatingActionButton mFab;
    @BindView(R.id.article_title) TextView mTitleView;
    @BindView(R.id.article_byline) TextView mBylineView;
    @BindView(R.id.article_body) TextView mBodyView;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        ButterKnife.bind(this, mRootView);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent();
                startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
            }
        });

        bindViews();
        return mRootView;
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        mBylineView.setMovementMethod(new LinkMovementMethod());

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            mTitleView.setText(title);
            if (getActivity() != null) {
                getActivity().setTitle(title);
                mToolbar.setTitle(title);
                Log.i(TAG, "bindViews: Setting title to " + title);
            }
            Date publishedDate = parsePublishedDate();
            String author = mCursor.getString(ArticleLoader.Query.AUTHOR);
            final String datePart;
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                datePart = DateUtils.getRelativeTimeSpanString(
                        publishedDate.getTime(),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString();
            } else {
                // If date is before 1902, just show the string
                datePart = outputFormat.format(publishedDate);

            }
            mBylineView.setText(getString(R.string.sub_heading, datePart, author));
            String article = mCursor.getString(ArticleLoader.Query.BODY);

            // Shorten article for performance reasons.
            if (article.length() > 10_000) {
                article = article.substring(0, 10_000);
            }

            // The articles include newlines for manual line wrapping. Because the screen width of
            // devices may need a different line wrapping, any single newline is ignored. Only two
            // consecutive newlines are assumed to be a new paragraph.
            mBodyView.setText(Html.fromHtml(article.replaceAll("(\r\n\r\n)", "<br /><br />")));

            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            GlideApp.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_cloud_download_black_24dp)
                    .into(mPhotoView);
        } else {
            mRootView.setVisibility(View.GONE);
            mTitleView.setText("N/A");
            mBylineView.setText("N/A");
            mBodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    /**
     * Called by the parent activity to notify the fragment that it has become selected.
     *
     * <p>The ViewPager pre-loads fragments. Therefore the Fragment.resume() method may be called
     * when the fragment is still hidden. Typically, the ViewPager has two fragments loaded to
     * make the swipe transition fluid.
     */
    public void onHasBecomeSelected() {
        Log.i(TAG, "onHasBecomeSelected: The fragment has become selected. " + mItemId);

        // Initialize toolbar.
        mToolbar.post(new Runnable() {
            @Override
            public void run() {
                getActivityCast().setSupportActionBar(mToolbar);
                ActionBar supportActionBar = getActivityCast().getSupportActionBar();
                if (supportActionBar != null) {
                    supportActionBar.setDisplayHomeAsUpEnabled(true);
                    supportActionBar.setDisplayShowHomeEnabled(true);
                }
            }
        });

        // Show swipe hint snack bar for first time.
        if (!XYZReaderPreferences.hasSeenSwipeSnackBar(getActivity())) {
            final Snackbar snackbar = Snackbar.make(
                    mRootView,
                    R.string.swipe_hint,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.dismiss_action, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    XYZReaderPreferences.setHasSeenSwipeSnackBar(getActivity(), true);
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }
    }
}
