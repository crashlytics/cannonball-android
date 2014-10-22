/**
 * Copyright (C) 2014 Twitter Inc and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric.samples.cannonball.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.tweetui.TweetViewAdapter;

import java.util.List;

import io.fabric.samples.cannonball.App;
import io.fabric.samples.cannonball.R;

/**
 * PoemPopularActivity that displays a list of tweets, showing only the tweet text.
 */
public class PoemPopularActivity extends ListActivity {

    private static final String TAG = "PoemPopularActivity";
    private static final int SEARCH_COUNT = 20;
    private static final String SEARCH_RESULT_TYPE = "recent";
    private static final String SEARCH_QUERY = "#cannonballapp AND pic.twitter.com AND " +
            "(#adventure OR #nature OR #romance OR #mystery)";
    private TweetViewAdapter adapter;
    private boolean flagLoading;
    private boolean endOfSearchResults;
    private long maxId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);

        setUpViews();

        loadTweets();
    }

    private void setUpViews() {
        setUpBack();
        setUpPopularList();
    }

    private void setUpPopularList() {
        adapter = new TweetViewAdapter(PoemPopularActivity.this);
        setListAdapter(adapter);

        getListView().setEmptyView(findViewById(R.id.loading));
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if ((firstVisibleItem + visibleItemCount == totalItemCount) &&
                        totalItemCount != 0) {
                    Crashlytics.log("PopularTweets: scrolled to end of Tweet list");
                    if (!flagLoading && !endOfSearchResults) {
                        flagLoading = true;
                        loadTweets();
                    }
                }
            }
        });
    }

    private void setUpBack() {
        final ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crashlytics.log("PopularTweets: getting back to theme chooser");
                onBackPressed();
            }
        });
    }

    private void loadTweets() {
        Crashlytics.log("PopularTweets: loading more tweets");
        setProgressBarIndeterminateVisibility(true);

        final SearchService service = Twitter.getApiClient().getSearchService();
        service.tweets(SEARCH_QUERY, null, null, null, SEARCH_RESULT_TYPE, SEARCH_COUNT, null, null,
                maxId, true, new Callback<Search>() {
                    @Override
                    public void success(Result<Search> searchResult) {
                        Crashlytics.setLong(App.CRASHLYTICS_KEY_SEARCH_COUNT,
                                searchResult.data.searchMetadata.count);
                        setProgressBarIndeterminateVisibility(false);
                        final List<Tweet> tweets = searchResult.data.tweets;
                        adapter.getTweets().addAll(tweets);
                        adapter.notifyDataSetChanged();
                        if (tweets.size() > 0) {
                            maxId = tweets.get(tweets.size() - 1).id - 1;
                        } else {
                            endOfSearchResults = true;
                        }
                        flagLoading = false;
                    }

                    @Override
                    public void failure(TwitterException error) {
                        Crashlytics.logException(error);

                        setProgressBarIndeterminateVisibility(false);
                        Toast.makeText(PoemPopularActivity.this,
                                getResources().getString(R.string.toast_retrieve_tweets_error),
                                Toast.LENGTH_SHORT).show();

                        flagLoading = false;
                    }
                }
        );
    }

}
