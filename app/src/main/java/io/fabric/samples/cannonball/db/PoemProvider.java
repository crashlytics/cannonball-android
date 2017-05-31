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
package io.fabric.samples.cannonball.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.crashlytics.android.Crashlytics;

public class PoemProvider extends ContentProvider {
    private DbHelper dbHelper;

    static final int POEMS = 1;
    static final int POEMS_ID = 2;

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(PoemContract.AUTHORITY, "poems", POEMS);
        sUriMatcher.addURI(PoemContract.AUTHORITY, "poems/#", POEMS_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return (dbHelper != null);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final long id = db.insertWithOnConflict(PoemContract.TABLE, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);

        if (id > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        } else {
            Crashlytics.log("PoemProvider: not able to insert a poem in Database");
            return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(PoemContract.TABLE);

        switch (sUriMatcher.match(uri)) {
            case POEMS_ID:
                queryBuilder.appendWhere(PoemContract.Columns.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                break;
        }

        final Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(PoemContract.TABLE);

        switch (sUriMatcher.match(uri)) {
            case POEMS:
                return 0;
            case POEMS_ID:
                final String[] args = { uri.getLastPathSegment() };
                final int count
                        = db.delete(PoemContract.TABLE, PoemContract.Columns.ID + " = ?", args);
                if (count != 0)
                    getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
