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

package io.fabric.samples.cannonball.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

import io.fabric.samples.cannonball.App;

public class InitialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TwitterSession session = Twitter.getSessionManager().getActiveSession();
        if (session != null) {
            startThemeActivity(session);
        } else {
            startLoginActivity();
        }
    }

    private void startThemeActivity(TwitterSession session) {
        final Intent intent = new Intent(this, ThemeChooserActivity.class);
        startActivity(intent);
        log("Splash: user with active session", true);
        Crashlytics.setUserName(session.getUserName());
        Crashlytics.setUserIdentifier(String.valueOf(session.getUserId()));
    }

    private void startLoginActivity() {
        final Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        log("Splash: anonymous user", false);
    }

    private void log(String log, boolean state) {
        Crashlytics.log(log);
        Crashlytics.setBool(App.CRASHLYTICS_KEY_SESSION_ACTIVATED, state);
    }
}
