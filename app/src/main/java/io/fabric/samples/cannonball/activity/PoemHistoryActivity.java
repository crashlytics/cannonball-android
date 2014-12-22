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

import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.fabric.samples.cannonball.App;
import io.fabric.samples.cannonball.AppService;
import io.fabric.samples.cannonball.R;
import io.fabric.samples.cannonball.db.PoemContract;
import io.fabric.samples.cannonball.model.Theme;
import io.fabric.samples.cannonball.view.AvenirTextView;
import io.fabric.samples.cannonball.view.ImageLoader;

public class PoemHistoryActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "PoemHistory";
    private PoemCursorAdapter adapter;
    private OnShareClickListener shareListener;
    private OnDeleteClickListener deleteListener;
    private PoemDeletedReceiver poemDeletedReceiver;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent().getBooleanExtra(ThemeChooserActivity.IS_NEW_POEM, false)) {
            final Intent intent = new Intent(getApplicationContext(), ThemeChooserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poem_history);

        setUpViews();

        getLoaderManager().initLoader(0, null, this);
    }

    private void setUpViews() {
        setUpBack();
        setUpPoemList();
    }

    private void setUpPoemList() {
        shareListener = new OnShareClickListener();
        deleteListener = new OnDeleteClickListener();

        final ListView poemsList = (ListView) findViewById(R.id.poem_history_list);

        adapter = new PoemCursorAdapter(
                getApplicationContext(),
                null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        poemsList.setAdapter(adapter);
    }

    private void setUpBack() {
        final ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter(App.BROADCAST_POEM_DELETION);
        poemDeletedReceiver = new PoemDeletedReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(poemDeletedReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(poemDeletedReceiver);
    }

    class PoemCursorAdapter extends CursorAdapter {
        private final LayoutInflater inflater;

        PoemCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(R.layout.listview_poem, parent, false);
        }

        @Override
        public void bindView(View view, Context context, final Cursor cursor) {
            final ImageView image = (ImageView) view.findViewById(R.id.poem_image);
            // TODO optimize that to avoid getIdentifier call
            try {
                final Theme t = Theme.valueOf(cursor.getString(cursor.getColumnIndex(PoemContract.Columns.THEME)).toUpperCase());
                final int poemImage = t.getImageList().get(cursor.getInt(cursor.getColumnIndex(PoemContract.Columns.IMAGE)));
                image.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageLoader.getImageLoader().load(poemImage, image);
                    }
                });
            } catch (Resources.NotFoundException ex) {
                //In case an identifier is removed from the list
            }

            final ImageView shareImageView = (ImageView) view.findViewById(R.id.share);
            shareImageView.setTag(cursor.getInt(cursor.getColumnIndex(PoemContract.Columns.ID)));
            shareImageView.setOnClickListener(shareListener);

            final ImageView deleteImageView = (ImageView) view.findViewById(R.id.delete);
            deleteImageView.setTag(cursor.getInt(cursor.getColumnIndex(PoemContract.Columns.ID)));
            deleteImageView.setOnClickListener(deleteListener);

            AvenirTextView text = (AvenirTextView) view.findViewById(R.id.poem_text);
            text.setText(cursor.getString(cursor.getColumnIndex(PoemContract.Columns.TEXT)));

            text = (AvenirTextView) view.findViewById(R.id.poem_theme);
            text.setText("#" + cursor.getString(cursor.getColumnIndex(PoemContract.Columns.THEME)));
        }
    }

    class OnShareClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final RelativeLayout originalPoem = (RelativeLayout) v.getParent();

            final LinearLayout shareContainer = (LinearLayout) findViewById(R.id.share_container);
            if (shareContainer.getChildCount() > 0) {
                shareContainer.removeAllViews();
            }
            final RelativeLayout poem
                    = (RelativeLayout) getLayoutInflater().inflate(R.layout.listview_poem, null);

            final ImageView share = (ImageView) poem.findViewById(R.id.share);
            share.setVisibility(View.GONE);
            final ImageView delete = (ImageView) poem.findViewById(R.id.delete);
            delete.setVisibility(View.GONE);

            TextView text = (TextView) poem.findViewById(R.id.poem_text);
            TextView originalText = (TextView) originalPoem.findViewById(R.id.poem_text);
            text.setTextSize(getResources().getDimensionPixelSize(R.dimen.share_text_size));
            final int padding = getResources().getDimensionPixelSize(R.dimen.share_text_padding);
            text.setPadding(padding, padding, padding,
                    getResources().getDimensionPixelSize(R.dimen.share_text_margin_bottom));
            final RelativeLayout.LayoutParams params
                    = (RelativeLayout.LayoutParams) text.getLayoutParams();
            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            text.setLayoutParams(params);
            text.setText(originalText.getText());

            text = (TextView) poem.findViewById(R.id.poem_theme);
            originalText = (TextView) originalPoem.findViewById(R.id.poem_theme);
            text.setTextSize(getResources().getDimensionPixelSize(R.dimen.share_text_size));
            text.setText(originalText.getText());

            final ImageView poemImage = (ImageView) poem.findViewById(R.id.poem_image);
            final ImageView originalPoemImage
                    = (ImageView) originalPoem.findViewById(R.id.poem_image);
            poemImage.setImageDrawable(originalPoemImage.getDrawable());
            poem.setTag(v.getTag());
            shareContainer.addView(poem);

            new SharePoemTask().execute(poem);
        }
    }

    class OnDeleteClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AppService.deletePoem(getApplicationContext(), (Integer) v.getTag());
        }
    }

    class SharePoemTask extends AsyncTask<View, Void, Boolean> {
        @Override
        protected Boolean doInBackground(View... views) {
            final View poem = views[0];
            boolean result = false;

            if (App.isExternalStorageWritable()) {
                // generating image
                final Bitmap bitmap = Bitmap.createBitmap(
                        getResources().getDimensionPixelSize(R.dimen.share_width_px),
                        getResources().getDimensionPixelSize(R.dimen.share_height_px),
                        Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                poem.draw(canvas);

                final File picFile = App.getPoemFile("poem_" + poem.getTag() + ".jpg");

                try {
                    picFile.createNewFile();
                    final FileOutputStream picOut = new FileOutputStream(picFile);
                    final boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, picOut);
                    if (saved) {
                        final CharSequence hashtag
                                = ((TextView) poem.findViewById(R.id.poem_theme)).getText();

                        final Uri imageUri = Uri.parse(picFile.getAbsolutePath());
                        result = true;
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getResources().getString(R.string.toast_share_error),
                                Toast.LENGTH_SHORT).show();
                    }
                    picOut.close();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_share_error),
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                poem.destroyDrawingCache();
            } else {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_share_error),
                        Toast.LENGTH_SHORT).show();
            }

            return result;
        }

    }

    class PoemDeletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(App.BROADCAST_POEM_DELETION_RESULT, false)) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_poem_deleted),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_poem_delete_error),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getApplicationContext(), PoemContract.CONTENT_URI, null, null, null,
                PoemContract.SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
