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
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import io.fabric.samples.cannonball.R;
import io.fabric.samples.cannonball.model.Theme;

public final class ImageAdapter extends PagerAdapter {
    private final WeakReference<Context> context;
    private final Theme theme;

    public ImageAdapter(Context ctx, Theme theme) {
        this.theme = theme;
        this.context = new WeakReference<Context>(ctx);

    }

    @Override
    public int getCount() {
        return theme.getImageList().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        if (context.get() != null) {
            final int drawableId = theme.getImageList().get(position + 1);
            final ImageView view = new ImageView(context.get());
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            view.setContentDescription(context.get().getResources().getString(R.string
                    .content_desc_poempic));
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setAdjustViewBounds(true);
            view.setTag(drawableId);
            container.addView(view, 0);
            view.post(new Runnable() {
                @Override
                public void run() {
                    ImageLoader.getImageLoader().load(drawableId, view);
                }
            });
            return view;
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
        object = null;
    }
}
