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
package io.fabric.samples.cannonball.model;

import android.util.SparseIntArray;

import java.util.Locale;

public enum Theme {

    ADVENTURE(0, ThemeDrawables.ADVENTURE_DRAWABLE),
    MYSTERY(1, ThemeDrawables.MYSTERY_DRAWABLE),
    NATURE(2, ThemeDrawables.NATURE_DRAWABLE),
    ROMANCE(3, ThemeDrawables.ROMANCE_DRAWABLE);


    private final int id;
    private final SparseIntArray imageList;
    private final String themeName;


    Theme(int id, SparseIntArray imageList) {
        this.id = id; // index in strings.xml array, see themes array
        this.themeName = name().toLowerCase(Locale.US);
        this.imageList = imageList;
    }

    public int getId() {
        return id;
    }

    public SparseIntArray getImageList() {
        return imageList;
    }

    public String getDisplayName() {
        return themeName;
    }

    public String getHashtag() {
        return "#" + themeName;
    }
}

