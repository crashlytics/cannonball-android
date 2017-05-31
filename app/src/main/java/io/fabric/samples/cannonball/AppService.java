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
package io.fabric.samples.cannonball;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.crashlytics.android.Crashlytics;

import io.fabric.samples.cannonball.db.PoemContract;

public class AppService extends IntentService {

    private static final String TAG = "AppService";
    private static final String PARAM_OP = "AppService.OP";
    private static final int OP_CREATE_POEM = -1;
    private static final int OP_DELETE_POEM = -2;

    public static void createPoem(Context ctx, String text, int drawableId, String theme,
            String createdAt) {
        final Intent i = new Intent(ctx, AppService.class)
                .putExtra(PARAM_OP, OP_CREATE_POEM)
                .putExtra(PoemContract.Columns.TEXT, text)
                .putExtra(PoemContract.Columns.IMAGE, drawableId)
                .putExtra(PoemContract.Columns.THEME, theme)
                .putExtra(PoemContract.Columns.CREATED_AT, createdAt);
        ctx.startService(i);
    }

    public static void deletePoem(Context ctx, Integer id) {
        final Intent i = new Intent(ctx, AppService.class)
                .putExtra(PARAM_OP, OP_DELETE_POEM)
                .putExtra(PoemContract.Columns.ID, id);
        ctx.startService(i);
    }

    public AppService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int op = intent.getIntExtra(PARAM_OP, 0);
        switch (op) {
            case OP_CREATE_POEM:
                doCreatePoem(
                        intent.getStringExtra(PoemContract.Columns.TEXT),
                        intent.getIntExtra(PoemContract.Columns.IMAGE, 0),
                        intent.getStringExtra(PoemContract.Columns.THEME),
                        intent.getStringExtra(PoemContract.Columns.CREATED_AT)
                );
                break;
            case OP_DELETE_POEM:
                doDeletePoem(
                        intent.getIntExtra(PoemContract.Columns.ID, 0)
                );
                break;
            default:
                Crashlytics.log("AppService: Unexpected op: " + op);
        }
    }


    private void doCreatePoem(String text, int drawableId, String theme, String createdAt) {
        final ContentValues values = new ContentValues();

        values.put(PoemContract.Columns.TEXT, text);
        values.put(PoemContract.Columns.IMAGE, drawableId);
        values.put(PoemContract.Columns.THEME, theme);
        values.put(PoemContract.Columns.CREATED_AT, createdAt);

        final Uri newUri = getContentResolver().insert(PoemContract.CONTENT_URI, values);

        final Intent localIntent = new Intent(App.BROADCAST_POEM_CREATION);
        if (newUri != null) {
            localIntent.putExtra(App.BROADCAST_POEM_CREATION_RESULT, true);
        } else {
            localIntent.putExtra(App.BROADCAST_POEM_CREATION_RESULT, false);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void doDeletePoem(Integer id) {
        final Intent localIntent = new Intent(App.BROADCAST_POEM_DELETION);
        final Uri uri = Uri.parse(PoemContract.CONTENT_URI + "/" + id);

        if (getContentResolver().delete(uri, null, null) != 0) {
            localIntent.putExtra(App.BROADCAST_POEM_DELETION_RESULT, true);
        } else {
            localIntent.putExtra(App.BROADCAST_POEM_DELETION_RESULT, false);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
