package io.fabric.samples.cannonball.view;

/*
 * Adapted from https://code.google.com/p/android-imagedownloader/
 */

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ImageLoader {
    private static class Loader {
        static ImageLoader INSTANCE = new ImageLoader();
    }

    public static ImageLoader getImageLoader() {
        return Loader.INSTANCE;
    }

    private ImageLoader() {

    }

    /**
     * Loads the specified resource and binds it to the provided ImageView.
     *
     * @param drawableId The resource id of the image to load.
     * @param imageView The ImageView to bind the loaded image to.
     */
    public void load(int drawableId, ImageView imageView) {
        if (cancelPotentialDownload(drawableId, imageView)) {
            BackgroundTask task = new BackgroundTask(imageView);
            BackgroundTaskDrawable downloadedDrawable = new BackgroundTaskDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(drawableId);
        }
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private BackgroundTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof BackgroundTaskDrawable) {
                BackgroundTaskDrawable downloadedDrawable = (BackgroundTaskDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    /**
     * Returns true if the current load has been canceled or if there was no load in
     * progress on this image view.
     * Returns false if the load in progress deals with the same resource. The load is not
     * stopped in that case.
     */
    private boolean cancelPotentialDownload(int drawableId, ImageView imageView) {
        BackgroundTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            int bitmapUrl = bitmapDownloaderTask.drawableId;
            if (bitmapUrl != drawableId) {
                bitmapDownloaderTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * The actual AsyncTask that will asynchronously run the image.
     */
    class BackgroundTask extends AsyncTask<Integer, Void, Bitmap> {
        private int drawableId;
        private final WeakReference<ImageView> imageViewReference;
        private int w;
        private int h;

        public BackgroundTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                w = imageView.getMeasuredWidth();
                h = imageView.getMeasuredHeight();
            }
        }

        /**
         * Actual load method.
         */
        @Override
        protected Bitmap doInBackground(Integer... params) {
            drawableId = params[0];
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                final Context context = imageView.getContext();
                return decodeSampledBitmapFromResource(context.getResources(), drawableId, h, w);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                BackgroundTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                if (this == bitmapDownloaderTask){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /**
     * A fake Drawable that will be attached to the imageView while the task is in progress.
     *
     * <p>Contains a reference to the actual task, so that a task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the finish order.</p>
     */
    static class BackgroundTaskDrawable extends ColorDrawable {
        private final WeakReference<BackgroundTask> bitmapDownloaderTaskReference;

        public BackgroundTaskDrawable(BackgroundTask bitmapDownloaderTask) {
            super(Color.TRANSPARENT);
            bitmapDownloaderTaskReference =
                    new WeakReference<BackgroundTask>(bitmapDownloaderTask);
        }

        public BackgroundTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}