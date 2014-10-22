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

import android.content.Context;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WordBank {
    private final ArrayList<String> words;
    private final SparseArray<List<String>> groups;
    private int groupSize;
    private int currentGroup = 0;

    // configure depending on the size of word container
    private static final int MAX_WORDS_IN_GROUP = 12;

    public WordBank(Context context, Theme theme) {
        groups = new SparseArray<List<String>>();
        final int id = context.getResources().getIdentifier("wordbank_" + theme.getDisplayName(),
                "array", context.getPackageName());
        words = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(id)));

        shuffle();
    }

    public List<String> loadWords() {
        final List<String> words = groups.get(currentGroup);
        incrementCurrentGroup();

        return words;
    }

    private void shuffle() {
        // Adding same size random set of words in each group that will be reloaded
        // when user clicks Shuffle in the UI

        final int size = words.size();
        groupSize = size / MAX_WORDS_IN_GROUP;
        final Random random = new Random();
        currentGroup = 0;
        final ArrayList<String> clonedWords = (ArrayList<String>) words.clone();

        for (int i = 0; i < groupSize; i++) {
            groups.put(i, new ArrayList<String>());
        }

        for (int i = clonedWords.size(); i > 0; i--) {
            final int index = random.nextInt(i);

            final List<String> list = groups.get(currentGroup);
            list.add(clonedWords.get(index));
            clonedWords.remove(index);

            incrementCurrentGroup();
        }

        currentGroup = 0;
    }

    private void incrementCurrentGroup() {
        currentGroup++;
        currentGroup = (currentGroup >= groupSize ? 0 : currentGroup);
    }

}
