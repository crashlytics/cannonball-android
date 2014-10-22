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
package io.fabric.samples.cannonball.view;

import android.content.Context;
import android.graphics.Point;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import io.fabric.samples.cannonball.R;
import io.fabric.samples.cannonball.model.Theme;

public class ThemeAdapter extends ArrayAdapter<Theme> {

    public static final int HEADER_LAYOUT_HEIGHT = 93;
    private final int layout;
    private final int[] itemsDrawableId;
    private int height;
    private int width;


    public ThemeAdapter(Context context, Theme[] objects) {
        super(context, R.layout.theme_row, objects);
        layout = R.layout.theme_row;
        itemsDrawableId = new int[objects.length];
        setUpItemsDrawableId(itemsDrawableId, objects);
        setDisplaySize();

    }

    private void setUpItemsDrawableId(int[] itemsDrawableId, Theme[] objects) {
        final Random random = new Random();
        int index = 0;
        for (Theme theme : objects) {
            final SparseIntArray drawables = theme.getImageList();
            final int randomIndex = random.nextInt(drawables.size()) + 1;
            itemsDrawableId[index] = drawables.get(randomIndex);
            index++;
        }
    }

    private void setDisplaySize() {
        final WindowManager wm
                = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();
        final Point p = new Point();
        display.getSize(p);
        final Float ht_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                HEADER_LAYOUT_HEIGHT, getContext().getResources().getDisplayMetrics());
        height = (p.y - ht_px.intValue()) / Theme.values().length;
        width = p.x;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Theme currentTheme = getItem(position);
        View view = convertView;
        final ThemeHolder holder;
        if (view == null) {
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(layout, parent, false);
            holder = initThemeHolder(view);
            view.setTag(holder);
        } else {
            holder = (ThemeHolder) view.getTag();
        }
        setUpThemeHolder(position, currentTheme, holder);
        return view;
    }

    private void setUpThemeHolder(final int position, Theme currentTheme,
                                  final ThemeHolder holder) {
        holder.imageView.getLayoutParams().height = height;
        holder.imageView.post(new Runnable() {
            @Override
            public void run() {
                ImageLoader.getImageLoader().load(itemsDrawableId[position], holder.imageView);
            }
        });
        holder.textView.setText(currentTheme.getHashtag());
        holder.textView.setPadding(0, 0, 0,
                getContext().getResources().getDimensionPixelSize(
                        R.dimen.theme_name_margin_bottom));
        holder.gradientView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
    }

    private ThemeHolder initThemeHolder(View view) {
        final ThemeHolder holder = new ThemeHolder();
        holder.imageView = (ImageView) view.findViewById(R.id.theme);
        holder.textView = (TextView) view.findViewById(R.id.theme_hashtag);
        holder.gradientView = (FrameLayout) view.findViewById(R.id.gradient);
        return holder;
    }

    static class ThemeHolder {
        ImageView imageView;
        TextView textView;
        FrameLayout gradientView;
    }

}
