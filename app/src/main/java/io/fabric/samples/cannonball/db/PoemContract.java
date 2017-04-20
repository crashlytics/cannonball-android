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

import android.net.Uri;
import android.provider.BaseColumns;

public final class PoemContract {
    private PoemContract() {}

    public static final String AUTHORITY = "io.fabric.samples.cannonball.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/poems");

    public static final String DB_NAME = "cannonball.db";
    public static final int DB_VERSION = 3;
    public static final String TABLE = "poems";
    public static final String SORT_ORDER = Columns.CREATED_AT + " desc";

    public static final String DATABASE_CREATE_POEMS = "create table "
            + TABLE + "("
            + Columns.ID + " integer primary key autoincrement, "
            + Columns.TEXT + " text not null, "
            + Columns.IMAGE + " integer not null, "
            + Columns.THEME + " text not null, "
            + Columns.CREATED_AT + " text not null "
            + ");";

    public static final class Columns implements BaseColumns {
        private Columns() {}
        public static final String ID = "_id";
        public static final String TEXT = "text";
        public static final String IMAGE = "image";
        public static final String THEME = "theme";
        public static final String CREATED_AT = "created_at";

    }
}
