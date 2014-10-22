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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import io.fabric.samples.cannonball.R;

public class CountdownView extends View {
    private static final int CIRCLE_STROKE_WIDTH = 3;
    private static final int TIMER_STROKE_WIDTH = 7;

    private int color;
    private Paint thinCirclePaint;
    private Paint timeCirclePaint;
    private float countdownTime;
    private float currentTime;
    private RectF oval;

    public CountdownView(Context context) {
        super(context);
        initCountdown(null);
    }

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCountdown(attrs);
    }

    public CountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCountdown(attrs);
    }

    private void initCountdown(AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray attr
                    = getContext().obtainStyledAttributes(attrs, R.styleable.CountdownView);
            color = attr.getColor(R.styleable.CountdownView_color, R.color.green);
            countdownTime = attr.getFloat(R.styleable.CountdownView_countdownTime, 60);
            currentTime = attr.getFloat(R.styleable.CountdownView_currentTime, 60);
            attr.recycle();
        } else {
            color = getResources().getColor(R.color.green);
            countdownTime = 60;
            currentTime = 60;
        }

        thinCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thinCirclePaint.setColor(color);
        thinCirclePaint.setStrokeWidth(CIRCLE_STROKE_WIDTH);
        thinCirclePaint.setStyle(Paint.Style.STROKE);

        timeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeCirclePaint.setColor(getResources().getColor(R.color.green));
        timeCirclePaint.setStrokeWidth(TIMER_STROKE_WIDTH);
        timeCirclePaint.setStyle(Paint.Style.STROKE);

        oval = new RectF();

        setFocusable(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int mMeasuredWidth = getMeasuredWidth();
        final int mMeasuredHeight = getMeasuredHeight();

        final int left   = getPaddingStart() + TIMER_STROKE_WIDTH;
        final int top    = getPaddingTop() + TIMER_STROKE_WIDTH;
        final int right  = mMeasuredWidth - getPaddingEnd() - TIMER_STROKE_WIDTH;
        final int bottom = mMeasuredHeight - getPaddingBottom() - TIMER_STROKE_WIDTH;

        oval.set(left, top, right, bottom);

        canvas.drawArc(oval, -90, 360, false, thinCirclePaint);

        final float angle = (360 * ((countdownTime - currentTime) / countdownTime));
        canvas.drawArc(oval, angle - 90, 360 - angle, false, timeCirclePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measuredWidth = measure(widthMeasureSpec);
        final int measuredHeight = measure(heightMeasureSpec);

        final int d = Math.min(measuredHeight, measuredWidth);

        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec) {
        int result;

        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            result = 50;
        } else {
            result = specSize;
        }

        return result;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        thinCirclePaint.setColor(color);
        timeCirclePaint.setColor(color);
        invalidate();
    }

    public float getCountdownTime() {
        return countdownTime;
    }

    public void setCountdownTime(float countdownTime) {
        this.countdownTime = countdownTime;
        invalidate();
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(float currentTime) {
        this.currentTime = currentTime;
        invalidate();
    }
}
