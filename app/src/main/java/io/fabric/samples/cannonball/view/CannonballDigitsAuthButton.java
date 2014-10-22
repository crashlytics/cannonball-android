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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.digits.sdk.android.DigitsAuthButton;

import io.fabric.samples.cannonball.App;
import io.fabric.samples.cannonball.R;

public class CannonballDigitsAuthButton extends DigitsAuthButton {
    public CannonballDigitsAuthButton(Context context) {
        super(context);
        init();
    }

    public CannonballDigitsAuthButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }



    public CannonballDigitsAuthButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {
        if (isInEditMode()){
            return;
        }
        final Drawable phone = getResources().getDrawable(R.drawable.ic_signin_phone);
        phone.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_ATOP);
        setCompoundDrawablesWithIntrinsicBounds(phone, null, null, null);
        setBackgroundResource(R.drawable.digits_button);
        setTextSize(20);
        setTextColor(getResources().getColor(R.color.green));
        setTypeface(App.getInstance().getTypeface());
    }
}
