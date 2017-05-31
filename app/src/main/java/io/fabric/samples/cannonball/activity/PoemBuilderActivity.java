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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.SparseIntArray;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.fabric.samples.cannonball.App;
import io.fabric.samples.cannonball.AppService;
import io.fabric.samples.cannonball.R;
import io.fabric.samples.cannonball.model.Theme;
import io.fabric.samples.cannonball.model.WordBank;
import io.fabric.samples.cannonball.view.CountdownView;
import io.fabric.samples.cannonball.view.FlowLayout;
import io.fabric.samples.cannonball.view.ImageAdapter;

public class PoemBuilderActivity extends Activity {
    public static final String KEY_THEME = "Theme";

    private static final String TAG = "PoemBuilder";
    private static final int PLAY_TIME = 60;
    private static final int PLACEHOLDER_POSITION = -1;

    private Theme poemTheme;
    private FlowLayout poemContainer;
    private FlowLayout wordsContainer;
    private ViewPager poemImagePager;
    private WordBank wordBank;
    private TextView placeholder;
    private PoemCreatedReceiver poemCreatedReceiver;
    private CountDownTimer countDown;
    private CountdownView countdownView;
    private TextView countdownText;
    private TextView shuffleText;
    private ImageView tick;
    private List<String> suffixes;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private boolean areCrashesEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_builder);

        areCrashesEnabled = App.getInstance().areCrashesEnabled();
        poemTheme = (Theme) getIntent().getSerializableExtra(KEY_THEME);

        setUpViews();
    }

    private void setUpViews() {
        setUpCountDown();
        setUpWordContainers();
        setUpBack();
        setUpShuffle();
        setUpSavePoem();
    }

    private void setUpSavePoem() {
        // Tick button, when you are done
        tick = (ImageView) findViewById(R.id.tick);
        tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crashlytics.log("PoemBuilder: clicked to save poem");
                countDown.cancel();
                createPoem();
            }
        });
    }

    private void setUpShuffle() {
        // Shuffle button
        shuffleText = (TextView) findViewById(R.id.shuffle);
        shuffleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffleWords();
                Answers.getInstance().logCustom(new CustomEvent("shuffled words"));
            }
        });
    }

    private void setUpBack() {
        // go back if clicked
        final ImageView backButton = (ImageView) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setUpWordContainers() {
        // Setting up the word containers with random words
        wordsContainer = (FlowLayout) findViewById(R.id.words_container);
        wordsContainer.setOnDragListener(new PanelDragListener());
        poemContainer = (FlowLayout) findViewById(R.id.poem_container);
        poemContainer.setOnDragListener(new PanelDragListener());
        wordBank = new WordBank(getApplicationContext(), poemTheme);
        shuffleWords();

        poemImagePager = (ViewPager) findViewById(R.id.poem_image_pager);
        poemImagePager.setAdapter(new ImageAdapter(getApplicationContext(), poemTheme));
        poemContainer.setOnTouchListener(new ImageTouchListener());

        // Placeholder used by drag and drop
        placeholder = (TextView) findViewById(R.id.placeholder);
        placeholder.setText("");
        final int PLACEHOLDER = 42;
        placeholder.setTag(PLACEHOLDER);

        suffixes = Arrays.asList(getResources().getStringArray(R.array.wordbank_suffixes));
    }

    private void setUpCountDown() {
        countdownView = (CountdownView) findViewById(R.id.countdown_spinner_view);
        countdownText = (TextView) findViewById(R.id.countdown_count);
        countDown = createCountdown(PLAY_TIME);
    }

    @Override
    public void onBackPressed() {
        Crashlytics.log("PoemBuilder: getting back, user cancelled the poem creation");
        Answers.getInstance().logCustom(new CustomEvent("gave up building a poem"));
        super.onBackPressed();
        countDown.cancel();
    }

    private void createPoem() {
        if (poemContainer.getChildCount() > 0) {
            final String poemText = getPoemText();
            final SparseIntArray imgList = poemTheme.getImageList();
            // the line below seems weird, but relies on the fact that the index of SparseIntArray could be any integer
            final int poemImage = imgList.keyAt(imgList.indexOfValue(imgList.get(poemImagePager.getCurrentItem() + 1)));
            Crashlytics.setString(App.CRASHLYTICS_KEY_POEM_TEXT, poemText);
            Crashlytics.setInt(App.CRASHLYTICS_KEY_POEM_IMAGE, poemImage);

            Answers.getInstance().logCustom(new CustomEvent("clicked save poem")
                    .putCustomAttribute("poem size", poemText.length())
                    .putCustomAttribute("poem theme", poemTheme.getDisplayName())
                    .putCustomAttribute("poem image", poemImage));

            AppService.createPoem(getApplicationContext(),
                    poemText,
                    poemImage,
                    poemTheme.getDisplayName(),
                    dateFormat.format(Calendar.getInstance().getTime()));
        } else {
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.toast_wordless_poem), Toast.LENGTH_SHORT)
                    .show();
            Crashlytics.log("PoemBuilder: User tried to create poem without words on it");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter intentFilter = new IntentFilter(App.BROADCAST_POEM_CREATION);
        poemCreatedReceiver = new PoemCreatedReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(poemCreatedReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(poemCreatedReceiver);
    }

    class PoemCreatedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(App.BROADCAST_POEM_CREATION_RESULT, false)) {
                Crashlytics.log("PoemBuilder: poem saved, receiver called");
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_poem_created), Toast.LENGTH_SHORT)
                        .show();
                final Intent i = new Intent(getApplicationContext(), PoemHistoryActivity.class);
                i.putExtra(ThemeChooserActivity.IS_NEW_POEM, true);
                startActivity(i);
            } else {
                Crashlytics.log("PoemBuilder: error when saving poem");
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_poem_error), Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
        }
    }

    private void shuffleWords() {
        wordsContainer.removeAllViews();

        final List<String> wordList = wordBank.loadWords();
        for (String w : wordList) {
            final TextView wordView
                    = (TextView) getLayoutInflater().inflate(R.layout.word, null, true);
            wordView.setText(w);
            wordView.setOnTouchListener(new WordTouchListener());
            wordView.setTag(w);
            wordsContainer.addView(wordView);
        }

        Crashlytics.setInt(App.CRASHLYTICS_KEY_WORDBANK_COUNT, wordList.size());
    }

    private String getPoemText() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < poemContainer.getChildCount(); i++) {
            final CharSequence word = ((TextView) poemContainer.getChildAt(i)).getText();
            if (!suffixes.contains(word) && i != 0) sb.append(" ");
            sb.append(word);
        }
        return sb.toString();
    }

    final class ImageTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            poemImagePager.onTouchEvent(event);
            return true;
        }
    }

    private CountDownTimer createCountdown(long remainingSeconds) {
        countdownView.setCurrentTime(remainingSeconds);
        return new CountDownTimer(remainingSeconds * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                final long secsToFinish = millisUntilFinished / 1000;
                if (secsToFinish == 10) {
                    countdownText.setTextColor(getResources().getColor(R.color.red_font));
                    countdownView.setColor(getResources().getColor(R.color.red_font));
                    shuffleText.setTextColor(getResources().getColor(R.color.red_font));
                }
                countdownText.setText("" + secsToFinish);
                countdownView.setCurrentTime(secsToFinish);
                Crashlytics.setLong(App.CRASHLYTICS_KEY_COUNTDOWN, secsToFinish);
            }

            public void onFinish() {
                Crashlytics.log("PoemBuilder: countdown timer ended, saving poem...");
                if (poemContainer.getChildCount() > 0) {
                    createPoem();
                } else {
                    Crashlytics.log("PoemBuilder: Countdown finishes counting, no words added");
                    onBackPressed();
                }

            }

        }.start();
    }

    private final class WordTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN &&
                    motionEvent.getPointerId(0) == 0) {
                // the getPointerId is to avoid executing something if someone decides to drag with
                // 2 fingers
                final ClipData data = ClipData.newPlainText("word", (String) view.getTag());
                final View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.GONE);
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // it is a tap, if the touch was quick enough to cancel the execution
                // of ACTION_DOWN, we should consider as a tap too.
                final FlowLayout parent = (FlowLayout) view.getParent();
                if (parent.getId() == R.id.words_container) {
                    moveView(wordsContainer, poemContainer, view);
                } else {
                    moveView(poemContainer, wordsContainer, view);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    class PanelDragListener implements View.OnDragListener {
        private static final int TAP_RANGE = 200;

        private long dragStartedAt;
        private int finalPosition;
        private ArrayList<TextView> words = new ArrayList<TextView>();

        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // Determines if this View can accept the dragged data
                    // Called on all views that has this listener set
                    final ClipDescription clipDescription = event.getClipDescription();
                    if (clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        dragStartedAt = System.currentTimeMillis();
                        words.clear();
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    if (words.size() == 0)
                        fillWordsList((FlowLayout) v);
                    finalPosition = pointToWordIndex(event.getX(), event.getY());
                    if (PLACEHOLDER_POSITION != finalPosition) {
                        final ViewGroup parent = (ViewGroup) placeholder.getParent();
                        if (parent != null)
                            parent.removeView(placeholder);
                        ((FlowLayout) v).addView(placeholder, finalPosition);
                        placeholder.setVisibility(View.VISIBLE);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    // Called on the view that received the drop
                    final View word = (View) event.getLocalState();
                    final FlowLayout from = (FlowLayout) word.getParent();
                    FlowLayout to = (FlowLayout) v;

                    if (System.currentTimeMillis() - dragStartedAt > TAP_RANGE) {
                        // it was a drag
                        if (finalPosition >= words.size()) {
                            moveView(from, to, word);
                        } else {
                            moveView(from, to, word, finalPosition);
                        }
                    } else { // it is a tap
                        final int viewId;
                        if (areCrashesEnabled) {
                            // Crashlytics:
                            // To generate the crash, open the app and drag a word really quick
                            // from the Word Bank to the Poem (in less than TAP_RANGE ms)
                            viewId = to.getId();
                            Crashlytics.log("PoemBuilder: An enabled crash will execute");
                        } else {
                            viewId = from.getId();
                        }
                        if (viewId == R.id.words_container) {
                            to = poemContainer;
                            moveView(wordsContainer, poemContainer, word);
                        } else {
                            to = wordsContainer;
                            moveView(poemContainer, wordsContainer, word);
                        }
                    }

                    // change word color if it is on poem container or wordscontainer
                    if (to.equals(poemContainer)) {
                        word.setBackgroundResource(R.drawable.word_at_poem);
                        ((TextView) word).setTextColor(getResources().getColor(R.color.white));
                        word.invalidate();
                    } else if (to.equals(wordsContainer)) {
                        word.setBackgroundResource(R.drawable.word);
                        ((TextView) word).setTextColor(getResources().getColor(R.color.green));
                        word.invalidate();
                    }

                    final ViewGroup parent = (ViewGroup) placeholder.getParent();
                    if (parent != null)
                        parent.removeView(placeholder);
                    placeholder.setVisibility(View.GONE);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    // Called on all views that has this listener set
                    if (!event.getResult()) {
                        // what to do if the view is dropped in the wrong place
                        final View localState = (View) event.getLocalState();
                        localState.setVisibility(View.VISIBLE);
                        final ViewGroup placeholderParent = (ViewGroup) placeholder.getParent();
                        if (placeholderParent != null)
                            placeholderParent.removeView(placeholder);
                        placeholder.setVisibility(View.GONE);
                    }
                    return true;
                default:
                    break;
            }
            return false;
        }

        private void fillWordsList(FlowLayout view) {
            for (int i = 0; i < view.getChildCount(); i++) {
                final TextView word = (TextView) view.getChildAt(i);
                if (word.getTag() instanceof String)
                    words.add(word);
            }
        }

        private Integer pointToWordIndex(float x, float y) {
            float startX = 0;
            boolean reachedY = false;
            final int count = words.size();
            // Margin top and bottom between words, see flowlayout
            final int space = getResources().getDimensionPixelSize(R.dimen.word_padding_vertical);

            for (int i = 0; i < count; i++) {
                final TextView word = words.get(i);
                final float wordX = word.getLeft();
                final float wordY = word.getTop();

                if (y > wordY - space && y < (wordY + word.getHeight() + space)) {
                    if (x > startX && x < (wordX + Math.round(word.getWidth() / 2))) {
                        return i;
                    }
                    startX = (wordX + (word.getWidth() / 2));
                    reachedY = true;
                } else if (reachedY) {
                    return i;
                }
            }
            return count;
        }
    }

    private void moveView(ViewGroup from, ViewGroup to, View object) {
        from.removeView(object);
        to.addView(object);
        object.setVisibility(View.VISIBLE);
        onWordMoved();
    }

    private void moveView(ViewGroup from, ViewGroup to, View object, int position) {
        from.removeView(object);
        to.addView(object, position);
        object.setVisibility(View.VISIBLE);
        onWordMoved();
    }

    private void onWordMoved() {
        if (poemContainer.getChildCount() > 0) {
            tick.setColorFilter(getResources().getColor(R.color.green));
        } else {
            tick.setColorFilter(getResources().getColor(R.color.grayish_blue));
        }
    }
}
