/**
 * Copyright (C) 2017 Google Inc and other contributors.
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
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import io.fabric.samples.cannonball.R;

/**
 * PoemPopularActivity that displays a list of tweets, showing only the tweet text.
 */
public class PoemPopularActivity extends ListActivity {

    private static final String TAG = "PoemPopularActivity";
    private static final String SEARCH_QUERY = "#cannonballapp AND pic.twitter.com AND " +
            "(#adventure OR #nature OR #romance OR #mystery)";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_list);

        setUpViews();
    }

    private void setUpViews() {
        setUpPopularList();
        setUpBack();
    }

    private void setUpPopularList() {
        SearchTimeline searchTimeline = new SearchTimeline.Builder().query(SEARCH_QUERY).build();

        final TweetTimelineListAdapter timelineAdapter = new TweetTimelineListAdapter(this, searchTimeline);
        setListAdapter(timelineAdapter);
        getListView().setEmptyView(findViewById(R.id.loading));
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

}
